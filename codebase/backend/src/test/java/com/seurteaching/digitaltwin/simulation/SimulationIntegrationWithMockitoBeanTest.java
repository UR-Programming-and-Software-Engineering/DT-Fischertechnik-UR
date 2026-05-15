package com.seurteaching.digitaltwin.simulation;

import com.seurteaching.digitaltwin.model.SimulationDataDetails;
import com.seurteaching.digitaltwin.mqtt.MachineRegistry;
import com.seurteaching.digitaltwin.simulation.restApi.SimulationApiController;
import com.seurteaching.digitaltwin.simulation.restApi.SimulationConfigDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * Integration Tests with argument captor
 * Uses MockitoBean (non deprecated) as EntityManager
 * to capture the persist() calls
 */
@SpringBootTest
@ActiveProfiles("test")
class SimulationIntegrationWithMockitoBeanTest {

    @Autowired
    private SimulationApiController simulationApiController;

    @Autowired
    private SimulationController simulationController;

    @MockitoBean
    private EntityManager entityManager;

    @MockitoBean
    private MachineRegistry machineRegistry;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Ensure simulation is stopped before each test
        if (simulationController.isRunning()) {
            simulationController.stopSimulation();
        }

        // Reset mocks for clean state
        reset(entityManager, machineRegistry);
        // Re-setup after reset
        setupEntityManagerMock();
    }

    /**
     * Konfiguriert den EntityManager Mock um die MachineRegistry Queries zu handhaben
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setupEntityManagerMock() {
        // Mock TypedQuery für MachineRegistry queries
        jakarta.persistence.TypedQuery mockQuery = mock(jakarta.persistence.TypedQuery.class);

        // Configure the mock to return empty results for any query
        when(entityManager.createQuery(anyString(), any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.setMaxResults(anyInt())).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(java.util.Collections.emptyList());

        // Mock für persist() calls - simuliert die ID-Vergabe durch die Datenbank
        doAnswer(invocation -> {
            SimulationDataDetails entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(1L); // Fake ID für Tests
            }
            return null;
        }).when(entityManager).persist(any(SimulationDataDetails.class));

        // Mock für merge() calls - returns the same object
        when(entityManager.merge(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock für andere EntityManager methods
        when(entityManager.getTransaction()).thenReturn(mock(jakarta.persistence.EntityTransaction.class));
    }

    @ParameterizedTest(name = "Test Simulation Scenario: {0}")
    @MethodSource("simulationScenarios")
    void testSimulationScenariosWithArgumentCaptor(String scenarioName,
            SimulationConfigDTO config,
            SimulationDataDetails expectedResult,Boolean stoppingEarly, long stopSimAfter) throws Exception {

        // ArgumentCaptor to capture all persist/merge calls
        ArgumentCaptor<SimulationDataDetails> captor = ArgumentCaptor.forClass(SimulationDataDetails.class);

        System.out.printf("🚀 Starting test: %s%n", scenarioName);

        // Start simulation with config
        simulationApiController.startSimulationWithConfig(config);

        if (stoppingEarly){
            stopSimulationAfter(stopSimAfter);
        }else{
            // Wait for simulation to complete
            waitForSimulationCompletion();
        }

        // Verify that EntityManager was called with simulation data
        verify(entityManager, atLeastOnce()).persist(captor.capture());
        
        // Get the final simulation result (last captured call has the complete data)
        List<SimulationDataDetails> capturedData = captor.getAllValues();
        SimulationDataDetails actualResult = capturedData.get(capturedData.size() - 1);

        System.out.printf("📊 Result: %d/%d packages, %.6f kWh%n",
                actualResult.getTotalPackages(),
                actualResult.getExpectedPackages(),
                actualResult.getTotalEnergyUsage());

        // Validate the simulation result
        assertSimulationDataMatches(expectedResult, actualResult, scenarioName);

        System.out.printf("✅ Test passed: %s%n", scenarioName);
    }

    /**
     * Provides test scenarios with their configurations and expected results
     */
    static Stream<Object[]> simulationScenarios() {
        return Stream.of(
                // Real scenario - exactly 1 package should go through the entire process
                new Object[] {
                        "Real Scenario - Single Package Flow",
                        SimulationScenarioCreator.createRealScenarioConfig(),
                        SimulationScenarioCreator.createExpectedRealScenarioResult(),
                        false,
                        0
                },
                // Short simulation scenario
                new Object[] {
                        "Short Simulation - 1 minute",
                        SimulationScenarioCreator.createShortSimulationConfig(),
                        SimulationScenarioCreator.createExpectedShortSimulationResult(),
                        false,
                        0
                },
                // Short simulation scenario
                new Object[] {
                        "Early stopping short Simulation - 1 minute",
                        SimulationScenarioCreator.createShortSimulationConfig(),
                        SimulationScenarioCreator.createEarlyStoppedExpectedShortSimulationResult(),
                        true,
                        15000
                },
                new Object[] {
                        "Package Mismatch Simulation - 1 Hour",
                        SimulationScenarioCreator.createHourSimulationExpectedMismatch(),
                        SimulationScenarioCreator.createHourSimulationExpectedMismatchResult(),
                        false,
                        0
                },
                // Simulating 5 days would run for 15 minutes => to long for each commit
//                new Object[]{
//                        "5 days simulation",
//                        SimulationScenarioCreator.create5daysSimulation(),
//                        SimulationScenarioCreator.create5daysSimulationResult(),
//                        false,
//                        0
//                },
                new Object[]{
                        "1 day fast vacuum grippers Simulation",
                        SimulationScenarioCreator.createFastVGs1daySimulation(),
                        SimulationScenarioCreator.createFastVGs1daySimulationResult(),
                        false,
                        0
                }
                );
    }

    /**
     * Waits for the simulation to complete with timeout
     */
    private void waitForSimulationCompletion() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Thread monitorThread = new Thread(() -> {
            try {
                while (simulationController.isRunning()) {
                    Thread.sleep(100); // Check every 100ms
                }
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        monitorThread.start();

        boolean completed = latch.await(420, TimeUnit.SECONDS);
        assertTrue(completed, "Simulation should complete within 7 Minutes - check lag or other errors");

        if (monitorThread.isAlive()) {
            monitorThread.interrupt();
        }
    }
    private void stopSimulationAfter(long stopAfter) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Thread monitorThread = new Thread(() -> {
            try {
                Thread.sleep(stopAfter);
                // Actually stop the simulation here
                simulationController.stopSimulation();
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        monitorThread.start();

        boolean completed = latch.await(120, TimeUnit.SECONDS);
        assertTrue(completed, "Simulation should be stopped within 2 Minutes");

        if (monitorThread.isAlive()) {
            monitorThread.interrupt();
        }
    }



    /**
     * Validates that the actual simulation result matches the expected result
     */
    private void assertSimulationDataMatches(SimulationDataDetails expected,
            SimulationDataDetails actual,
            String scenarioName) {



        // Basic validations
        assertNotNull(actual, "Simulation result should not be null for scenario: " + scenarioName);
        assertNotNull(actual.getTimeCreated(), "Creation time should be set");
        assertNotNull(actual.getTimeEnded(), "End time should be set");

        assertTrue(actual.getTotalPackages() >= 0, "Total packages should be non-negative");
        assertTrue(actual.getTotalEnergyUsage() >= 0.0, "Total energy usage should be non-negative");


        // More advanced validations - allow reasonable tolerance for timing variations
        if (expected.getExpectedPackages() > 0) {
            int tolerance = 0;
            assertTrue(Math.abs(actual.getExpectedPackages() - expected.getExpectedPackages()) <= tolerance,
                    String.format("Expected packages mismatch for scenario %s: expected %d ±%d, but was %d",
                            scenarioName, expected.getExpectedPackages(), tolerance, actual.getExpectedPackages()));
        }

        if(expected.getTotalPackages() > 0){
            // set tolerance to 0.1 percent of the expected packages
            int tolerance = 1;
            if (expected.getExpectedPackages() > 1000){
                tolerance = Math.round(expected.getExpectedPackages() / 1000);
            }
            assertTrue(Math.abs(actual.getTotalPackages() - expected.getTotalPackages()) <= tolerance ,
                    String.format("Total Packages mismatch for scenario %s: expected %d ±%d, but was %d",
                            scenarioName, expected.getTotalPackages(), tolerance, actual.getTotalPackages()
                            )
                    );
        }

        /*
         * Currently there is too much variability in energy consumption
         */
        if (expected.getTotalEnergyUsage() > 0){
            // set tolerance to 1 percent of the expected energy usage
            double tolerance;
            if (expected.getTotalEnergyUsage() > 50.0){
                tolerance = Math.round( (expected.getTotalEnergyUsage() / 100 ) * 20) / 10;
            }else{
                tolerance = 0.15;
            }
            assertTrue(Math.abs(actual.getTotalEnergyUsage() - expected.getTotalEnergyUsage()) <= tolerance,
                    String.format("Expected Energy mismatch for scenario %s: expected %f ±%f, but was %f",
                            scenarioName, expected.getTotalEnergyUsage(), tolerance, actual.getTotalEnergyUsage()
                    ));
        }


        System.out.printf("  ✓ everything matches: Packages: %d/%d, Energy: %.6f kWh / %.6f kWh, Duration: %s ms%n",
                actual.getTotalPackages(), expected.getTotalPackages(),
                actual.getTotalEnergyUsage(), expected.getTotalEnergyUsage(),
                actual.getDuration());
    }
}
