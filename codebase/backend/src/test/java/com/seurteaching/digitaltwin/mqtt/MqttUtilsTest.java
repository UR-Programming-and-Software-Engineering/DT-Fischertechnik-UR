package com.seurteaching.digitaltwin.mqtt;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

@JsonTest 
public class MqttUtilsTest {

    //tests for getMachineName method
    @Test
    @DisplayName("should extract VacuumGripper01 machine name from topic")
    void getMachineNameExtractsVacuumGripper01() {
        String topic = "PLC/Island 1/VacuumGripper/I1VacuumGripper01/measurements/input/vacuumSensVerticalEncoderCounter";
        assertEquals("I1VacuumGripper01", MqttUtils.getMachineName(topic));
    }

    @Test
    @DisplayName("should extract VacuumGripper02 machine name from topic")
    void getMachineNameExtractsVacuumGripper02() {
        String topic = "PLC/Island 1/VacuumGripper/I1VacuumGripper02/measurements/output/vacuumActValve";
        assertEquals("I1VacuumGripper02", MqttUtils.getMachineName(topic));
    }

    @Test
    @DisplayName("should extract ConveyorBelt01 machine name from topic")
    void getMachineNameExtractsConveyorBelt01() {
        String topic = "PLC/Island 1/ConveyorBelt/I1ConveyorBelt01/measurements/input/conveyorSensFeed";
        assertEquals("I1ConveyorBelt01", MqttUtils.getMachineName(topic));
    }

    @Test
    @DisplayName("should extract MultiProcessing01 machine name from topic")
    void getMachineNameExtractsMultiProcessing01() {
        String topic = "PLC/Island 1/MultiProcessing/I1MultiProcessing01/measurements/output/multiProcessingValveFeeder";
        assertEquals("I1MultiProcessing01", MqttUtils.getMachineName(topic));
    }

    @Test
    @DisplayName("should extract SortingLine01 machine name from topic")
    void getMachineNameExtractsSortingLine01() {
        String topic = "PLC/Island 1/SortingLine/I1SortingLine01/measurements/input/sortingLineSensInputLightBarrier";
        assertEquals("I1SortingLine01", MqttUtils.getMachineName(topic));
    }

    @Test
    @DisplayName("should return empty string for too short topic")
    void getMachineNameWithTooShortTopic() {
        String topic = "PLC/Island 1/VacuumGripper";
        assertEquals("", MqttUtils.getMachineName(topic));
    }

    @Test
    @DisplayName("should handle null topic for machine name")
    void getMachineNameWithNullTopic() {
        assertEquals("", MqttUtils.getMachineName(null));
    }

    @Test
    @DisplayName("should handle empty topic for machine name")
    void getMachineNameWithEmptyTopic() {
        assertEquals("", MqttUtils.getMachineName(""));
    }

    @Test
    @DisplayName("should handle topic with different number of separators")
    void getMachineNameWithDifferentSeparatorCount() {
        String topic = "a/b/c/d/e";
        assertEquals("d", MqttUtils.getMachineName(topic));
    }

    @Test
    @DisplayName("should handle topic with special characters in machine name")
    void getMachineNameWithSpecialCharacters() {
        String topic = "PLC/Island 1/VacuumGripper/Special@Machine#123/measurements/input/sensor";
        assertEquals("Special@Machine#123", MqttUtils.getMachineName(topic));
    }

    @Test
    @DisplayName("should handle topic with numbers only in machine name position")
    void getMachineNameWithOnlyNumbers() {
        String topic = "a/b/c/12345/e/f/g";
        assertEquals("12345", MqttUtils.getMachineName(topic));
    }
    
    @Test
    @DisplayName("NONSENSE: Should attempt to extract machine name from gibberish")
    void getMachineNameNonsenseInput() {
        String topic = "!!!///???///!!!///???";
        assertEquals("???", MqttUtils.getMachineName(topic));
    }

    //tests for getAttrributeName method
    @Test
    @DisplayName("should extract vacuumSensVerticalEncoderCounter attribute from topic")
    void getAttributeNameExtractsVerticalEncoder() {
        String topic = "PLC/Island 1/VacuumGripper/I1VacuumGripper01/measurements/input/vacuumSensVerticalEncoderCounter";
        assertEquals("vacuumSensVerticalEncoderCounter", MqttUtils.getAttributeName(topic));
    }

    @Test
    @DisplayName("should extract vacuumActValve attribute from topic")
    void getAttributeNameExtractsValve() {
        String topic = "PLC/Island 1/VacuumGripper/I1VacuumGripper02/measurements/output/vacuumActValve";
        assertEquals("vacuumActValve", MqttUtils.getAttributeName(topic));
    }

    @Test
    @DisplayName("should extract conveyorSensFeed attribute from topic")
    void getAttributeNameExtractsFeed() {
        String topic = "PLC/Island 1/ConveyorBelt/I1ConveyorBelt01/measurements/input/conveyorSensFeed";
        assertEquals("conveyorSensFeed", MqttUtils.getAttributeName(topic));
    }

    @Test
    @DisplayName("should extract multiProcessingValveFeeder attribute from topic")
    void getAttributeNameExtractsFeeder() {
        String topic = "PLC/Island 1/MultiProcessing/I1MultiProcessing01/measurements/output/multiProcessingValveFeeder";
        assertEquals("multiProcessingValveFeeder", MqttUtils.getAttributeName(topic));
    }

