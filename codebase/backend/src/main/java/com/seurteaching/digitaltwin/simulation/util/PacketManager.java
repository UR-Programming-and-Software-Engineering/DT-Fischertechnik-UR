package com.seurteaching.digitaltwin.simulation.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.seurteaching.digitaltwin.machines.Color;

/*
 * Manages packets in a thread-safe manner with a fixed maximum capacity.
 */
public class PacketManager {

    private static final int MAX_PACKETS = 100;
    private static final int MAX_AVAILABLE_HOURS = 100_000;
    private static final int ONE_HOUR_MS = 1000 * 60 * 60;

    private final Object lock = new Object();
    private final ArrayDeque<Packet> packets;
    private int totalProcessedPackets = 0;
    private final ArrayList<Integer> hourlyPackages;

    private Map<Color, Integer> packagesByType;
    private double avgTimePerPackage = 0.0;

    private final double speedMultiplier;

    public PacketManager(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
        this.packets = new ArrayDeque<>(MAX_PACKETS);
        this.hourlyPackages = new ArrayList<>();
        this.packagesByType = new HashMap<>();
    }

    public void addPacket(Packet packet) {
        synchronized (lock) {
            totalProcessedPackets++;
            if (packets.size() == MAX_PACKETS) {
                Packet evicted = packets.pollFirst(); // evict oldest

                // update avgTimePerPackage when evicting
                // note that the packet could in theory be not delivered yet
                // this is very unlikely though, as packets are usually delivered quickly
                if (evicted != null) {
                    if(evicted.isDelivered()) {
                        double evictedTime = transformToSimulatedTime(evicted.getTimeToDeliver());
                        // a2 = (a1 * n + x) / (n + 1)
                        avgTimePerPackage = ((avgTimePerPackage * (totalProcessedPackets - 1)) + evictedTime) / totalProcessedPackets;
                    }else {
                        System.err.println("Evicting undelivered packet: " + evicted.getId());
                    }
                }
            }
            packets.addLast(packet);

            int packetHour = (int) (packet.getSimulationCreationTimestamp() / ONE_HOUR_MS);

            if(packetHour >= MAX_AVAILABLE_HOURS) {
                System.err.println("Packet hour exceeds max available hours: " + packetHour);
            }

            int packetHourBounded = Math.min(packetHour, MAX_AVAILABLE_HOURS - 1);

            while (hourlyPackages.size() <= packetHour) {
                hourlyPackages.add(0);
            }

            hourlyPackages.set(packetHourBounded, hourlyPackages.get(packetHourBounded) + 1);

            int colorCounter = packagesByType.getOrDefault(packet.getPacketColor(), 0);
            packagesByType.put(packet.getPacketColor(), colorCounter + 1);
        }
    }

    public ArrayDeque<Packet> getPacketsSnapshot() {
        synchronized (lock) {
            return new ArrayDeque<>(packets);
        }
    }

    public ArrayList<Integer> getHourlyPackagesSnapshot() {
        synchronized (lock) {
            return new ArrayList<>(hourlyPackages);
        }
    }

    public void clear() {
        synchronized (lock) {
            if(packets != null) packets.clear();
            totalProcessedPackets = 0;
            if(hourlyPackages != null) hourlyPackages.clear();
            if (packagesByType != null) packagesByType.clear();
        }
    }

    public Packet getLastPacketSnapshot() {
        synchronized (lock) {
            if(packets.peekLast() != null) {
                return packets.peekLast().clone();
            } else {
                return null;
            }
        }
    }

    public int getPacketCount() {
        synchronized (lock) {
            return totalProcessedPackets;
        }
    }

    public Map<Color, Integer> getPackagesByTypeSnapshot() {
        synchronized (lock) {
            if (packagesByType == null) return Collections.emptyMap();
            return new HashMap<>(packagesByType);
        }
    }

    public double getAverageThroughput() {
        synchronized (lock) {
            if (hourlyPackages.isEmpty()) return 0.0;
            int sum = 0;
            for (int count : hourlyPackages) {
                sum += count;
            }
            return Math.round((double) sum / hourlyPackages.size() * 100) / 100.0; // round to 2 decimal places
        }
    }

    public double getAverageTimePerPackage() {
        synchronized (lock) {
            double avgTimePerPackageCopy = avgTimePerPackage;

            avgTimePerPackageCopy *= totalProcessedPackets - packets.size();

            int nonDeliveredCount = 0;
            for(Packet p : packets) {
                if(p.isDelivered()) {
                    avgTimePerPackageCopy += transformToSimulatedTime(p.getTimeToDeliver());
                }else {
                    nonDeliveredCount++;
                }
            }

            double avgTimePerPackageNs = avgTimePerPackageCopy / (totalProcessedPackets - nonDeliveredCount);
            double avgTimePerPackageSeconds = avgTimePerPackageNs / 1000.0;

            return Math.round(avgTimePerPackageSeconds * 100) / 100.0; // round to 2 decimal places
        }
    }

    private long transformToSimulatedTime(long realTimeMs) {
        return (long) (realTimeMs * speedMultiplier);
    }

}
