package com.seurteaching.digitaltwin.simulation.restApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.seurteaching.digitaltwin.model.SimulationDataDetails;
import com.seurteaching.digitaltwin.model.SimulationParameters;
import com.seurteaching.digitaltwin.model.SimulationBaseData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.seurteaching.digitaltwin.simulation.SimulationController;
import com.seurteaching.digitaltwin.simulation.util.SimulationConfig;
import com.seurteaching.digitaltwin.simulation.util.SimulationConfigFactory;

@RestController
@RequestMapping("/api/simulation")
public class SimulationApiController {

        @Autowired
        private SimulationController simulationController;
        @Autowired
        private EntityManager entityManager;

        @PostMapping("/start-with-config")
        public ResponseEntity<String> startSimulationWithConfig(@RequestBody SimulationConfigDTO config) {

                try {
                        if (simulationController.isRunning()) {
                                return ResponseEntity.badRequest().body("Simulation is already running");
                        }
                        SimulationParameters params = config.getParameters();
                        SimulationConfig simConfig = SimulationConfigFactory.createConfigWithParams(params);
                        simulationController.startSimulation(simConfig);
                        return ResponseEntity.ok("Simulation started with configuration: " + config.getParameters());
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body("Failed to start simulation: " + e.getMessage());
                }
        }

        @PostMapping("/stop-simulation")
        public String stopSimulation() {
                simulationController.stopSimulation();
                return "Simulation stopped";
        }

        @GetMapping("/get-running-simulation")
        public SimulationDataDetails getRunningSimulation() {
                SimulationDataDetails d = this.simulationController.getRunningSimulation();
                if(d == null){
                        throw new IllegalStateException("No running simulation found");
                }
                return d;
        }

        @GetMapping("/status")
        public String getStatus() {
                return simulationController.getMachines().toString();
        }

        @GetMapping("/machines")
        public String[] getMachines() {
                return simulationController.getPackePath();
        }

        @GetMapping("/machine-steps")
        public List<String> getMachineSteps() {
                List<String> MACHINE_STEPS = Arrays.asList(
                                "SortingLine01",
                                "VacuumGripper02",
                                "ConveyorBelt01",
                                "VacuumGripper01",
                                "MultiProcessing01",
                                "VacuumGripper01",
                                "Highbay01");

                return MACHINE_STEPS;
        }

        @GetMapping("/get-last-base")
        public ResponseEntity<Map<String, List<SimulationBaseData>>> getLastBaseSimulations(
                        @RequestParam(defaultValue = "5") int limit) {
                try {
                        List<SimulationBaseData> simulations = entityManager
                                        .createQuery("SELECT s FROM SimulationBaseData s ORDER BY s.timeCreated DESC",
                                                        SimulationBaseData.class)
                                        .setMaxResults(limit)
                                        .getResultList();

                        Map<String, List<SimulationBaseData>> response = new HashMap<>();
                        response.put("simulations", simulations);

                        return ResponseEntity.ok(response);

                } catch (Exception e) {
                        System.err.println("Error fetching last base simulations: " + e.getMessage());
                        e.printStackTrace();

                        Map<String, List<SimulationBaseData>> errorResponse = new HashMap<>();
                        errorResponse.put("simulations", new ArrayList<>());
                        return ResponseEntity.ok(errorResponse);
                }
        }

        @GetMapping("/get-last-detailed")
        public ResponseEntity<Map<String, List<SimulationDataDetails>>> getLastDetailedSimulations(
                        @RequestParam(defaultValue = "3") int limit) {
                try {
                        List<SimulationDataDetails> simulations = entityManager
                                        .createQuery("SELECT d FROM SimulationDataDetails d ORDER BY d.timeCreated DESC",
                                                        SimulationDataDetails.class)
                                        .setMaxResults(limit)
                                        .getResultList();

                        Map<String, List<SimulationDataDetails>> response = new HashMap<>();
                        response.put("simulations", simulations);

                        return ResponseEntity.ok(response);

                } catch (Exception e) {
                        System.err.println("Error fetching last detailed simulations: " + e.getMessage());
                        e.printStackTrace();

                        Map<String, List<SimulationDataDetails>> errorResponse = new HashMap<>();
                        errorResponse.put("simulations", new ArrayList<>());
                        return ResponseEntity.ok(errorResponse);
                }
        }
        @GetMapping("/get-simulation/{id}")
        public ResponseEntity<SimulationDataDetails> getSimByID(@PathVariable long id){
                try {
                        SimulationDataDetails simulation = entityManager
                                .createQuery("Select d FROM SimulationDataDetails d WHERE d.id = :id", SimulationDataDetails.class)
                                .setParameter("id", id)
                                .getSingleResult();
                        return ResponseEntity.ok(simulation);
                } catch (NoResultException e){
                        return ResponseEntity.notFound().build();
                } catch (Exception e) {
                        System.err.println("Error fetching Simulation with id: " + id + ": " +e.getMessage());
                        e.printStackTrace();
                        return ResponseEntity.internalServerError().build();
                }
        }
}