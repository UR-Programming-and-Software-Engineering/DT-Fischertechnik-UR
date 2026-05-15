package com.seurteaching.digitaltwin.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.seurteaching.digitaltwin.machines.BaseMachine;
import com.seurteaching.digitaltwin.mqtt.MachineRegistry;
import com.seurteaching.digitaltwin.simulation.SimulationController;
import com.seurteaching.digitaltwin.simulation.util.Packet;
import com.seurteaching.digitaltwin.simulation.SimulationBaseMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/*
 * Handles WebSocket connections and periodically sends updates about the simulation state.
 * Every 500ms, it sends
 * 1) Simulation Mode: packets and machines
 * 2) MQTT Mode: only machines
 */
@Component
public class WebsocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketHandler.class);

    private final SimulationController simulationController;
    private final MachineRegistry MR;

    public WebsocketHandler(SimulationController simulationController, MachineRegistry MR) {
        this.simulationController = simulationController;
        this.MR = MR;
    }

    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        // For now, we do not expect any messages from clients
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        sessions.add(session);
        logger.info("WebSocket connection established: " + session.getId());
        String data = createUpdateDataJson();
        sendDataToSession(session, data);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status)
            throws Exception {
        sessions.remove(session);
        logger.info("WebSocket connection closed: " + session.getId());
    }

    // Updates all connected clients with the current simulation state
    @Scheduled(fixedRate = 500)
    public void sendPeriodicUpdates() {
        if (!sessions.isEmpty()) {
            try {
                String jsonUpdate = createUpdateDataJson();

                sessions.removeIf(session -> {
                    return sendDataToSession(session, jsonUpdate);
                });

            } catch (Exception e) {
                logger.error("Error creating WebSocket response", e);
            }
        }
    }

    private boolean sendDataToSession(WebSocketSession session, String data) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(data));
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            logger.error("Error sending message to session " + session.getId(), e);
            return true;
        }
    }

    private String createUpdateDataJson() throws JsonProcessingException {
        Map<String, Object> updateData = new HashMap<>();

        updateData.put("simulationRunning", simulationController.isRunning());

        if (simulationController.isRunning()) {
            List<Map<String, Object>> machines = simulationController.getMachines().stream()
                    .map(this::createMachineDataWithEnergy)
                    .collect(Collectors.toList());
            updateData.put("machines", machines);

            // Get packet data
            List<Map<String, Object>> packetData = new ArrayList<>();
            for (Packet packet : simulationController.getPackets()) {
                // generatePacketInfo is more in depth and should be used in the future. for now
                // FEspecs should suffice
                Map<String, Object> packetInfo = generatePacketInfoFEspecs(packet);
                packetData.add(packetInfo);
            }
            updateData.put("packages", packetData);
        } else {
            List<BaseMachine> machines = MR.getAllMachines();
            updateData.put("machines", machines);
            updateData.put("packages", new ArrayList<>()); // No packages in MQTT mode
        }
        return OBJECT_MAPPER.writeValueAsString(updateData);
    }

    /**
     * Creates machine data by converting the BaseMachine into a map,
     * then adds energy and heating fields.
     */
    private Map<String, Object> createMachineDataWithEnergy(SimulationBaseMachine<?, ?> simulationMachine) {
        BaseMachine base = simulationMachine.getBaseMachine();
        Map<String, Object> machineData = OBJECT_MAPPER.convertValue(
            base,
            new TypeReference<Map<String, Object>>() {}
        );
        double calculationOverload = Math.round(( (double) simulationMachine.getHeating() / (double) simulationMachine.getHeatThreshold() * 100.0));
        machineData.put("energyKWHperMinute", simulationMachine.getEnergyStorage().getWhPerMinute());
        machineData.put("energyKWHs", simulationMachine.getEnergyStorage().getWhPerHour());
        machineData.put("currentOverload", calculationOverload);
        machineData.put("overheatingCounter", simulationMachine.getOverheatingCounter());
        return machineData;
    }

    private Map<String, Object> generatePacketInfoFEspecs(Packet packet) {
        Map<String, Object> packetInfo = new HashMap<>();
        packetInfo.put("id", packet.getId());
        packetInfo.put("index", packet.getIndex());
        packetInfo.put("machineName", packet.getCurrentLocation());
        return packetInfo;
    }

}
