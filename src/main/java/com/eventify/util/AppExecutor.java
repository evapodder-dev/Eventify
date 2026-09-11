package com.eventify.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Centralized thread-pool executor for running background database, JSON, and API tasks
 * off the JavaFX Application Thread.
 */
public class AppExecutor {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("Eventify-Worker-" + thread.threadId());
        thread.setDaemon(true);
        return thread;
    });

    private AppExecutor() {
    }

    public static ExecutorService getExecutor() {
        return EXECUTOR;
    }

    public static void shutdown() {
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(2, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
