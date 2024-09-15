package com.interview.level;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public interface CacheLevel {
    String get(String key) throws IOException;

    boolean put(String key, String value) throws IOException;

    boolean containsKey(String key);

    int size();

    int capacity();

    int getReadTime();

    int getWriteTime();

    boolean isCapacityBreached();

    Map.Entry<String, String>  evictLastCache();
}

