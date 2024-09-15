package com.interview.library;

import com.interview.level.CacheLevel;
import com.interview.level.LRUCacheLevel;
import com.interview.manager.CacheManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CacheLibrary provides an interface to interact with the CacheManager.
 */
public class CacheLibrary {
    private final CacheManager cacheManager;

    /**
     * Initializes the CacheLibrary with given configurations.
     *
     * @param capacities Array of capacities for each cache level.
     * @param readTimes  Array of read times for each cache level.
     * @param writeTimes Array of write times for each cache level.
     * @param filePaths  Array of file paths for file-based cache levels.
     * @throws IOException If there is an error initializing the file-based caches.
     */
    public CacheLibrary(int[] capacities, int[] readTimes, int[] writeTimes) throws IOException {
        List<CacheLevel> levels = createCacheLevels(capacities, readTimes, writeTimes);
        this.cacheManager = new CacheManager(levels);
    }

    /**
     * Creates a list of cache levels with the specified configurations.
     *
     * @param capacities Array of capacities for each cache level.
     * @param readTimes  Array of read times for each cache level.
     * @param writeTimes Array of write times for each cache level.

     * @return List of CacheLevel objects.
     */
    private List<CacheLevel> createCacheLevels(int[] capacities, int[] readTimes, int[] writeTimes) throws IOException {
        List<CacheLevel> levels = new ArrayList<>();
        for (int i = 0; i < capacities.length; i++) {

            levels.add(new LRUCacheLevel(capacities[i], readTimes[i], writeTimes[i]));

        }
        return levels;
    }

    /**
     * Writes a key-value pair to the cache.
     * The write operation is asynchronous and managed by the CacheManager.
     *
     * @param key   The key to write.
     * @param value The value to write.
     */
    public void put(String key, String value) {
        try {
            cacheManager.write(key, value);  // Asynchronous write managed by CacheManager
        } catch (Exception e) {
            System.err.println("Error during write operation: " + e.getMessage());
        }
    }

    /**
     * Reads a value from the cache for a given key.
     *
     * @param key The key to read.
     * @return The value associated with the key.
     */
    public String get(String key) {
        long startTime = System.currentTimeMillis();
        String value = null;
        try {
            value = cacheManager.read(key);  // Synchronous read
            long endTime = System.currentTimeMillis();
            if (value != null) {
                System.out.println(value + " [Found in L" + (cacheManager.getLevelOfKey(key) + 1) + " Cache] [Read Time: " + (endTime - startTime) + " ms]");
            } else {
                System.out.println("Key Not Present");
            }
        } catch (Exception e) {
            System.err.println("Error during read operation: " + e.getMessage());
        }
        return value;
    }

    /**
     * Displays cache statistics.
     */
    public void displayStats() {
        System.out.println("Current Usage:");
        try {
            cacheManager.stat();
        } catch (Exception e) {
            System.err.println("Error displaying cache statistics: " + e.getMessage());
        }
    }

    /**
     * Shuts down the CacheManager's executor service and ensures all asynchronous tasks are completed.
     */
    public void shutdown() {
        try {
            cacheManager.shutdown();  // Shutdown ExecutorService
            System.out.println("Cache system shut down successfully.");
        } catch (Exception e) {
            System.err.println("Error shutting down cache system: " + e.getMessage());
        }
    }
}
