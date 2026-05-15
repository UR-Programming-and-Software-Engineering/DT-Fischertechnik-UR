package com.seurteaching.digitaltwin.simulation.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seurteaching.digitaltwin.model.SimulationParameters;
import com.seurteaching.digitaltwin.simulation.SimulationBaseMachine;
import com.seurteaching.digitaltwin.simulation.simulation01.ConveyorBelt01;
import com.seurteaching.digitaltwin.simulation.simulation01.HighBay01;
import com.seurteaching.digitaltwin.simulation.simulation01.MultiProcessing01;
import com.seurteaching.digitaltwin.simulation.simulation01.SortingLine01;
import com.seurteaching.digitaltwin.simulation.simulation01.VacuumGripper01;
import com.seurteaching.digitaltwin.simulation.simulation01.VacuumGripper02;

public class SimulationConfigFactory {

    public static SimulationConfig createDefaultConfig() {
        return createConfigWithParams(new SimulationParameters());
    }

    public static SimulationConfig createConfigWithParams(SimulationParameters params) {

        List<SimulationBaseMachine<?,?>> machines = new ArrayList<>();
        List<PacketPass> edges = new ArrayList<>();

        // Create machines
        double baseSimSpeed = 1.0;
        if (params != null) baseSimSpeed = params.getSpeedMultiplier();

        Map<String, Double> machineSpeedMultipliers = (params != null) ? params.getMachinePowerMultipliers() : new HashMap<String, Double>();

        double sortingLineSpeed = machineSpeedMultipliers.getOrDefault("SortingLine01", 1.0);
        double vacuumGripper02Speed = machineSpeedMultipliers.getOrDefault("VacuumGripper02", 1.0);
        double conveyorBeltSpeed = machineSpeedMultipliers.getOrDefault("ConveyorBelt01", 1.0);
        double vacuumGripper01Speed = machineSpeedMultipliers.getOrDefault("VacuumGripper01", 1.0);
        double multiProcessing01Speed = machineSpeedMultipliers.getOrDefault("MultiProcessing01", 1.0);
        double highBay01Speed = machineSpeedMultipliers.getOrDefault("HighBay01", 1.0) ;

        SortingLine01 sortingLine = new SortingLine01(baseSimSpeed, sortingLineSpeed);
        VacuumGripper02 vacuumGripper = new VacuumGripper02(baseSimSpeed, vacuumGripper02Speed);
        ConveyorBelt01 conveyorBelt = new ConveyorBelt01(baseSimSpeed, conveyorBeltSpeed);
        VacuumGripper01 vacuumGripper01 = new VacuumGripper01(baseSimSpeed, vacuumGripper01Speed);
        MultiProcessing01 multiProcessing01 = new MultiProcessing01(baseSimSpeed, multiProcessing01Speed);
        HighBay01 highBay01 = new HighBay01(baseSimSpeed, highBay01Speed);

        // Set dependencies
        vacuumGripper.setSortingLine01(sortingLine);
        vacuumGripper.setConveyorBelt01(conveyorBelt);
        conveyorBelt.setVacuumGripper02(vacuumGripper);
        multiProcessing01.setVacuumGripper01(vacuumGripper01);
        highBay01.setVacuumGripper01(vacuumGripper01);
        highBay01.setMultiProcessing01(multiProcessing01);
        vacuumGripper01.setConveyorBelt01(conveyorBelt);
        vacuumGripper01.setHighBay01(highBay01);
        vacuumGripper01.setMultiProcessing01(multiProcessing01);

        // Add machines to list
        machines.add(sortingLine);
        machines.add(vacuumGripper);
        machines.add(conveyorBelt);
        machines.add(vacuumGripper01);
        machines.add(multiProcessing01);
        machines.add(highBay01);

        // Add edges
        edges.add(new PacketPass(
                        vacuumGripper,
                        conveyorBelt,
                        VacuumGripper02.VacuumGripper02State.P3,
                        ConveyorBelt01.ConveyorBelt01State.WaitingIncoming));

        edges.add(new PacketPass(
                        sortingLine,
                        vacuumGripper,
                        SortingLine01.SortingLine01State.WaitingOutgoingBlue,
                        VacuumGripper02.VacuumGripper02State.P2));

        edges.add(new PacketPass(
                        sortingLine,
                        vacuumGripper,
                        SortingLine01.SortingLine01State.WaitingOutgoingRed,
                        VacuumGripper02.VacuumGripper02State.P2));

        edges.add(new PacketPass(
                        sortingLine,
                        vacuumGripper,
                        SortingLine01.SortingLine01State.WaitingOutgoingWhite,
                        VacuumGripper02.VacuumGripper02State.P2));

        edges.add(new PacketPass(
                        conveyorBelt,
                        vacuumGripper01,
                        ConveyorBelt01.ConveyorBelt01State.WaitingOutgoing,
                        VacuumGripper01.VacuumGripper01State.Conveyor));

        edges.add(new PacketPass(
                        vacuumGripper01,
                        multiProcessing01,
                        VacuumGripper01.VacuumGripper01State.MultiIncoming,
                        MultiProcessing01.MultiProcessing01State.WaitingIncoming));

        edges.add(new PacketPass(
                        multiProcessing01,
                        vacuumGripper01,
                        MultiProcessing01.MultiProcessing01State.WaitingOutgoing,
                        VacuumGripper01.VacuumGripper01State.MultiOutgoing));

        edges.add(new PacketPass(
                        vacuumGripper01,
                        highBay01,
                        VacuumGripper01.VacuumGripper01State.Highbay,
                        HighBay01.HighBay01State.WaitingForGripperToLoad));

        String[] packetPath = {
                        "SortingLine01",
                        "VacuumGripper02",
                        "ConveyorBelt01",
                        "VacuumGripper01",
                        "MultiProcessing01",
                        "VacuumGripper01",
                        "HighBay01"
        };

        SimulationConfig config = new SimulationConfig(
                        params.getSimulationDays(),
                        params.getSimulationHours(),
                        params.getSimulationMinutes(),
                        params.getSpeedMultiplier(),
                        params.getPackageGenerationInterval(),
                        params.getMachinePowerMultipliers(),
                        params,
                        machines,
                        edges,
                        packetPath
        );

        return config;
    }

}
