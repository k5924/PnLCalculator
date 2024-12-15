package org.example.shared;

import org.example.shared.interfaces.ObjectPool;
import org.example.shared.interfaces.Worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class DefaultWorkerPool<T extends Worker> implements ObjectPool<T> {

    private final Supplier<T> workerFactory;
    private final int numberOfThreads;
    private T[] workers;
    private Future<?>[] futures;
    private int numberOfWorkers;
    private int currentPosition = 0;

    public DefaultWorkerPool(final Supplier<T> workerFactory,
                             final int numberOfThreads) {
        this.workerFactory = workerFactory;
        this.numberOfThreads = numberOfThreads;
    }

    public DefaultWorkerPool<T> setNumberOfWorkers(final int numberOfWorkers) {
        this.numberOfWorkers = Math.min(numberOfWorkers, numberOfThreads);
        this.futures = new Future[this.numberOfWorkers];
        this.workers = (T[]) new Worker[this.numberOfWorkers];
        for (int i = 0; i < this.numberOfWorkers; i++) {
            workers[i] = workerFactory.get();
        }
        return this;
    }

    @Override
    public T get() {
        return get(currentPosition++);
    }

    @Override
    public T get(final int hashCode) {
        return workers[Math.abs(hashCode % numberOfWorkers)];
    }

    @Override
    public void doWork() {
        try (ExecutorService executorService = Executors.newFixedThreadPool(numberOfWorkers)) {
            for (int i = 0; i < numberOfWorkers; i++) {
                final T worker = workers[i];
                final Future<?> future = executorService.submit(worker::doWork);
                futures[i] = future;
            }

            for (int i = 0; i < numberOfWorkers; i++) {
                final Future<?> future = futures[i];
                future.get();
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
