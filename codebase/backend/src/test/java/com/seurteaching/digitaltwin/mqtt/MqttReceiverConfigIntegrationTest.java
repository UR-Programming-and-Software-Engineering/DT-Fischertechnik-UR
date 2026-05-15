package com.seurteaching.digitaltwin.mqtt;

import java.time.LocalDateTime;
import com.seurteaching.digitaltwin.machines.BaseMachine;
import com.seurteaching.digitaltwin.machines.SortingLine;
import com.seurteaching.digitaltwin.machines.VacuumGripper;
import com.seurteaching.digitaltwin.machines.ConveyorBelt;
import com.seurteaching.digitaltwin.machines.GripperPosition;
import com.seurteaching.digitaltwin.machines.Highbay;
import com.seurteaching.digitaltwin.machines.MultiProcessing;
import com.seurteaching.digitaltwin.machines.Color;
import com.seurteaching.digitaltwin.machines.Branch;
import com.seurteaching.digitaltwin.machines.MachineState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

// File: src/test/java/com/seurteaching/digitaltwin/mqtt/MqttReceiverConfigIntegrationTest.java



@ActiveProfiles("test")
@SpringBootTest
public class MqttReceiverConfigIntegrationTest {

    @Autowired
    private MachineRegistry machineRegistry;

    @Autowired
    private MqttReceiverConfig mqttReceiverConfig;

    // @BeforeEach
    // void setup() {
    //     // Add a test machine to the registry
    //     if (!machineRegistry.getMachines().containsKey("SortingLine01")) {
    //         SortingLine sl = new SortingLine();
    //         sl.setMachineName("SortingLine01");
    //         //machineRegistry.put("SortingLine01", sl);
    //     }
    // }

