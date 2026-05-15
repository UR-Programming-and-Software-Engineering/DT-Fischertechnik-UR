package com.seurteaching.digitaltwin.simulation.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.LongSupplier;

/*
 * Thread safe class to store energy usage over time
 * Stores energy usage in Wh per minute and per hour
 * Minute count is capped to MAX_MINUTES
 */
public class EnergyStorage {

    private static final long ONE_MIN_MS = 60_000L;
    private static final long ONE_HOUR_MS = 3_600_000L;

    private static final int MAX_HISTORY_HOURS = 5;
    private static final int MAX_MINUTES = MAX_HISTORY_HOURS * 60;

    private final LongSupplier timeSupplier;
    private final Object lock = new Object();

    private long lastRealMs;
    private long lastSimMs;
    private double lastUsage;

    private final LinkedList<Double> whPerMinute = new LinkedList<>();
    private final ArrayList<Double> whPerHour = new ArrayList<>();
    private double currentMinuteWh = 0.0;
    private double currentHourSum = 0.0;
    private int minutesInCurrentHour = 0;

    private final double speedMultiplier;

    public EnergyStorage(double sppedMultiplier, LongSupplier timeSupplier) {
        this.speedMultiplier = sppedMultiplier;
        this.timeSupplier = timeSupplier;
        this.lastRealMs = -1L;
        this.lastSimMs  = 0L;
        this.lastUsage  = 0.0;
    }

    public EnergyStorage(double speedMultiplier) {
        this(speedMultiplier, System::currentTimeMillis);
    }

    public ArrayList<Double> getWhPerMinute() {
        synchronized (lock) {
            return new ArrayList<>(whPerMinute);
        }
    }

    public ArrayList<Double> getWhPerHour() {
        synchronized (lock) {
            ArrayList<Double> out = new ArrayList<>(whPerHour.size() + 1);
            out.addAll(whPerHour);
            if (minutesInCurrentHour > 0) out.add(currentHourSum);
            return out;
        }
    }
    
    public double getTotalKWH() {
        synchronized (lock) {
            double sum = 0.0;
            for (double h : whPerHour) sum += h;
            sum += currentHourSum;
            return sum;
        }
    }

    /*
     * thread safe methods to change state
     */

    public void reset() {
        synchronized (lock) {
            whPerMinute.clear();
            whPerHour.clear();
            currentMinuteWh = 0.0;
            currentHourSum = 0.0;
            minutesInCurrentHour = 0;
            lastRealMs = -1L;
            lastSimMs  = 0L;
            lastUsage  = 0.0;
        }
    }

    public void stopRecording() {
        setEnergyUsage(0.0);
    }

    public void setEnergyUsage(double currentEnergyUsage) {

        synchronized (lock) {
        
            //in idle multiple calls with same usage are triggered
            if(currentEnergyUsage == lastUsage) {
                return;
            }

            long nowReal = timeSupplier.getAsLong();

            if (lastRealMs >= 0) {
                long deltaReal = Math.max(0L, nowReal - lastRealMs);
                long simDelta  = Math.round(deltaReal * speedMultiplier);
                long remaining = simDelta;

                // complete current simulated minute (if we had a partial)
                long msIntoMinute = (lastSimMs % ONE_MIN_MS);
                long remToFull    = (msIntoMinute == 0) ? 0 : (ONE_MIN_MS - msIntoMinute);
                if (remToFull > 0 && remaining > 0) {
                    long firstMs = Math.min(remToFull, remaining);
                    addToCurrentMinute(firstMs);
                    if(firstMs == remToFull) {
                        closeOutMinute();
                    }
                    lastSimMs += firstMs;
                    remaining -= firstMs;
                }

                // full simulated minutes
                long fullMin = remaining / ONE_MIN_MS;
                for (int i = 0; i < fullMin; i++) {
                    appendCompletedMinute(lastUsage * (ONE_MIN_MS / (double) ONE_HOUR_MS));
                }
                long advanced = fullMin * ONE_MIN_MS;
                lastSimMs += advanced;
                remaining -= advanced;

                // leftover partial simulated minute
                if (remaining > 0) {
                    addToCurrentMinute(remaining);
                    lastSimMs += remaining;
                }
            }

            lastRealMs = nowReal;
            lastUsage  = currentEnergyUsage;
        }
    }

    private void addToCurrentMinute(long millis) {
        currentMinuteWh += lastUsage * (millis / (double) ONE_HOUR_MS);
    }

    private void closeOutMinute() {
        appendCompletedMinute(currentMinuteWh);
        currentMinuteWh = 0.0;
    }

    private void appendCompletedMinute(double minuteWh) {
        whPerMinute.addLast(minuteWh);
        if (whPerMinute.size() > MAX_MINUTES) whPerMinute.removeFirst();

        currentHourSum += minuteWh;
        minutesInCurrentHour++;
        if (minutesInCurrentHour >= 60) {
            whPerHour.add(currentHourSum);
            currentHourSum = 0.0;
            minutesInCurrentHour = 0;
        }
    }


}
