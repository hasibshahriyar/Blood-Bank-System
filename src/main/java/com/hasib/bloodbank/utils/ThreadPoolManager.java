package com.hasib.bloodbank.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Centralized thread pool manager for the Blood Bank application
 */
public class ThreadPoolManager {
    private static ThreadPoolManager instance;
    private final ExecutorService networkExecutor;
    private final ExecutorService databaseExecutor;
    private final ExecutorService notificationExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    private ThreadPoolManager() {
        // Network operations thread pool (for socket communications)
        this.networkExecutor = Executors.newFixedThreadPool(10, new ThreadFactory() {
            private int counter = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "NetworkThread-" + (++counter));
                t.setDaemon(false);
                t.setUncaughtExceptionHandler((thread, ex) -> {
                    System.err.println("Uncaught exception in " + thread.getName() + ": " + ex.getMessage());
                    ex.printStackTrace();
                });
                return t;
            }
        });

        // Database operations thread pool
        this.databaseExecutor = Executors.newFixedThreadPool(5, new ThreadFactory() {
            private int counter = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "DatabaseThread-" + (++counter));
                t.setDaemon(false);
                t.setUncaughtExceptionHandler((thread, ex) -> {
                    System.err.println("Uncaught exception in " + thread.getName() + ": " + ex.getMessage());
                    ex.printStackTrace();
                });
                return t;
            }
        });

        // Notification processing thread pool
        this.notificationExecutor = Executors.newFixedThreadPool(3, new ThreadFactory() {
            private int counter = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "NotificationThread-" + (++counter));
                t.setDaemon(false);
                t.setUncaughtExceptionHandler((thread, ex) -> {
                    System.err.println("Uncaught exception in " + thread.getName() + ": " + ex.getMessage());
                    ex.printStackTrace();
                });
                return t;
            }
        });

        // Scheduled tasks (periodic operations)
        this.scheduledExecutor = Executors.newScheduledThreadPool(2, new ThreadFactory() {
            private int counter = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "ScheduledThread-" + (++counter));
                t.setDaemon(true);
                t.setUncaughtExceptionHandler((thread, ex) -> {
                    System.err.println("Uncaught exception in " + thread.getName() + ": " + ex.getMessage());
                    ex.printStackTrace();
                });
                return t;
            }
        });
    }

    public static synchronized ThreadPoolManager getInstance() {
        if (instance == null) {
            instance = new ThreadPoolManager();
        }
        return instance;
    }

    public CompletableFuture<Void> executeNetworkTask(Runnable task) {
        if (isShutdown.get()) {
            throw new IllegalStateException("ThreadPoolManager has been shutdown");
        }
        return CompletableFuture.runAsync(task, networkExecutor);
    }

    public <T> CompletableFuture<T> executeNetworkTask(Callable<T> task) {
        if (isShutdown.get()) {
            throw new IllegalStateException("ThreadPoolManager has been shutdown");
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, networkExecutor);
    }

    public CompletableFuture<Void> executeDatabaseTask(Runnable task) {
        if (isShutdown.get()) {
            throw new IllegalStateException("ThreadPoolManager has been shutdown");
        }
        return CompletableFuture.runAsync(task, databaseExecutor);
    }

    public <T> CompletableFuture<T> executeDatabaseTask(Callable<T> task) {
        if (isShutdown.get()) {
            throw new IllegalStateException("ThreadPoolManager has been shutdown");
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, databaseExecutor);
    }

    public CompletableFuture<Void> executeNotificationTask(Runnable task) {
        if (isShutdown.get()) {
            throw new IllegalStateException("ThreadPoolManager has been shutdown");
        }
        return CompletableFuture.runAsync(task, notificationExecutor);
    }

    public <T> CompletableFuture<T> executeNotificationTask(Callable<T> task) {
        if (isShutdown.get()) {
            throw new IllegalStateException("ThreadPoolManager has been shutdown");
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, notificationExecutor);
    }

    public ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit unit) {
        if (isShutdown.get()) {
            throw new IllegalStateException("ThreadPoolManager has been shutdown");
        }
        return scheduledExecutor.schedule(task, delay, unit);
    }

    public ScheduledFuture<?> schedulePeriodicTask(Runnable task, long initialDelay, long period, TimeUnit unit) {
        if (isShutdown.get()) {
            throw new IllegalStateException("ThreadPoolManager has been shutdown");
        }
        return scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            System.out.println("Shutting down ThreadPoolManager...");

            networkExecutor.shutdown();
            databaseExecutor.shutdown();
            notificationExecutor.shutdown();
            scheduledExecutor.shutdown();

            try {
                if (!networkExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    networkExecutor.shutdownNow();
                }
                if (!databaseExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    databaseExecutor.shutdownNow();
                }
                if (!notificationExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    notificationExecutor.shutdownNow();
                }
                if (!scheduledExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                networkExecutor.shutdownNow();
                databaseExecutor.shutdownNow();
                notificationExecutor.shutdownNow();
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            System.out.println("ThreadPoolManager shutdown complete");
        }
    }

    public boolean isShutdown() {
        return isShutdown.get();
    }

    // Runtime hook to ensure proper shutdown
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (instance != null) {
                instance.shutdown();
            }
        }));
    }
}
