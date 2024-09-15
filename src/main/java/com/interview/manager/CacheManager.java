package com.interview.manager;

import com.interview.level.CacheLevel;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CacheManager manages multiple cache levels and provides methods for read, write, and statistics operations.
 */
public class CacheManager {
    private final List<CacheLevel> levels;
    private final Queue<Long> readTimes;
    private final Queue<Long> writeTimes;
    private final ExecutorService executorService;

    /**
     * Constructs a CacheManager with the specified cache levels.
     *
     * @param levels A list of cache levels to manage.
     */
    public CacheManager(List<CacheLevel> levels) {
        this.levels = levels;
        this.readTimes = new ConcurrentLinkedQueue<>();
        this.writeTimes = new ConcurrentLinkedQueue<>();
        this.executorService = Executors.newFixedThreadPool(4);  // Adjust pool size based on needs
    }

    /**
     * Reads the value associated with the specified key from the cache.
     * If the key is not found, it searches subsequent cache levels.
     * If found, updates all higher-priority cache levels with the new value.
     *
     * @param key the key to be read
     * @return the value associated with the key, or null if not found
     */
    public String read(String key) throws IOException {
        long totalReadTime = 0;
        String value = null;
        int foundLevel = -1;

        for (int i = 0; i < levels.size(); i++) {
            CacheLevel level = levels.get(i);
            totalReadTime += level.getReadTime();
            value = level.get(key);
            if (value != null) {
                foundLevel = i;
                break;
            }
        }

        if (value == null) {
            System.out.println("Key Not Present");
        } else {
            System.out.println("Key: " + key + " [Found in L" + (foundLevel + 1) + "]");
            asyncWriteToHigherLevels(key, value, foundLevel);
        }

        recordReadTime(totalReadTime);
        System.out.println("[Read Time: " + totalReadTime + " ms]");
        return value;
    }

    /**
     * Writes the key-value pair to all cache levels starting from L1.
     * If the key already has the same value in any level, no write is performed at that level.
     *
     * @param key   the key to be written
     * @param value the value to be associated with the key
     */
    public void write(String key, String value) {
        executorService.submit(() -> {
            long totalWriteTime = 0;
            boolean isWritten = false;

            // Use AtomicReference for evictedKey and evictedValue to ensure mutability inside lambda
            AtomicReference<String> evictedKey = new AtomicReference<>(null);
            AtomicReference<String> evictedValue = new AtomicReference<>(null);

            for (int i = 0; i < levels.size(); i++) {
                CacheLevel level = levels.get(i);

                try {
                    // If the key exists with the same value, skip writing to this level
                    if (level.containsKey(key) && level.get(key).equals(value)) {
                        break;
                    }

                    // Evict if capacity is reached BEFORE adding the new item
//                    if (level.isCapacityBreached()) {
//
//                    }

                    // Write the key-value pair to the current level if not already written
                    if (!isWritten) {
                        synchronized (level) {
                            if (!level.put(key, value)){
                                Map.Entry<String, String> evictedEntry = level.evictLastCache();
                                evictedKey.set(evictedEntry.getKey());
                                evictedValue.set(evictedEntry.getValue());

                            }
                        }
                        totalWriteTime += level.getWriteTime();
                        isWritten = true;
                    }

                    // If an eviction occurred, try to move the evicted entry to the next level
                    if (evictedKey.get() != null && i + 1 < levels.size()) {
                        CacheLevel nextLevel = levels.get(i + 1);
                        synchronized (nextLevel) {
                            nextLevel.put(evictedKey.get(), evictedValue.get());
                        }
                        totalWriteTime += nextLevel.getWriteTime();
                        evictedKey.set(null);  // Reset evicted key after passing it to the next level
                        evictedValue.set(null);
                    } else{
                        break;
                    }

                } catch (IOException e) {
                    throw new RuntimeException("Error writing to cache level", e);
                }
            }

            // If eviction reaches the last level, log the final eviction
            if (evictedKey.get() != null) {
                System.out.println("Final eviction at the last level: " + evictedKey.get() + " -> " + evictedValue.get());
            }

            recordWriteTime(totalWriteTime);
            System.out.println("Took " + totalWriteTime + " ms to write");
        });
    }

