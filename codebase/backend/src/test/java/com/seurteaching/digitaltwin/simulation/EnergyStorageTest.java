package com.seurteaching.digitaltwin.simulation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import com.seurteaching.digitaltwin.simulation.util.EnergyStorage;

public class EnergyStorageTest {

    private double speedupMultiplier;
    private EnergyStorage storage;
    private AtomicLong now;
    private LongSupplier timeSupplier;

    private static final double EPS = 1e-10;

    @BeforeEach
    void setUp() {
        // 1 ms real = 1 minute simulated
        speedupMultiplier = 60000.0;
        // start aligned to a minute boundary
        now = new AtomicLong(60_000L);
        timeSupplier = now::get;
        storage = new EnergyStorage(speedupMultiplier, timeSupplier);
    }

    @Test
    void testEmptyStorage() {
        assertTrue(storage.getWhPerMinute().isEmpty(), "no data should yield empty minute list");
        assertTrue(storage.getWhPerHour().isEmpty(), "no data should yield empty hour list");
        assertEquals(0.0, storage.getTotalKWH(), "no data should yield zero total Wh");
    }

    @Test
    void testSingleEntry() {
        storage.setEnergyUsage(120.0);
        assertTrue(storage.getWhPerMinute().isEmpty(), "single data point yields no completed minute");
        assertTrue(storage.getWhPerHour().isEmpty(), "single data point yields no hourly aggregation");
        assertEquals(0.0, storage.getTotalKWH(), "single data point yields zero total Wh");
    }

    @Test
    void testTwoEntriesShortInterval() {
        storage.setEnergyUsage(100.0);
        storage.setEnergyUsage(100.0); // no time advanced
        assertTrue(storage.getWhPerMinute().isEmpty(), "no full minute elapsed");
        assertTrue(storage.getWhPerHour().isEmpty(), "no full minute -> no partial hour entry");
        assertEquals(0.0, storage.getTotalKWH(), "no energy integrated");
    }

    @Test
    void testEnergyCalculation_RollsHourAndKeepsCurrentMinutes() {
        storage = new EnergyStorage(speedupMultiplier, timeSupplier);

        storage.setEnergyUsage(80.0);
        now.addAndGet(65L);     // 65 minutes simulated
        storage.setEnergyUsage(0.0);

        List<Double> minutes = storage.getWhPerMinute();
        List<Double> hours   = storage.getWhPerHour();

        // After 65 minutes: 60 rolled into hour list, 5 remain as completed minutes of current hour
        assertEquals(65, minutes.size(), "should expose only the 5 completed minutes in the current hour");
        assertEquals(2, hours.size(),  "one completed hour + current partial hour sum");

        double perMinute = 80.0 / 60.0;
        for (double m : minutes) assertEquals(perMinute, m, EPS);

        assertEquals(60 * perMinute, hours.get(0), EPS, "first (completed) hour sum");
        assertEquals(5 * perMinute,  hours.get(1), EPS, "current partial hour sum");

        double sumHours = hours.stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(sumHours, storage.getTotalKWH(), EPS, "total Wh matches hours sum");
    }

    @Test
    void testMultipleEnergyUsageCalls(TestReporter reporter) {
        storage = new EnergyStorage(speedupMultiplier, timeSupplier);

        // varied power values for 10 full-minute intervals
        double[] powers = {10.0,20.0,30.0,40.0,50.0,60.0,70.0,80.0,90.0,100.0,110.0};

        storage.setEnergyUsage(powers[0]);
        for (int i = 1; i < powers.length; i++) {
            now.addAndGet(1L);                   // +1 minute simulated
            storage.setEnergyUsage(powers[i]);   // closes a minute at previous power
        }

        List<Double> minutes = storage.getWhPerMinute();
        reporter.publishEntry("minutes", minutes.toString());

        assertEquals(powers.length - 1, minutes.size(), "should record 10 completed minutes");

        for (int i = 0; i < minutes.size(); i++) {
            assertEquals(powers[i] / 60.0, minutes.get(i), EPS, "minute #" + i);
        }

        // hour view should be a single partial hour equal to the sum of minutes
        List<Double> hours = storage.getWhPerHour();
        assertEquals(1, hours.size(), "all minutes are within the same (partial) hour");

        double sumMinutes = minutes.stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(sumMinutes, hours.get(0), EPS, "partial hour equals sum of minutes");
        assertEquals(sumMinutes, storage.getTotalKWH(), EPS, "total Wh matches");
    }

    @Test
    void testPartialMinuteFillUp() {
        // For this test: 1 ms real = 1 s simulated (so 30 ms = 30 s = 0.5 minute)
        double newSpeedupMultiplier = 1000.0;
        now.set(60_000L); // align to minute boundary
        storage = new EnergyStorage(newSpeedupMultiplier, timeSupplier);

        double P = 120.0; // watts
        storage.setEnergyUsage(P);

        // +0.5 minute (30 s) → partial minute, not exposed yet
        now.addAndGet(30L);
        storage.setEnergyUsage(P);
        assertEquals(0, storage.getWhPerMinute().size(),
                "after 30s simulated, no completed minute yet");

        // +another 0.5 minute -> completes first minute
        now.addAndGet(30L);
        storage.stopRecording();
        List<Double> minutes = storage.getWhPerMinute();
        assertEquals(1, minutes.size(), "30s + 30s should close exactly one minute");
        assertEquals(P / 60.0, minutes.get(0), EPS, "first minute Wh");

        // Now add 10s + 20s (still partial, not exposed), then +30s to close second minute
        storage.setEnergyUsage(P);
        now.addAndGet(10L);
        storage.setEnergyUsage(P);
        assertEquals(1, storage.getWhPerMinute().size(), "after +10s more, still 1 completed minute");

        now.addAndGet(20L);
        storage.setEnergyUsage(P);
        assertEquals(1, storage.getWhPerMinute().size(), "after total 30s, still partial");

        now.addAndGet(30L); // reaches 60 s total for the second minute
        storage.stopRecording();
        minutes = storage.getWhPerMinute();
        assertEquals(2, minutes.size(), "second minute now completed");
        assertEquals(P / 60.0, minutes.get(1), EPS, "second minute Wh");

        // Hour aggregation: one partial hour equal to sum of completed minutes
        List<Double> hours = storage.getWhPerHour();
        assertEquals(1, hours.size(), "both minutes are within the same partial hour");
        assertEquals(2 * (P / 60.0), hours.get(0), EPS, "hourly sum matches two minutes");

        double sumMinutes = minutes.stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(sumMinutes, storage.getTotalKWH(), EPS, "total matches minute sum");
    }

    @Test
    void testHourRollOver() {
        // 60 minutes at constant power rolls into a completed hour and clears the minute buffer
        double P = 60.0; // watts -> 1 Wh per minute
        storage.setEnergyUsage(P);
        for (int i = 0; i < 60; i++) {
            now.addAndGet(1L);   // +1 minute simulated
            storage.setEnergyUsage(P);
        }
        storage.setEnergyUsage(0.0); //stop recoding energy
        List<Double> hours = storage.getWhPerHour();
        assertEquals(1, hours.size(), "one completed hour present");
        assertEquals(P, hours.get(0), EPS, "hour energy equals P * 1h");
        assertEquals(P, storage.getTotalKWH(), EPS, "total equals the completed hour");
    }
}
