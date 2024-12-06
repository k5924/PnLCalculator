package org.example.shared;

import org.example.shared.interfaces.ObjectPool;
import org.example.shared.interfaces.Worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class DefaultWorkerPool<T extends Worker> implements ObjectPool<T> {

    private final T[] workers;
    private final Future<?>[] futures;
    private final int numberOfWorkers;
    private int currentPosition = 0;

    public DefaultWorkerPool(final int numberOfWorkers,
                             final Supplier<T> workerFactory) {
        this.numberOfWorkers = numberOfWorkers;
        this.futures = new Future[numberOfWorkers];
        this.workers = (T[]) new Worker[numberOfWorkers];
        for (int i = 0; i < numberOfWorkers; i++) {
            workers[i] = workerFactory.get();
        }
    }

    @Override
    public T get() {
        return workers[currentPosition++ % numberOfWorkers];
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
