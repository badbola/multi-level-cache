package com.interview;

import com.interview.library.CacheLibrary;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // Get number of cache levels from user
            System.out.println("Enter the number of cache levels: ");
            int numberOfLevels = scanner.nextInt();

            // Initialize arrays for capacities, read times, and write times
            int[] capacities = new int[numberOfLevels];
            int[] readTimes = new int[numberOfLevels];
            int[] writeTimes = new int[numberOfLevels];

            // Get capacities for each cache level
            for (int i = 0; i < numberOfLevels; i++) {
                System.out.println("Enter the capacity for cache level " + (i + 1) + ": ");
                capacities[i] = scanner.nextInt();
            }

            // Get read times for each cache level
            for (int i = 0; i < numberOfLevels; i++) {
                System.out.println("Enter the read time for cache level " + (i + 1) + " (in ms): ");
                readTimes[i] = scanner.nextInt();
            }

            // Get write times for each cache level
            for (int i = 0; i < numberOfLevels; i++) {
                System.out.println("Enter the write time for cache level " + (i + 1) + " (in ms): ");
                writeTimes[i] = scanner.nextInt();
            }

            // Initialize CacheLibrary
            CacheLibrary cacheLibrary = new CacheLibrary(capacities, readTimes, writeTimes);

            // Input processing loop
            scanner.nextLine(); // Clear the buffer
            while (true) {
                System.out.println("Input: ");
                String input = scanner.nextLine();

                // Exit condition
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }

                // Process WRITE operation
                if (input.startsWith("WRITE")) {
                    String[] parts = input.split("\"");
                    if (parts.length >= 3) {
                        String key = parts[1].trim();
                        String value = parts[3].trim();
                        cacheLibrary.put(key, value);
                    } else {
                        System.out.println("Invalid WRITE command format.");
                    }

                    // Process READ operation
                } else if (input.startsWith("READ")) {
                    String[] parts = input.split("\"");
                    if (parts.length >= 2) {
                        String key = parts[1].trim();
                        String result = cacheLibrary.get(key);
                        if (result != null) {
                            System.out.println("Value for key '" + key + "': " + result);
                        } else {
                            System.out.println("Key Not Present");
                        }
                    } else {
                        System.out.println("Invalid READ command format.");
                    }

                    // Process STAT operation
                } else if (input.equalsIgnoreCase("STAT")) {
                    cacheLibrary.displayStats();

                } else {
                    System.out.println("Unknown command. Valid commands are: WRITE, READ, STAT, or exit.");
                }
            }

            // Shutdown the cache system
            cacheLibrary.shutdown();

        } catch (InputMismatchException e) {
            System.err.println("Invalid input. Please enter numeric values.");
        } catch (IOException e) {
            System.err.println("Error initializing CacheLibrary: " + e.getMessage());
        }
    }
}
