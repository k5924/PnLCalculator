package org.example.reader;

import org.example.engine.TradeQueryingService;
import org.example.shared.ConvertedTrade;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class TradeInserterPool {

    private final TradeQueryInserter[] connections;
    private final Future<?>[] tasks;
    private final int numberOfProcessors;
    private int currentConnection = 0;

    public TradeInserterPool(final int numberOfProcessors,
                             final TradeQueryingService tradeQueryingService) {
        this.connections = new TradeQueryInserter[numberOfProcessors];
        this.tasks = new Future[numberOfProcessors];
        this.numberOfProcessors = numberOfProcessors;
        for (int i = 0; i < numberOfProcessors; i++) {
            connections[i] = new TradeQueryInserter(tradeQueryingService);
        }
    }

    public void submitTrade(final ConvertedTrade trade) {
        connections[currentConnection++ % numberOfProcessors].addTrade(trade);
    }

    public void doWork() {
        try (ExecutorService executorService = Executors.newFixedThreadPool(numberOfProcessors)) {
            for (int i = 0; i < numberOfProcessors; i++) {
                final TradeQueryInserter worker = connections[i];
                final Future<?> task = executorService.submit(worker::send);
                tasks[i] = task;
            }

            for (int i = 0; i < numberOfProcessors; i++) {
                final Future<?> task = tasks[i];
                task.get();
            }

            currentConnection = 0;
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
