package com.seurteaching.digitaltwin.mqtt;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.seurteaching.digitaltwin.machines.*;

@Component
public class MachineRegistry {

    private static final Logger logger = LoggerFactory.getLogger(MachineRegistry.class);

    private final EntityManager em;
    private Map<String, BaseMachine> machines = new HashMap<>();
    
    // Configured ObjectMapper for JSON serialization
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public MachineRegistry(EntityManager em) {
        this.em = em;
    }

    public void publishUpdate(String machineName, String attributeName, String value) {

        if(machineName.equals("PunchingMachine01")) {
            return; // Ignore updates for PunchingMachine01
        }
        if(machineName.equals("received")) {
            return; // Ignore 'received' which probably results of a wrong parsing of the MQTT data
        }

        BaseMachine machine = machines.get(machineName);

        if (machine == null) {
            logger.error("Machine not found: '" + machineName + "'. Available machines: " + machines.keySet());
            return;
        }

        if(attributeName.equals("isExecuting")) {
            if(value.equals("true")) {
                if(machine.getState() == MachineState.RUNNING) {
                    return; 
                }
                logger.info("Machine " + machineName + " is now running.");
                machine.setState(MachineState.RUNNING);
            } else {
                if(machine.getState() == MachineState.IDLE) {
                    return; 
                }
                logger.info("Machine " + machineName + " is now idle.");
                machine.setState(MachineState.IDLE);
            }
        }else {
            machine.processMQTT(attributeName, value);
        }

        machine.setDirty(true);
    }

    @PostConstruct
    public void init() {
        logger.info("MachineRegistry initializing...");

        SortingLine sortingLine = loadLastOrCreate(SortingLine.class, "SortingLine01");
        Highbay highbay = loadLastOrCreate(Highbay.class, "HighBay01");
        VacuumGripper vacuumGripper = loadLastOrCreate(VacuumGripper.class, "VacuumGripper01");
        VacuumGripper vacuumGripper2 = loadLastOrCreate(VacuumGripper.class, "VacuumGripper02");
        MultiProcessing multiProcessing = loadLastOrCreate(MultiProcessing.class, "MultiProcessing01");
        ConveyorBelt conveyorBelt = loadLastOrCreate(ConveyorBelt.class, "ConveyorBelt01");
        
        machines.put("SortingLine01", sortingLine);
        machines.put("HighBay01", highbay);
        machines.put("VacuumGripper01", vacuumGripper);
        machines.put("VacuumGripper02", vacuumGripper2);
        machines.put("MultiProcessing01", multiProcessing);
        machines.put("ConveyorBelt01", conveyorBelt);

        logger.info("Machine registry initialized with machines: " + machines.keySet());
    }

    private <T extends BaseMachine> T loadLast(Class<T> machineClass, String machineName) {
        try {
            return em.createQuery(
                    "SELECT e FROM " + machineClass.getSimpleName() +
                    " e WHERE e.machineName = :machineName ORDER BY e.id DESC",
                    machineClass)
                .setParameter("machineName", machineName)
                .setMaxResults(1)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private <T extends BaseMachine> T loadLastOrCreate(Class<T> machineClass, String machineName) {
        T machine = loadLast(machineClass, machineName);

        if (machine == null) {
            try {
                logger.info("Creating new instance of " + machineName);
                machine = machineClass.getDeclaredConstructor().newInstance(); // Create a new instance dynamically
                machine.setMachineName(machineName);
                machine.setDirty(true);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create a new instance of " + machineName, e);
            }
        } else {
            logger.info("Loaded existing " + machineName + ": " + machine);
            if (machine.getMachineName() == null) {
                machine.setMachineName(machineName);
                machine.setDirty(true);
            }
        }

        return machine;
    }

    @Scheduled(fixedRate = 300)
    @Transactional
    public void tick() {

        for(BaseMachine machine : machines.values()) {
            if (machine.isDirty()) {
                em.detach(machine);
                machine.setId(null);
                em.persist(machine);
                machine.setDirty(false);
            }
        }
    }

    public List<BaseMachine> getAllMachines() {
        return new ArrayList<>(this.machines.values());
    }
    public String getMachineJson(String machineName) {
        BaseMachine machine = machines.get(machineName);
        if (machine == null) {
            return "{}"; // Return empty JSON object if machine not found
        }
        try {
            return objectMapper.writeValueAsString(machine);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public Map<String, BaseMachine> getMachines() {
        return new HashMap<>(machines); // Return defensive copy
    }

    public BaseMachine getMachineByName(String machineName) {
        return machines.get(machineName);
    }
}
