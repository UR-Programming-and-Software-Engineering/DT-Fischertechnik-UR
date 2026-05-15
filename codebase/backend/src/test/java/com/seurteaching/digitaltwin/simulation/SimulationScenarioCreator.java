package com.seurteaching.digitaltwin.simulation;

import com.seurteaching.digitaltwin.model.SimulationDataDetails;
import com.seurteaching.digitaltwin.model.SimulationParameters;
import com.seurteaching.digitaltwin.simulation.restApi.SimulationConfigDTO;

import java.util.HashMap;
import java.util.Map;

public class SimulationScenarioCreator {

    /*
    * Creates config for the scenario provided in the simple_process.mp4
    * */
    public static SimulationConfigDTO createRealScenarioConfig() {
        SimulationConfigDTO config = new SimulationConfigDTO();
        SimulationParameters params = new SimulationParameters();

        params.setSimulationDays(0);
        params.setSimulationHours(0);
        params.setSimulationMinutes(5);
        params.setSpeedMultiplier(20.0);
        params.setPackageGenerationInterval(5);

        Map<String, Double> powerMultipliers = new HashMap<>();
        powerMultipliers.put("SortingLine01", 1.0);
        powerMultipliers.put("ConveyorBelt01", 1.0);
        powerMultipliers.put("VacuumGripper01", 1.0);
        powerMultipliers.put("VacuumGripper02", 1.0);
        powerMultipliers.put("MultiProcessing01", 1.0);
        powerMultipliers.put("HighBay01", 1.0);
        params.setMachinePowerMultipliers(powerMultipliers);

        config.setParameters(params);
        config.setTimestamp(String.valueOf(System.currentTimeMillis()));

        return config;
    }

    public static SimulationDataDetails createExpectedRealScenarioResult() {
        SimulationDataDetails expected = new SimulationDataDetails();
        expected.setName("Real Scenario Single Package");
        expected.setExpectedPackages(1); // Exactly 1 package in 5 minutes with 5s interval
        expected.setTotalPackages(1); // Should complete successfully
        expected.setTotalEnergyUsage(3.539);

        return expected;
    }



    /**
     * Creates configuration for a short 10-minute simulation with high speed
     */
    public static SimulationConfigDTO createShortSimulationConfig() {
        SimulationConfigDTO config = new SimulationConfigDTO();
        SimulationParameters params = new SimulationParameters();

        params.setSimulationDays(0);
        params.setSimulationHours(0);
        params.setSimulationMinutes(10);
        params.setSpeedMultiplier(20.0);
        params.setPackageGenerationInterval(3);

        // Standard machine power multipliers
        Map<String, Double> powerMultipliers = new HashMap<>();
        powerMultipliers.put("SortingLine01", 1.0);
        powerMultipliers.put("VacuumGripper02", 1.0);
        powerMultipliers.put("ConveyorBelt01", 1.0);
        powerMultipliers.put("VacuumGripper01", 1.0);
        powerMultipliers.put("MultiProcessing01", 1.0);
        powerMultipliers.put("HighBay01", 1.0);
        params.setMachinePowerMultipliers(powerMultipliers);

        config.setParameters(params);
        config.setTimestamp(String.valueOf(System.currentTimeMillis()));

        return config;
    }

    public static SimulationDataDetails createExpectedShortSimulationResult() {
        SimulationDataDetails expected = new SimulationDataDetails();
        expected.setName("Short Simulation Test");
        expected.setExpectedPackages(4);
        expected.setTotalPackages(4);
        expected.setTotalEnergyUsage(34.345);

        return expected;
    }

    public static SimulationDataDetails createEarlyStoppedExpectedShortSimulationResult() {
        SimulationDataDetails expected = new SimulationDataDetails();
        expected.setName("Short Simulation Test");
        expected.setExpectedPackages(4);
        expected.setTotalPackages(2);
        expected.setTotalEnergyUsage(14.240);

        return expected;
    }



