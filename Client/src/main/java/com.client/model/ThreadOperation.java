package com.client.model;

import java.util.concurrent.locks.ReentrantLock;

public class ThreadOperation {
    // ReentrantLock allows one thread at a time to access a critical section
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Performs a critical operation in a thread-safe way.
     * Uses a lock to ensure that only one thread can run it at a time.
     */
    public void performOperation() {
        lock.lock(); // Acquire the lock
        try {
            System.out.println("Performing a critical operation...");
            // Simulating some work (e.g., accessing shared resources)
            Thread.sleep(1000);
            System.out.println("Operation completed.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Preserve interruption status
            System.err.println("Operation was interrupted.");
        } finally {
            lock.unlock(); // Always release the lock
        }
    }
}