    @Test
    @DisplayName("should extract sortingLineSensInputLightBarrier attribute from topic")
    void getAttributeNameExtractsLightBarrier() {
        String topic = "PLC/Island 1/SortingLine/I1SortingLine01/measurements/input/sortingLineSensInputLightBarrier";
        assertEquals("sortingLineSensInputLightBarrier", MqttUtils.getAttributeName(topic));
    }

    @Test
    @DisplayName("should extract machine_feedback attribute from topic")
    void getAttributeNameExtractsMachineFeedback() {
        String topic = "PLC/Island 1/SortingLine/I1SortingLine01/events/emitted/machine_feedback";
        assertEquals("machine_feedback", MqttUtils.getAttributeName(topic));
    }

    @Test
    @DisplayName("should return empty string for too short topic when getting attribute")
    void getAttributeNameWithTooShortTopic() {
        String topic = "PLC/Island 1/VacuumGripper/I1VacuumGripper01/measurements";
        assertEquals("", MqttUtils.getAttributeName(topic));
    }

    @Test
    @DisplayName("should handle null topic for attribute name")
    void getAttributeNameWithNullTopic() {
        assertEquals("", MqttUtils.getAttributeName(null));
    }

    @Test
    @DisplayName("should handle empty topic for attribute name")
    void getAttributeNameWithEmptyTopic() {
        assertEquals("", MqttUtils.getAttributeName(""));
    }

    @Test
    @DisplayName("should handle topic with different number of separators for attribute")
    void getAttributeNameWithDifferentSeparatorCount() {
        String topic = "a/b/c/d/e/f/attribute/extra";
        assertEquals("attribute", MqttUtils.getAttributeName(topic));
    }

    @Test
    @DisplayName("should handle topic with special characters in attribute name")
    void getAttributeNameWithSpecialCharacters() {
        String topic = "PLC/Island 1/VacuumGripper/I1VacuumGripper01/measurements/input/special@attribute#123";
        assertEquals("special@attribute#123", MqttUtils.getAttributeName(topic));
    }
    
     

    //tests for getvalue method
    @Test
    @DisplayName("should extract numeric value from JSON payload")
    void getValueExtractsNumericValue() {
        String payload = "{\"value\": 0, \"timestamp\": \"2025-07-04T11:56:51.774495+00:00Z\"}";
        assertEquals("0", MqttUtils.getValue(payload));
    }

    @Test
    @DisplayName("should extract boolean true value from JSON payload")
    void getValueExtractsBooleanTrueValue() {
        String payload = "{\"value\": true, \"timestamp\": \"2025-07-04T11:56:51.775600+00:00Z\"}";
        assertEquals("true", MqttUtils.getValue(payload));
    }

    @Test
    @DisplayName("should extract boolean false value from JSON payload")
    void getValueExtractsBooleanFalseValue() {
        String payload = "{\"value\": false, \"timestamp\": \"2025-07-04T11:56:51.776163+00:00Z\"}";
        assertEquals("false", MqttUtils.getValue(payload));
    }

    @Test
    @DisplayName("should extract complex string value from JSON payload")
    void getValueExtractsComplexStringValue() {
        String payload = "{\"value\": \"{\\\"jsonType\\\": \\\"MACHINE_FEEDBACK\\\", \\\"status\\\": \\\"UNINITIALIZED_IDLE\\\", \\\"info\\\": \\\"\\\"}\", \"timestamp\": \"2025-07-04T11:56:51.790150+00:00Z\"}";
        assertEquals("{\"jsonType\": \"MACHINE_FEEDBACK\", \"status\": \"UNINITIALIZED_IDLE\", \"info\": \"\"}", MqttUtils.getValue(payload));
    }

    @Test
    @DisplayName("should handle null JSON payload")
    void getValueWithNullPayload() {
        assertEquals("", MqttUtils.getValue(null));
    }

    @Test
    @DisplayName("should handle empty JSON payload")
    void getValueWithEmptyPayload() {
        assertEquals("", MqttUtils.getValue(""));
    }

    @Test
    @DisplayName("should handle malformed JSON")
    void getValueWithMalformedJson() {
        assertEquals("", MqttUtils.getValue("{not valid json}"));
    }

    @Test
    @DisplayName("should handle JSON without value field")
    void getValueWithMissingValueField() {
        assertEquals("", MqttUtils.getValue("{\"timestamp\": \"2025-07-04T11:56:51.774495+00:00Z\"}"));
    }

    @Test
    @DisplayName("should handle null value in JSON")
    void getValueWithNullValueInJson() {
        assertEquals("", MqttUtils.getValue("{\"value\": null, \"timestamp\": \"2025-07-04T11:56:51.774495+00:00Z\"}"));
    }

    @Test
    @DisplayName("should extract decimal value from JSON")
    void getValueExtractsDecimalValue() {
        String payload = "{\"value\": 123.456, \"timestamp\": \"2025-07-04T11:56:51.774495+00:00Z\"}";
        assertEquals("123.456", MqttUtils.getValue(payload));
    }

    @Test
    @DisplayName("should extract empty string value from JSON")
    void getValueExtractsEmptyStringValue() {
        String payload = "{\"value\": \"\", \"timestamp\": \"2025-07-04T11:56:51.774495+00:00Z\"}";
        assertEquals("", MqttUtils.getValue(payload));
    }
    
   
    
    @Test
    @DisplayName("NONSENSE: Should attempt to extract value from HTML instead of JSON")
    void getValueWithHtmlInsteadOfJson() {
        assertEquals("", MqttUtils.getValue("<html><body><p value=\"test\">This is not JSON</p></body></html>"));
    }
}
