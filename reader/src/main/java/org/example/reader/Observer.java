package org.example.reader;

import java.time.Clock;

public final class Observer {

    private final String metricName;
    private final int numberOfRuns;
    private long elapsedTime = 0;
    private long minValue = Long.MAX_VALUE;
    private long maxValue = Long.MIN_VALUE;

    public Observer(final String metricName,
                    final int numberOfRuns) {
        this.metricName = metricName;
        this.numberOfRuns = numberOfRuns;
    }

    public void observe(final Runnable runnable) {
        final long startTime = Clock.systemUTC().millis();
        runnable.run();
        final long endTime = Clock.systemUTC().millis();
        final long totalTime = endTime - startTime;
        elapsedTime += totalTime;
        minValue = Math.min(totalTime, minValue);
        maxValue = Math.max(totalTime, maxValue);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(metricName)
                .append(" {min=")
                .append(minValue)
                .append("ms,")
                .append("max=")
                .append(maxValue)
                .append("ms,")
                .append("average=")
                .append(elapsedTime / numberOfRuns)
                .append("ms}")
        ;
        return sb.toString();
    }
}
