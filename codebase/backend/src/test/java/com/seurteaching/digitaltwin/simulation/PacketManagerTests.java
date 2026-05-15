package com.seurteaching.digitaltwin.simulation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.seurteaching.digitaltwin.SpringContext;
import com.seurteaching.digitaltwin.machines.Color;
import com.seurteaching.digitaltwin.simulation.util.Packet;
import com.seurteaching.digitaltwin.simulation.util.PacketManager;

@ExtendWith(MockitoExtension.class)
public class PacketManagerTests {

    private PacketManager packetManager;
    
    private MockedStatic<SpringContext> mockedSpringContext;
    
    @BeforeEach
    public void setUp() {
        packetManager = new PacketManager(1);
    }
    
    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        if (mockedSpringContext != null) {
            mockedSpringContext.close();
        }
    }
    
    @Test
    public void testAddPacket() {
        Packet packet = new Packet(1L, System.currentTimeMillis(), 0L, Color.RED);
        packetManager.addPacket(packet);
        
        assertEquals(1, packetManager.getPacketCount());
        
        ArrayDeque<Packet> packets = packetManager.getPacketsSnapshot();
        assertEquals(1, packets.size());
        assertEquals(packet.getId(), packets.peekFirst().getId());
    }
    
    @Test
    public void testMaxCapacity() {
        // Add more than MAX_PACKETS (100) packets
        for (int i = 0; i < 120; i++) {
            packetManager.addPacket(new Packet(i, System.currentTimeMillis(), i * 10000, Color.RED));
        }
        
        // Verify we maintain max capacity
        assertEquals(100, packetManager.getPacketsSnapshot().size());
        // But the total count is still accurate
        assertEquals(120, packetManager.getPacketCount());
    }
    
    @Test
    public void testHourlyPackages() {
        long currentTime = System.currentTimeMillis();
        int hourInMs = 60 * 60 * 1000;
        
        packetManager.addPacket(new Packet(1, currentTime, 0, Color.RED));
        packetManager.addPacket(new Packet(2, currentTime, 0, Color.BLUE));
        packetManager.addPacket(new Packet(3, currentTime, hourInMs, Color.WHITE)); // 1 hour
        
        ArrayList<Integer> hourlyPackages = packetManager.getHourlyPackagesSnapshot();
        assertEquals(2, hourlyPackages.size());
        assertEquals(2, hourlyPackages.get(0));
        assertEquals(1, hourlyPackages.get(1));
    }
    
    @Test
    public void testPackagesByType() {
        packetManager.addPacket(new Packet(1, System.currentTimeMillis(), 0, Color.RED));
        packetManager.addPacket(new Packet(2, System.currentTimeMillis(), 0, Color.RED));
        packetManager.addPacket(new Packet(3, System.currentTimeMillis(), 0, Color.BLUE));
        
        Map<Color, Integer> packagesByType = packetManager.getPackagesByTypeSnapshot();
        assertEquals(2, packagesByType.get(Color.RED));
        assertEquals(1, packagesByType.get(Color.BLUE));
        assertNull(packagesByType.get(Color.WHITE), "No WHITE packets were added");
    }
    
    @Test
    public void testClear() {
        packetManager.addPacket(new Packet(1, System.currentTimeMillis(), 0, Color.RED));
        packetManager.clear();
        
        assertEquals(0, packetManager.getPacketCount());
        assertEquals(0, packetManager.getPacketsSnapshot().size());
        assertEquals(0, packetManager.getHourlyPackagesSnapshot().size());
        assertTrue(packetManager.getPackagesByTypeSnapshot().isEmpty());
    }
    
    @Test
    public void testGetLastPacketSnapshot() {
        Packet packet1 = new Packet(1, System.currentTimeMillis(), 0, Color.RED);
        Packet packet2 = new Packet(2, System.currentTimeMillis(), 0, Color.BLUE);
        
        packetManager.addPacket(packet1);
        packetManager.addPacket(packet2);
        
        Packet lastPacket = packetManager.getLastPacketSnapshot();
        assertNotNull(lastPacket);
        assertEquals(2, lastPacket.getId());
        assertEquals(Color.BLUE, lastPacket.getPacketColor());
    }
    
    @Test
    public void testGetLastPacketSnapshotEmptyQueue() {
        Packet lastPacket = packetManager.getLastPacketSnapshot();
        assertNull(lastPacket, "Last packet should be null when queue is empty");
    }
    
    @Test
    public void testAverageThroughput() {
        packetManager.addPacket(new Packet(1, System.currentTimeMillis(), 0, Color.RED));
        packetManager.addPacket(new Packet(2, System.currentTimeMillis(), 0, Color.RED));
        packetManager.addPacket(new Packet(3, System.currentTimeMillis(), 3600000, Color.RED)); // 1 hour
        packetManager.addPacket(new Packet(4, System.currentTimeMillis(), 7200000, Color.RED)); // 2 hours
        
        // Expected: (2+1+1)/3 = 1.33
        assertEquals(1.33, packetManager.getAverageThroughput(), 0.01);
    }
    
    @Test
    public void testAverageThroughputNoPackets() {
        assertEquals(0.0, packetManager.getAverageThroughput(), "Average throughput should be 0 when no packets");
    }
    
    @Test
    public void testAverageTimePerPackageDelivered() {
        PacketManager packetManager2 = new PacketManager(2); // Speed multiplier of 2
        long startTime = System.currentTimeMillis();
                
        Packet packet1 = new Packet(1, startTime, 0, Color.RED);
        Packet packet2 = new Packet(2, startTime, 0, Color.BLUE);
        
        packetManager2.addPacket(packet1);
        packetManager2.addPacket(packet2);
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            fail("Interrupted during test");
        }
        
        packet1.setDelivered();
        packet2.setDelivered();
        
        double avgTime = packetManager2.getAverageTimePerPackage();
        assertTrue(avgTime >= 0, "Average time should be non-negative");
        assertTrue(avgTime >= 0.2, "Average time should reflect speed multiplier ");
        assertTrue(avgTime <= 0.25, "Average time should be less than real time due to speed multiplier");
    }
    
    @Test
    public void testAverageTimePerPackageNoDeliveredPackets() {
        Packet packet1 = new Packet(1, System.currentTimeMillis(), 0, Color.RED);
        packetManager.addPacket(packet1);
        
        assertEquals(0.0, packetManager.getAverageTimePerPackage(), 
                "Average time should be 0 when no packets are delivered");
    }
    
    @Test
    public void testPacketEviction() {
        
        for (int i = 0; i < 100; i++) {
            Packet packet = new Packet(i, System.currentTimeMillis(), 0, Color.RED);
            packetManager.addPacket(packet);
            packet.setDelivered();
        }
        
        Packet newPacket = new Packet(100, System.currentTimeMillis(), 0, Color.BLUE);
        packetManager.addPacket(newPacket);
        
        assertEquals(101, packetManager.getPacketCount());
        
        assertEquals(100, packetManager.getPacketsSnapshot().size());
        
        Packet oldest = packetManager.getPacketsSnapshot().peekFirst();
        assertNotEquals(0, oldest.getId(), "First packet should have been evicted");
        assertEquals(1, oldest.getId(), "Second packet should now be first");
        
        Packet newest = packetManager.getLastPacketSnapshot();
        assertEquals(100, newest.getId());
        assertEquals(Color.BLUE, newest.getPacketColor());
    }
}