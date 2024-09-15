package com.interview.level;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCacheLevel implements CacheLevel {
    private final int capacity;
    private final int readTime;
    private final int writeTime;
    private final LinkedHashMap<String, String> cache;

    public LRUCacheLevel(int capacity, int readTime, int writeTime) {
        this.capacity = capacity;
        this.readTime = readTime;
        this.writeTime = writeTime;

        // Initializing the cache with LRU behavior (access order set to true)
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true);  // accessOrder = true maintains LRU
    }

    @Override
    public String get(String key) throws IOException {
        // Simulate read time
        try {
            Thread.sleep(readTime);  // Simulating read time delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Read operation interrupted", e);
        }

        return cache.get(key);
    }


    @Override
    public boolean put(String key, String value) throws IOException {
        // Check if the cache is at capacity before adding a new item
        try {
            Thread.sleep(writeTime);  // Simulating read time delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Write operation interrupted", e);
        }
        cache.put(key, value);
        if (cache.size() > capacity) {
            return false;
        }
        return true;
    }

    @Override
    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public int getReadTime() {
        return readTime;
    }

    @Override
    public int getWriteTime() {
        return writeTime;
    }

    @Override
    public boolean isCapacityBreached() {
        // Check if the size exceeds capacity
        return cache.size() > capacity;
    }

    @Override
    public Map.Entry<String, String> evictLastCache() {
        // Manually evict the eldest entry if the capacity is breached
        if (cache.size() > capacity) {
            Map.Entry<String, String> eldestEntry = cache.entrySet().iterator().next();
            cache.remove(eldestEntry.getKey());
            return eldestEntry;
        }
        return null;
    }
}
