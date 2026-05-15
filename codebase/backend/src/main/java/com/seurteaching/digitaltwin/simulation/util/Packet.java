package com.seurteaching.digitaltwin.simulation.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.seurteaching.digitaltwin.machines.Color;

public class Packet {

    private long id;
    private long creationTimestamp;
    private long simulationCreationTimestamp; // 0 would be at the start of the simulation
    private boolean isDelivered;
    private Color packetcolor;
    private List<String> packetlocation;// transformed into synchronized list in constructor
    private int index;
    private long timeToDeliver;

    public Packet(long id, long creationTimestamp, long simulationCreationTimestamp, Color packetcolor) {
        this.id = id;
        this.creationTimestamp = creationTimestamp;
        this.packetcolor = packetcolor;
        this.packetlocation = Collections.synchronizedList(new ArrayList<>());
        this.simulationCreationTimestamp = simulationCreationTimestamp;
    }

    public long getSimulationCreationTimestamp() {
        return simulationCreationTimestamp;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public long getId() {
        return id;
    }

    public synchronized boolean isDelivered() {
        return isDelivered;
    }

    public synchronized void setDelivered() {
        if (isDelivered) return;
        
        this.isDelivered = true;

        long deliveredTimestamp = System.currentTimeMillis();

        this.timeToDeliver = (long) (deliveredTimestamp - creationTimestamp);
    }

    public Color getPacketColor() {
        return packetcolor;
    }

    public List<String> getPacketLocation() {
        return packetlocation;
    }

    public String getCurrentLocation() {
        synchronized (packetlocation) {
            int size = packetlocation.size();
            return size == 0 ? null : packetlocation.get(size - 1);
        }
    }

    public synchronized int getIndex() {
        return index;
    }

    public synchronized long getTimeToDeliver() {
        return timeToDeliver;
    }

    public synchronized void incrementIndex() {
        this.index++;
    }

    public Packet clone() {
        Packet clonedPacket = new Packet(this.id, this.creationTimestamp, this.simulationCreationTimestamp, this.packetcolor);
        // copy simple fields atomically
        synchronized (this) {
            clonedPacket.isDelivered = this.isDelivered;
            clonedPacket.index = this.index;
            clonedPacket.timeToDeliver = this.timeToDeliver;
        }
        // copy list contents safely
        synchronized (this.packetlocation) {
            clonedPacket.packetlocation.addAll(this.packetlocation);
        }
        return clonedPacket;
    }
}