    @Test
    void Super_test_test() {
        System.out.println("Super test test");
    }
    
// SortingLine Tests------------------------------------------------------------------------------------------------------------

    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_colorPresentBLUE() {
        // Arrange
        String topic = "PLC/Island 1/Sorting/SortingLine01/measurements/input/colorPresent";
        String payload = "{\"value\":\"BLUE\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("SortingLine01");
        assertThat(machine).isInstanceOf(SortingLine.class);
        SortingLine sl = (SortingLine) machine;
        assertThat(sl.getColorPresent()).isEqualTo(Color.BLUE);
    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_colorPresentRED() {
        // Arrange
        String topic = "PLC/Island 1/Sorting/SortingLine01/measurements/input/colorPresent";
        String payload = "{\"value\":\"RED\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("SortingLine01");
        assertThat(machine).isInstanceOf(SortingLine.class);
        SortingLine sl = (SortingLine) machine;
        assertThat(sl.getColorPresent()).isEqualTo(Color.RED);
    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_colorPresentWHITE() {
        // Arrange
        String topic = "PLC/Island 1/Sorting/SortingLine01/measurements/input/colorPresent";
        String payload = "{\"value\":\"WHITE\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("SortingLine01");
        assertThat(machine).isInstanceOf(SortingLine.class);
        SortingLine sl = (SortingLine) machine;
        assertThat(sl.getColorPresent()).isEqualTo(Color.WHITE);
    }




    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_SortingLine_conveyorRunningTrue() {
         // Arrange
        String topic = "PLC/Island 1/Sorting/SortingLine01/measurements/input/sortingLineActMotorConveyor";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("SortingLine01");
        assertThat(machine).isInstanceOf(SortingLine.class);
        SortingLine sl = (SortingLine) machine;
        assertThat(sl.isConveyorRunning()).isEqualTo(true);

    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_SortingLine_conveyorRunningFalse() {
         // Arrange
        String topic = "PLC/Island 1/Sorting/SortingLine01/measurements/input/sortingLineActMotorConveyor";
        String payload = "{\"value\":\"false\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("SortingLine01");
        assertThat(machine).isInstanceOf(SortingLine.class);
        SortingLine sl = (SortingLine) machine;
        assertThat(sl.isConveyorRunning()).isEqualTo(false);

    }



    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_SortingLine_ejectedToBranchLEFT() {

        // workaround...
        messageHandler_updatesMachineRegistry_whenMqttMessageReceived_colorPresentBLUE();

         // Arrange
        String topic = "PLC/Island 1/Sorting/SortingLine01/measurements/input/ejectedToBranch";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("SortingLine01");
        assertThat(machine).isInstanceOf(SortingLine.class);
        SortingLine sl = (SortingLine) machine;
        assertThat(sl.getEjectedToBranch()).isEqualTo(Branch.LEFT);

    }



    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_SortingLine_ejectedToBranchRIGHT() {

        // workaround...
        messageHandler_updatesMachineRegistry_whenMqttMessageReceived_colorPresentWHITE();

         // Arrange
        String topic = "PLC/Island 1/Sorting/SortingLine01/measurements/input/ejectedToBranch";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("SortingLine01");
        assertThat(machine).isInstanceOf(SortingLine.class);
        SortingLine sl = (SortingLine) machine;
        assertThat(sl.getEjectedToBranch()).isEqualTo(Branch.RIGHT);

    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_SortingLine_ejectedToBranchMIDDLE() {

        // workaround...
        messageHandler_updatesMachineRegistry_whenMqttMessageReceived_colorPresentRED();

         // Arrange
        String topic = "PLC/Island 1/Sorting/SortingLine01/measurements/input/ejectedToBranch";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("SortingLine01");
        assertThat(machine).isInstanceOf(SortingLine.class);
        SortingLine sl = (SortingLine) machine;
        assertThat(sl.getEjectedToBranch()).isEqualTo(Branch.MIDDLE);

    }




// ConveyorBelt Tests ------------------------------------------------------------------------------------------------------------

    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_ConveyorBelt_isExecuting() {
         // Arrange
        String topic = "PLC/Island 1/ConveyorBelt/ConveyorBelt01/measurements/input/isExecuting";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("ConveyorBelt01");
        assertThat(machine).isInstanceOf(ConveyorBelt.class);
        ConveyorBelt c = (ConveyorBelt) machine;
        assertThat(c.getState() == MachineState.RUNNING).isEqualTo(true);

    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_ConveyorBelt_isExecutingFALSE() {
         // Arrange
        String topic = "PLC/Island 1/ConveyorBelt/ConveyorBelt01/measurements/input/isExecuting";
        String payload = "{\"value\":\"false\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("ConveyorBelt01");
        assertThat(machine).isInstanceOf(ConveyorBelt.class);
        ConveyorBelt c = (ConveyorBelt) machine;
        assertThat(c.isConveyorRunning()).isEqualTo(false);

    }



    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_ConveyorBelt_conveyorActForwardTRUE() {
         // Arrange
        String topic = "PLC/Island 1/ConveyorBelt/ConveyorBelt01/measurements/input/conveyorActForward";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("ConveyorBelt01");
        assertThat(machine).isInstanceOf(ConveyorBelt.class);
        ConveyorBelt c = (ConveyorBelt) machine;
        assertThat(c.isForward()).isEqualTo(true);

    }



    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_ConveyorBelt_conveyorActForwardFALSE() {
         // Arrange
        String topic = "PLC/Island 1/ConveyorBelt/ConveyorBelt01/measurements/input/conveyorActForward";
        String payload = "{\"value\":\"false\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("ConveyorBelt01");
        assertThat(machine).isInstanceOf(ConveyorBelt.class);
        ConveyorBelt c = (ConveyorBelt) machine;
        assertThat(c.isForward()).isEqualTo(false);

    }

// MULTIPROCESSING TESTS------------------------------------------------------------------------------------------------------------

    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_MultiProcessing_multiProcessingSensVacuumGripperAtTurntable() {
         // Arrange
        String topic = "PLC/Island 1/MultiProcessing/MultiProcessing01/measurements/input/multiProcessingSensVacuumGripperAtTurntable";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("MultiProcessing01");
        assertThat(machine).isInstanceOf(MultiProcessing.class);
        MultiProcessing mp = (MultiProcessing) machine;
        assertThat(mp.getGripperPosition()).isEqualTo(GripperPosition.Turntable);

    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_MultiProcessing_multiProcessingSensVacuumGripperAtOven() {
         // Arrange
        String topic = "PLC/Island 1/MultiProcessing/MultiProcessing01/measurements/input/multiProcessingSensVacuumGripperAtOven";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("MultiProcessing01");
        assertThat(machine).isInstanceOf(MultiProcessing.class);
        MultiProcessing mp = (MultiProcessing) machine;
        assertThat(mp.getGripperPosition()).isEqualTo(GripperPosition.Oven);

    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_MultiProcessing_multiProcessingActGripperToOven() {
         // Arrange
        String topic = "PLC/Island 1/MultiProcessing/MultiProcessing01/measurements/input/multiProcessingActGripperToOven";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("MultiProcessing01");
        assertThat(machine).isInstanceOf(MultiProcessing.class);
        MultiProcessing mp = (MultiProcessing) machine;
        assertThat(mp.getGripperPosition()).isEqualTo(GripperPosition.Moving);

    }



    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_MultiProcessing_multiProcessingActGripperToTurntable() {
         // Arrange
        String topic = "PLC/Island 1/MultiProcessing/MultiProcessing01/measurements/input/multiProcessingActGripperToOven";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("MultiProcessing01");
        assertThat(machine).isInstanceOf(MultiProcessing.class);
        MultiProcessing mp = (MultiProcessing) machine;
        assertThat(mp.getGripperPosition()).isEqualTo(GripperPosition.Moving);

    }



    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_MultiProcessing_multiProcessingSensTurntablePosVacuum() {
         // Arrange
        String topic = "PLC/Island 1/MultiProcessing/MultiProcessing01/measurements/input/multiProcessingSensTurntablePosVacuum";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("MultiProcessing01");
        assertThat(machine).isInstanceOf(MultiProcessing.class);
        MultiProcessing mp = (MultiProcessing) machine;
        assertThat(mp.isTurntablePosVacuum()).isEqualTo(true);

    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_MultiProcessing_multiProcessingSensTurntablePosBelt() {
         // Arrange
        String topic = "PLC/Island 1/MultiProcessing/MultiProcessing01/measurements/input/multiProcessingSensTurntablePosBelt";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("MultiProcessing01");
        assertThat(machine).isInstanceOf(MultiProcessing.class);
        MultiProcessing mp = (MultiProcessing) machine;
        assertThat(mp.isTurntablePosBelt()).isEqualTo(true);

    }



    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_MultiProcessing_multiProcessingSensTurntablePosSaw() {
         // Arrange
        String topic = "PLC/Island 1/MultiProcessing/MultiProcessing01/measurements/input/multiProcessingSensTurntablePosSaw";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("MultiProcessing01");
        assertThat(machine).isInstanceOf(MultiProcessing.class);
        MultiProcessing mp = (MultiProcessing) machine;
        assertThat(mp.isTurntablePosSaw()).isEqualTo(true);

    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_MultiProcessing_multiProcessingSensTurntablePosSawFALSE() {
         // Arrange
        String topic = "PLC/Island 1/MultiProcessing/MultiProcessing01/measurements/input/multiProcessingSensTurntablePosSaw";
        String payload = "{\"value\":\"false\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("MultiProcessing01");
        assertThat(machine).isInstanceOf(MultiProcessing.class);
        MultiProcessing mp = (MultiProcessing) machine;
        assertThat(mp.isTurntablePosSaw()).isEqualTo(false);

    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_MultiProcessing_multiProcessingSensOven() {
         // Arrange
        String topic = "PLC/Island 1/MultiProcessing/MultiProcessing01/measurements/input/multiProcessingSensOven";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("MultiProcessing01");
        assertThat(machine).isInstanceOf(MultiProcessing.class);
        MultiProcessing mp = (MultiProcessing) machine;
        assertThat(mp.isSensOven()).isEqualTo(true);

    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_MultiProcessing_multiProcessingCompressor() {
         // Arrange
        String topic = "PLC/Island 1/MultiProcessing/MultiProcessing01/measurements/input/multiProcessingCompressor";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("MultiProcessing01");
        assertThat(machine).isInstanceOf(MultiProcessing.class);
        MultiProcessing mp = (MultiProcessing) machine;
        assertThat(mp.isMulttProcessingCompressorOn()).isEqualTo(true);

    }



    // VACUUM GRIPPER TESTS------------------------------------------------------------------------------------------------------------



    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_VacuumGripper_vacuumSensRotEncoderCounter() {
         // Arrange
        String topic = "PLC/Island 1/VacuumGripper/VacuumGripper01/measurements/input/vacuumSensRotEncoderCounter";
        String payload = "{\"value\":\"2690\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("VacuumGripper01");
        assertThat(machine).isInstanceOf(VacuumGripper.class);
        VacuumGripper vg = (VacuumGripper) machine;
        float expected = (float) ((2690.0f/61.2f) * 360.0f) % 360;
        assertThat(vg.getRotation()).isCloseTo(expected, within(0.01f));

    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_VacuumGripper_vacuumSensVerticalEncoderCounter() {
         // Arrange
        String topic = "PLC/Island 1/VacuumGripper/VacuumGripper01/measurements/input/vacuumSensVerticalEncoderCounter";
        String payload = "{\"value\":\"2690\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("VacuumGripper01");
        assertThat(machine).isInstanceOf(VacuumGripper.class);
        VacuumGripper vg = (VacuumGripper) machine;
        assertThat(vg.getHeight()).isEqualTo(Math.round((2690 * 5.0) / 75.0));

    }



    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_VacuumGripper_vacuumSensArmEncoderCounter() {
         // Arrange
        String topic = "PLC/Island 1/VacuumGripper/VacuumGripper01/measurements/input/vacuumSensArmEncoderCounter";
        String payload = "{\"value\":\"2690\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("VacuumGripper01");
        assertThat(machine).isInstanceOf(VacuumGripper.class);
        VacuumGripper vg = (VacuumGripper) machine;
        assertThat(vg.getForward()).isEqualTo(Math.round((2690 * 5.0) / 75.0));

    }



// HIGHBAY TESTS------------------------------------------------------------------------------------------------------------

    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_Highbay_cantileverBackward() {
         // Arrange
        String topic = "PLC/Island 1/HighBay/HighBay01/measurements/input/cantileverBackward";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("HighBay01");
        assertThat(machine).isInstanceOf(Highbay.class);
        Highbay hb = (Highbay) machine;
        assertThat(hb.isCantileverBackward()).isEqualTo(true);

    }



     @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_Highbay_cantileverForward() {
         // Arrange
        String topic = "PLC/Island 1/HighBay/HighBay01/measurements/input/cantileverForward";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("HighBay01");
        assertThat(machine).isInstanceOf(Highbay.class);
        Highbay hb = (Highbay) machine;
        assertThat(hb.isCantileverForward()).isEqualTo(true);

    }



    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_Highbay_cantileverUp() {
         // Arrange
        String topic = "PLC/Island 1/HighBay/HighBay01/measurements/input/cantileverUp";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("HighBay01");
        assertThat(machine).isInstanceOf(Highbay.class);
        Highbay hb = (Highbay) machine;
        assertThat(hb.isCantileverUp()).isEqualTo(true);

    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_Highbay_cantileverDown() {
         // Arrange
        String topic = "PLC/Island 1/HighBay/HighBay01/measurements/input/cantileverDown";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("HighBay01");
        assertThat(machine).isInstanceOf(Highbay.class);
        Highbay hb = (Highbay) machine;
        assertThat(hb.isCantileverDown()).isEqualTo(true);

    }


     @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_Highbay_horizontal() {
         // Arrange
        String topic = "PLC/Island 1/HighBay/HighBay01/measurements/input/horizontal";
        String payload = "{\"value\":\"20\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("HighBay01");
        assertThat(machine).isInstanceOf(Highbay.class);
        Highbay hb = (Highbay) machine;
        assertThat(hb.getHorizontal()).isEqualTo(20);

    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_Highbay_height() {
         // Arrange
        String topic = "PLC/Island 1/HighBay/HighBay01/measurements/input/height";
        String payload = "{\"value\":\"20\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("HighBay01");
        assertThat(machine).isInstanceOf(Highbay.class);
        Highbay hb = (Highbay) machine;
        assertThat(hb.getHeight()).isEqualTo(20);

    }


    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_Highbay_conveyorForward() {
         // Arrange
        String topic = "PLC/Island 1/HighBay/HighBay01/measurements/input/conveyorForward";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("HighBay01");
        assertThat(machine).isInstanceOf(Highbay.class);
        Highbay hb = (Highbay) machine;
        assertThat(hb.isConveyorForward()).isEqualTo(true);

    }




    @Test
    void messageHandler_updatesMachineRegistry_whenMqttMessageReceived_Highbay_conveyorBackward() {
         // Arrange
        String topic = "PLC/Island 1/HighBay/HighBay01/measurements/input/conveyorBackward";
        String payload = "{\"value\":\"true\",\"timestamp\":\"2025-05-12T12:03:35Z\"}";

        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_receivedTopic", topic)
                .build();

        // Act
        mqttReceiverConfig.messageHandler().handleMessage(message);

        // Assert
        BaseMachine machine = machineRegistry.getMachineByName("HighBay01");
        assertThat(machine).isInstanceOf(Highbay.class);
        Highbay hb = (Highbay) machine;
        assertThat(hb.isConveyorBackward()).isEqualTo(true);

    }



}