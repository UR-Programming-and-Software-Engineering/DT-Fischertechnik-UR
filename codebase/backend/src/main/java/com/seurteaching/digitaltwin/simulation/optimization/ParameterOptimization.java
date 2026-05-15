package com.seurteaching.digitaltwin.simulation.optimization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.seurteaching.digitaltwin.model.SimulationParameters;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;


@Component
public class ParameterOptimization {

    private static final long K = 5;
    private static final int PREDICTION_RANDOM_SAMPLES = 1000;
    private static final double SPEEDUP_LOWER_BOUND = 0.5;
    private static final double SPEEDUP_UPPER_BOUND = 2.0;
    private static final double PACKET_LOWER_BOUND = 1.0;
    private static final double PACKET_UPPER_BOUND = 5.0;

    private static final Logger logger = LoggerFactory.getLogger(ParameterOptimization.class);
    
    private static final String[] MACHINE_NAMES = {
        "SortingLine01",
        "ConveyorBelt01", 
        "VacuumGripper01",
        "VacuumGripper02",
        "MultiProcessing01",
        "HighBay01"
    };

    private static final Random random = new Random();
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Transactional(readOnly = true)
    public PredictionResult predictMetricsWithKNN(
            Map<String, Double> machinePowerMultipliers,
            double packageGenerationInterval) {

        float[] queryVector = createParameterVector(machinePowerMultipliers, packageGenerationInterval);
        //logger.info("Created query vector for similarity search: {}", Arrays.toString(queryVector));
        
        String sql =
            "SELECT sbd.id, sdd.total_packages, sdd.total_energy_usage, sp.simulation_days, sp.simulation_hours, sp.simulation_minutes " +
            "FROM simulation_base_data sbd " +
            "INNER JOIN simulation_data_details sdd ON sbd.id = sdd.id " +
            "INNER JOIN simulation_parameters sp ON sbd.parameters_id = sp.id " +
            "WHERE sdd.parameters_vec IS NOT NULL " +
            "ORDER BY sdd.parameters_vec <-> ?::vector " +
            "LIMIT ?";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, queryVector);
        query.setParameter(2, K);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        if (results.isEmpty()) {
            logger.warn("No similar simulation data found in the database");
            return new PredictionResult(0, 0.0, 0.0);
        }
        
        int totalPackagesSum = 0;
        double totalEnergyUsageSum = 0.0;
        
        for (Object[] row : results) {
            // Long id = ((Number) row[0]).longValue();
            int totalPackages = ((Number) row[1]).intValue();
            double totalEnergyUsage = ((Number) row[2]).doubleValue();
            int days = ((Number) row[3]).intValue();
            int hours = ((Number) row[4]).intValue();
            int minutes = ((Number) row[5]).intValue();

            float duration = days * 24 + hours + minutes / 60; // convert to hours
            
            totalPackagesSum += totalPackages / duration;
            totalEnergyUsageSum += totalEnergyUsage / duration;
            
            // logger.debug("Neighbor ID: {}, packages per hour: {}, energyUsage per hour: {}", 
            //          totalPackages / duration, totalEnergyUsage / duration);
        }
    
        int avgTotalPackages = totalPackagesSum / results.size();
        double avgTotalEnergyUsage = totalEnergyUsageSum / results.size();
        
        double avgEnergyPerPackage = avgTotalPackages > 0 ? 
                avgTotalEnergyUsage / avgTotalPackages : 0.0;
                
        return new PredictionResult(avgTotalPackages, avgEnergyPerPackage, avgTotalEnergyUsage);
    }
    
    private float[] createParameterVector(
            Map<String, Double> machinePowerMultipliers,
            double packageGenerationInterval) {
        
        float[] vector = new float[7];
        
        vector[0] = (float) packageGenerationInterval;
        
        for (int i = 0; i < MACHINE_NAMES.length; i++) {
            String machineName = MACHINE_NAMES[i];
            double multiplier = machinePowerMultipliers.getOrDefault(machineName, 1.0);
            vector[i + 1] = (float) multiplier;
        }
        
        return vector;
    }

    public OptimizationResponseDTO optimizeParameters(OptimizationRequestDTO request) {

        SimulationParameters base = request.getParameters();

        boolean fixInterval = request.isFixPackageGenerationInterval();
        List<String> fixedMachines = request.getFixMachinePowerMultipliers();

        OptimizationResponseDTO response = new OptimizationResponseDTO();

        SimulationParameters bestParamsEnergy = null;
        PredictionResult bestEnergy = null;

        SimulationParameters bestParamsPackages = null;
        PredictionResult bestPackages = null;

        SimulationParameters bestParamsCombined = null;
        PredictionResult bestCombined = null;
        
        // perform a simple random search
        for (int i = 0; i < PREDICTION_RANDOM_SAMPLES; i++) {                                                                                                                                                                                                                                                                                                                                                                              
            SimulationParameters cand = new SimulationParameters();
            cand.setSimulationDays(base.getSimulationDays());
            cand.setSimulationHours(base.getSimulationHours());
            cand.setSimulationMinutes(base.getSimulationMinutes());
            cand.setSpeedMultiplier(base.getSpeedMultiplier());
            cand.setPackageGenerationInterval(fixInterval ? base.getPackageGenerationInterval() : roundWithOneDecimal(
                getRandomDoubleInRange(PACKET_LOWER_BOUND, PACKET_UPPER_BOUND)));

            Map<String, Double> multipliers = new HashMap<>();
            for (Map.Entry<String, Double> e : base.getMachinePowerMultipliers().entrySet()) {
                String id = e.getKey();
                multipliers.put(id, fixedMachines != null && fixedMachines.contains(id)
                    ? e.getValue()
                    : roundWithOneDecimal(getRandomDoubleInRange(SPEEDUP_LOWER_BOUND, SPEEDUP_UPPER_BOUND)));
            }
            cand.setMachinePowerMultipliers(multipliers);

            PredictionResult pr = predictMetricsWithKNN(multipliers, cand.getPackageGenerationInterval());
            // logger.info("Candidate parameters: packageGenerationInterval={}, multipliers={} => prediction={}", 
            //    cand.getPackageGenerationInterval(), multipliers, pr);

            if (bestEnergy == null || pr.getEnergyPerPackage() < bestEnergy.getEnergyPerPackage()) {
                bestEnergy = pr;
                bestParamsEnergy = cand;
            }

            if (bestPackages == null || pr.getTotalPackages() > bestPackages.getTotalPackages()) {
                bestPackages = pr;
                bestParamsPackages = cand;
            }

            double score = pr.getEnergyPerPackage() > 0
                ? pr.getTotalPackages() / pr.getEnergyPerPackage()
                : Double.POSITIVE_INFINITY;
            double bestScore = bestCombined != null && bestCombined.getEnergyPerPackage() > 0
                ? bestCombined.getTotalPackages() / bestCombined.getEnergyPerPackage()
                : Double.NEGATIVE_INFINITY;
            
            if (bestCombined == null || score > bestScore) {
                bestCombined = pr;
                bestParamsCombined = cand;
            }
        }

        response.setEnergyPerHourPrediction(bestParamsEnergy);
        response.setEnergyPerPackagePredicition(bestParamsCombined);
        response.setPackagesPerHourPrediction(bestParamsPackages);

        return response;
    }
    
    private double roundWithOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private double getRandomDoubleInRange(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
}

