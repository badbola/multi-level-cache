# Cache System

## Overview

This project implements a multi-level cache system with configurable levels, capacities, read times, and write times. The system uses a `CacheLibrary` to manage caching operations and a `CacheManager` to handle interactions with multiple cache levels.

## Features

- **Configurable Cache Levels**: Set up multiple cache levels with specified capacities, read times, and write times.
- **Read and Write Operations**: Perform read and write operations across cache levels.
- **Eviction Policy**: Handle cache eviction and propagation of evicted items to higher levels.
- **Statistics**: Retrieve statistics about cache usage and operation times.

## Components

### CacheLibrary

`CacheLibrary` is the main class that manages the cache levels. It provides methods for putting and getting values from the cache, and displaying cache statistics.

#### Key Methods

- `put(String key, String value)`: Adds or updates a key-value pair in the cache.
- `get(String key)`: Retrieves the value associated with a key.
- `displayStats()`: Shows statistics of the cache, including usage and average read/write times.
- `shutdown()`: Shuts down the cache system gracefully.

### CacheManager

`CacheManager` manages multiple `CacheLevel` instances and provides methods for read and write operations, including handling evictions and propagations.

#### Key Methods

- `read(String key)`: Reads a value associated with a key and updates all higher-priority cache levels.
- `write(String key, String value)`: Writes a key-value pair to all cache levels.
- `stat()`: Prints the current cache usage and average read/write times.
- `shutdown()`: Shuts down the executor service.

### Main Class

The `Main` class provides a command-line interface for interacting with the cache system. It allows users to configure cache levels and perform operations such as `WRITE`, `READ`, and `STAT`.

#### Running the Application

1. **Build the Project**

   ```sh
   mvn clean install

2. **Run the Application**

   ```sh
   java -cp target/projectMCS-1.0-SNAPSHOT.jar com.interview.Main

3. **Input Command**
    ```sh
    WRITE "key", "value": Write a key-value pair to the cache.
    READ "key": Read the value associated with a key.
    STAT: Display cache usage and statistics.
    EXIT: Exit the application.

