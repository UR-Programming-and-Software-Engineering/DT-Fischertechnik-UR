package com.seurteaching.digitaltwin.simulation.util;

import java.util.List;
import java.util.Map;

import com.seurteaching.digitaltwin.model.SimulationParameters;
import com.seurteaching.digitaltwin.simulation.SimulationBaseMachine;

/*
 * Configuration class for the simulation
 * Immutable after creation
 */
public final class SimulationConfig {

    private final int simulationDays;
    private final int simulationHours;
    private final int simulationMinutes;
    private final double speedMultiplier;
    private final double packageGenerationInterval;
    private final Map<String, Double> machinePowerMultipliers;
    private final SimulationParameters simulationParameters;

    private final List<SimulationBaseMachine<?, ?>> machines;
    private final List<PacketPass> edges;
    private final String[] packetPath;

    public SimulationConfig(
            int simulationDays,
            int simulationHours,
            int simulationMinutes,
            double speedMultiplier,
            double packageGenerationInterval,
            Map<String, Double> machinePowerMultipliers,
            SimulationParameters simulationParameters,
            List<SimulationBaseMachine<?, ?>> machines,
            List<PacketPass> edges,
            String[] packetPath) {

        this.simulationDays = simulationDays;
        this.simulationHours = simulationHours;
        this.simulationMinutes = simulationMinutes;
        this.speedMultiplier = speedMultiplier;
        this.packageGenerationInterval = packageGenerationInterval;
        this.machinePowerMultipliers = (machinePowerMultipliers == null) ? Map.of() : Map.copyOf(machinePowerMultipliers);
        this.simulationParameters = simulationParameters;
        this.machines = (machines == null) ? List.of() : List.copyOf(machines);
        this.edges = (edges == null) ? List.of() : List.copyOf(edges);
        this.packetPath = (packetPath == null) ? null : packetPath.clone();
    }

    public int getSimulationDays() { return simulationDays; }
    public int getSimulationHours() { return simulationHours; }
    public int getSimulationMinutes() { return simulationMinutes; }
    public double getSpeedMultiplier() { return speedMultiplier; }
    public double getPackageGenerationInterval() { return packageGenerationInterval; }
    public Map<String, Double> getMachinePowerMultipliers() { return machinePowerMultipliers; }
    public SimulationParameters getSimulationParameters() { return simulationParameters; }
    public List<SimulationBaseMachine<?, ?>> getMachines() { return machines; }
    public List<PacketPass> getEdges() { return edges; }
    public String[] getPacketPath() { return packetPath == null ? null : packetPath.clone(); }

    public Double getMachinePowerMultiplier(String machineName) {
        return machinePowerMultipliers.getOrDefault(machineName, 1.0);
    }

    public long getPacketCreationIntervalMs() {
        return (long)((packageGenerationInterval * 60000) / speedMultiplier);
    }

    public double getMachineDelay(String machineName) {
        return getMachinePowerMultiplier(machineName) * getSpeedMultiplier();
    }
    
    public long getSimulationDuration() {
        long totalMinutes = getSimulatedTimeInMinutes();
        long totalInMS = totalMinutes * 60_000L;
        long simulationDuration = Math.round(totalInMS / this.speedMultiplier);
        return simulationDuration;
    }
    public long getSimulatedTimeInMinutes(){
        long totalMinutes =
                (long) this.simulationDays * 24L  * 60L
                + (long) this.simulationHours * 60L
                + (long) this.simulationMinutes;
        return totalMinutes;
    }

    public int getExpectedPackages() {
        double simulatedMinutes = getSimulatedTimeInMinutes();
        return (int) Math.ceil(simulatedMinutes / getPackageGenerationInterval());
    }

    public int getPackageThroughputEstimated() {
        double simulatedMinutes = getSimulatedTimeInMinutes();
        double simulatedHours = simulatedMinutes / 60;
        if (simulatedHours > 0) {
            int estimatedPackages = getExpectedPackages();
            return (int) (estimatedPackages / simulatedHours);
        }
        return 0;
    }

    public int getTimePerPackageEstimated() {
        double simulatedMinutes = getSimulatedTimeInMinutes();
        double simulatedHours = simulatedMinutes / 60;
        if (simulatedHours > 0) {
            int estimatedPackages = getExpectedPackages();
            return (int) (simulatedHours / Math.max(estimatedPackages, 1) * 60);
        }
        return 0;
    }
}