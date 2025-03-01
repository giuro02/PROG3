package com.client.model;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RefreshClient {
    // ReentrantReadWriteLock for handling multiple readers & single writer
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private String latestData = "";

    /**
     * Updates the data in a thread-safe manner.
     * Uses a write lock to prevent other threads from reading while updating.
     */
    public void updateData(String newData) {
        Lock writeLock = lock.writeLock(); // Get the write lock
        writeLock.lock(); // Lock it to prevent other writes/reads
        try {
            this.latestData = newData; // Update the shared data
            System.out.println("Data updated: " + newData);
        } finally {
            writeLock.unlock(); // Always unlock in the 'finally' block
        }
    }

    /**
     * Retrieves the latest data in a thread-safe way.
     * Uses a read lock to allow multiple readers but prevents writing.
     */
    public String getData() {
        Lock readLock = lock.readLock(); // Get the read lock
        readLock.lock(); // Lock it to ensure safe reading
        try {
            return latestData; // Return the stored data
        } finally {
            readLock.unlock(); // Release the lock
        }
    }
}
