package org.example.reader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class WorkerPool {

    private final int numberOfProcessors;
    private final Worker[] workers;
    private final Future<?>[] tasks;
    private int currentPosition = 0;

    public WorkerPool(final TradeIndexingService tradeIndexingService,
                      final int numberOfProcessors) {
        this.numberOfProcessors = numberOfProcessors;
        this.workers = new Worker[numberOfProcessors];
        this.tasks = new Future[numberOfProcessors];
        for (int i = 0; i < numberOfProcessors; i++) {
            workers[i] = new Worker(tradeIndexingService);
        }
    }

    public Worker get() {
        return workers[currentPosition++];
    }

    public void doWork() {
        try (ExecutorService executorService = Executors.newFixedThreadPool(numberOfProcessors)) {
            for (int i = 0; i < numberOfProcessors; i++) {
                final Worker work = workers[i];
                final Future<?> task = executorService.submit(work::processSlice);
                tasks[i] = task;
            }

            for (int i = 0; i < numberOfProcessors; i++) {
                final Future<?> task = tasks[i];
                task.get();
            }

            currentPosition = 0;
            executorService.shutdown();
            final boolean finished = executorService.awaitTermination(60, TimeUnit.SECONDS);
            if (!finished) {
                executorService.shutdownNow();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