    /**
     * Prints the current usage and average read and write times of the cache system.
     */
    public void stat() {
        System.out.println("Current Cache Usage:");
        for (int i = 0; i < levels.size(); i++) {
            CacheLevel level = levels.get(i);
            System.out.println("L" + (i + 1) + ": " + level.size() + "/" + level.capacity());
        }

        System.out.println("Average READ Time (last 5 operations): " + getAverageTime(readTimes) + " ms");
        System.out.println("Average WRITE Time (last 5 operations): " + getAverageTime(writeTimes) + " ms");
    }

    /**
     * Shuts down the ExecutorService to stop all asynchronous tasks.
     */
    public void shutdown() {
        executorService.shutdown();
        System.out.println("ExecutorService shut down.");
    }

    private void asyncWriteToHigherLevels(String key, String value, int startLevel) {
        AtomicReference<String> evictedKey = new AtomicReference<>(key);
        AtomicReference<String> evictedValue = new AtomicReference<>(value);
        AtomicBoolean isWritten = new AtomicBoolean(false);  // To track the write state
        AtomicLong totalWriteTime = new AtomicLong(0);       // To track total write time

        // Iterate over levels from 0 up to (but not including) the startLevel
        for (int i = 0; i < startLevel && i < levels.size(); i++) {
            CacheLevel level = levels.get(i);

            int finalI = i;
            executorService.submit(() -> {
                try {
                    synchronized (level) {
                        // Write to the current level if it hasn't been written yet
                        if (!isWritten.get()) {
                            if (!level.put(evictedKey.get(), evictedValue.get())) {
                                // Eviction occurred, evict the last entry
                                Map.Entry<String, String> evictedEntry = level.evictLastCache();
                                evictedKey.set(evictedEntry.getKey());
                                evictedValue.set(evictedEntry.getValue());
                            } else {
                                // Successfully written, reset evicted key and value
                                evictedKey.set(null);
                                evictedValue.set(null);
                            }
                            totalWriteTime.addAndGet(level.getWriteTime());  // Accumulate write time
                            isWritten.set(true);  // Mark as written
                        }

                        // If an eviction occurred, propagate the evicted key-value to the next level
                        if (evictedKey.get() != null && finalI + 1 < startLevel) {
                            CacheLevel nextLevel = levels.get(finalI + 1);
                            synchronized (nextLevel) {
                                nextLevel.put(evictedKey.get(), evictedValue.get());
                            }
                            totalWriteTime.addAndGet(nextLevel.getWriteTime());  // Accumulate write time
                            evictedKey.set(null);  // Reset after passing to the next level
                            evictedValue.set(null);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error writing to cache level", e);
                }
            });
        }

        // If eviction reaches the last level in the range, log the final eviction
        if (evictedKey.get() != null && startLevel>0) {
            System.out.println("Final eviction at level " + (startLevel - 1) + ": " + evictedKey.get() + " -> " + evictedValue.get());
        }

        // Record the total write time
        recordWriteTime(totalWriteTime.get());  // Use totalWriteTime with AtomicLong's get()
        System.out.println("Took " + totalWriteTime.get() + " ms to write to higher levels.");
    }


    private void recordReadTime(long time) {
        if (readTimes.size() >= 5) {
            readTimes.poll();
        }
        readTimes.offer(time);
    }

    private void recordWriteTime(long time) {
        if (writeTimes.size() >= 5) {
            writeTimes.poll();
        }
        writeTimes.offer(time);
    }

    private long getAverageTime(Queue<Long> times) {
        return times.stream().mapToLong(Long::longValue).sum() / (times.size() > 0 ? times.size() : 1);
    }

    /**
     * Returns the level at which the key is present.
     *
     * @param key the key to check
     * @return the index of the level containing the key, or -1 if not found
     */
    public int getLevelOfKey(String key) {
        for (int i = 0; i < levels.size(); i++) {
            if (levels.get(i).containsKey(key)) {
                return i;
            }
        }
        return -1;
    }
}