    /**
     * Creates configuration for a short 1-hour simulation with high speed
     */
    public static SimulationConfigDTO createHourSimulationExpectedMismatch() {
        SimulationConfigDTO config = new SimulationConfigDTO();
        SimulationParameters params = new SimulationParameters();

        params.setSimulationDays(0);
        params.setSimulationHours(1);
        params.setSimulationMinutes(0);
        params.setSpeedMultiplier(60.0);
        params.setPackageGenerationInterval(1.5);

        // Standard machine power multipliers
        Map<String, Double> powerMultipliers = new HashMap<>();
        powerMultipliers.put("SortingLine01", 1.0);
        powerMultipliers.put("VacuumGripper02", 1.0);
        powerMultipliers.put("ConveyorBelt01", 1.0);
        powerMultipliers.put("VacuumGripper01", 1.0);
        powerMultipliers.put("MultiProcessing01", 1.0);
        powerMultipliers.put("HighBay01", 1.0);
        params.setMachinePowerMultipliers(powerMultipliers);

        config.setParameters(params);
        config.setTimestamp(String.valueOf(System.currentTimeMillis()));

        return config;
    }

    public static SimulationDataDetails createHourSimulationExpectedMismatchResult() {
        SimulationDataDetails expected = new SimulationDataDetails();
        expected.setName("Short Simulation Test");
        expected.setExpectedPackages(40);
        expected.setTotalPackages(36);
        expected.setTotalEnergyUsage(286.269);

        return expected;
    }


    /**
     * Creates configuration for a long 5 days simulation with high speed
     * Test takes ~14 min which is too long for regular tests
     */
    /*
    public static SimulationConfigDTO create5daysSimulation() {
        SimulationConfigDTO config = new SimulationConfigDTO();
        SimulationParameters params = new SimulationParameters();

        params.setSimulationDays(5);
        params.setSimulationHours(0);
        params.setSimulationMinutes(0);
        params.setSpeedMultiplier(500.0);
        params.setPackageGenerationInterval(2.5);

        // Standard machine power multipliers
        Map<String, Double> powerMultipliers = new HashMap<>();
        powerMultipliers.put("SortingLine01", 1.0);
        powerMultipliers.put("VacuumGripper02", 1.0);
        powerMultipliers.put("ConveyorBelt01", 1.0);
        powerMultipliers.put("VacuumGripper01", 1.0);
        powerMultipliers.put("MultiProcessing01", 1.0);
        powerMultipliers.put("HighBay01", 1.0);
        params.setMachinePowerMultipliers(powerMultipliers);

        config.setParameters(params);
        config.setTimestamp(String.valueOf(System.currentTimeMillis()));

        return config;
    }
    // Todo simulate and log results
    public static SimulationDataDetails create5daysSimulationResult() {
        SimulationDataDetails expected = new SimulationDataDetails();
        expected.setName("5 days simulation Result");
        expected.setExpectedPackages(40);
        expected.setTotalPackages(35);
        expected.setTotalEnergyUsage(0.007252);

        return expected;
    }
    */

    /**
     * Creates configuration for a 1 day simulation with faster Vacuum Grippers
     */

    public static SimulationConfigDTO createFastVGs1daySimulation() {
        SimulationConfigDTO config = new SimulationConfigDTO();
        SimulationParameters params = new SimulationParameters();

        params.setSimulationDays(1);
        params.setSimulationHours(0);
        params.setSimulationMinutes(0);
        params.setSpeedMultiplier(250.0);
        params.setPackageGenerationInterval(2.5);

        // Standard machine power multipliers
        Map<String, Double> powerMultipliers = new HashMap<>();
        powerMultipliers.put("SortingLine01", 1.0);
        powerMultipliers.put("VacuumGripper02", 2.0);
        powerMultipliers.put("ConveyorBelt01", 1.0);
        powerMultipliers.put("VacuumGripper01", 2.0);
        powerMultipliers.put("MultiProcessing01", 1.0);
        powerMultipliers.put("HighBay01", 1.0);
        params.setMachinePowerMultipliers(powerMultipliers);

        config.setParameters(params);
        config.setTimestamp(String.valueOf(System.currentTimeMillis()));

        return config;
    }
    public static SimulationDataDetails createFastVGs1daySimulationResult() {
        SimulationDataDetails expected = new SimulationDataDetails();
        expected.setName("1 day fast vacuum grippers simulation Result");
        expected.setExpectedPackages(576);
        expected.setTotalPackages(573);
        expected.setTotalEnergyUsage(5666.03);

        return expected;
    }

}
