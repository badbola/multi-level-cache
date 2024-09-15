package com.interview.manager;

import com.interview.level.CacheLevel;
import com.interview.manager.CacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

class CacheManagerTest {

    private CacheManager cacheManager;
    private CacheLevel mockLevel1;
    private CacheLevel mockLevel2;
    private CacheLevel mockLevel3;

    @BeforeEach
    void setUp() {
        // Create mock CacheLevel objects
        mockLevel1 = mock(CacheLevel.class);
        mockLevel2 = mock(CacheLevel.class);
        mockLevel3 = mock(CacheLevel.class);


        // Create a list of CacheLevel objects and add the mocks
        List<CacheLevel> levels = new ArrayList<>();
        levels.add(mockLevel1);
        levels.add(mockLevel2);

        // Initialize CacheManager with the mocked levels
        cacheManager = new CacheManager(levels);
    }

    @Test
    void testReadKeyPresent() throws IOException {
        // Setup the behavior of the mocks
        when(mockLevel1.get("7")).thenReturn(null);
        when(mockLevel2.get("7")).thenReturn("value7");

        // Call the method under test
        String result = cacheManager.read("7");

        // Verify interactions with the mocks
        verify(mockLevel1).get("7");
        verify(mockLevel2).get("7");

        // Optionally, assert the result if needed
        assertEquals("value7", result);
    }

    @Test
    public void testWrite() throws IOException {
        // Simulate write and eviction
        when(mockLevel1.put("1", "value1")).thenReturn(false);
        when(mockLevel1.evictLastCache()).thenReturn(Map.entry("2", "value2"));
        when(mockLevel2.put("2", "value2")).thenReturn(true);

        cacheManager.write("1", "value1");

        // Verify write and eviction propagation
        verify(mockLevel1, times(1)).put("1", "value1");
        verify(mockLevel1, times(1)).evictLastCache();
        verify(mockLevel2, times(1)).put("2", "value2");
    }

    @Test
    public void testStat() {
        // Mock cache levels
        when(mockLevel1.size()).thenReturn(1);
        when(mockLevel1.capacity()).thenReturn(5);
        when(mockLevel2.size()).thenReturn(2);
        when(mockLevel2.capacity()).thenReturn(5);
        when(mockLevel3.size()).thenReturn(3);
        when(mockLevel3.capacity()).thenReturn(5);

        // Add dummy times
        Queue<Long> readTimes = new ConcurrentLinkedQueue<>(Arrays.asList(10L, 20L, 30L, 40L, 50L));
        Queue<Long> writeTimes = new ConcurrentLinkedQueue<>(Arrays.asList(15L, 25L, 35L, 45L, 55L));
        cacheManager = new CacheManager(Arrays.asList(mockLevel1, mockLevel2, mockLevel3));

        // Mock internal queues
        try {
            // Use reflection to set private fields
            java.lang.reflect.Field readTimesField = cacheManager.getClass().getDeclaredField("readTimes");
            readTimesField.setAccessible(true);
            readTimesField.set(cacheManager, readTimes);

            java.lang.reflect.Field writeTimesField = cacheManager.getClass().getDeclaredField("writeTimes");
            writeTimesField.setAccessible(true);
            writeTimesField.set(cacheManager, writeTimes);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }

        // Capture output using System.out replacement or other means
        cacheManager.stat();

        // Verify output - needs capturing System.out output or use a logging framework
    }

    @Test
    public void testShutdown() {
        cacheManager.shutdown();
        // Verify shutdown is called on the executor service
        // This would need capturing or asserting the shutdown state if possible
    }

    // Additional tests here
}
