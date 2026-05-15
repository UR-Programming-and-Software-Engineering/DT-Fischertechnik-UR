package com.seurteaching.digitaltwin.simulation.restApi;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seurteaching.digitaltwin.machines.Color;
import com.seurteaching.digitaltwin.simulation.SimulationController;
import com.seurteaching.digitaltwin.simulation.util.PacketManager;

@RestController
@RequestMapping("/api/tracking")
public class TrackingApiController {

    @Autowired
    private SimulationController simulationController;

    @GetMapping("/package-info")
    public PackageTrackingInformation getPackageTrackingInformation() {
        PackageTrackingInformation info = new PackageTrackingInformation();

        PacketManager pm = simulationController.getPacketManager();
        Map<Color, Integer> raw = (pm != null) ? pm.getPackagesByTypeSnapshot() : Map.of();

        Map<String, Integer> packagesByType = new HashMap<>();
        for (Map.Entry<Color, Integer> e : raw.entrySet()) {
            packagesByType.put(e.getKey().name(), e.getValue());
        }

        double avgThroughput = (pm != null) ? pm.getAverageThroughput() : 0.0;
        double avgTime = (pm != null) ? pm.getAverageTimePerPackage() : 0.0;

        info.setPackagesByType(packagesByType);
        info.setAverageTimePerPackage(avgTime);
        info.setTroughputPerHour(avgThroughput);

        return info;
    }
}
