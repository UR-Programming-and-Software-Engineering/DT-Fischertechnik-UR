package com.seurteaching.digitaltwin.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MqttUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String getMachineName(String topic) {
        return getSegment(topic, 3);
    }

    public static String getAttributeName(String topic) {
        return getSegment(topic, 6);
    }

    public static String getValue(String jsonPayload) {
        if (jsonPayload == null || jsonPayload.isEmpty()) {
            return "";
        }
        try {
            JsonNode root = mapper.readTree(jsonPayload);
            JsonNode valueNode = root.get("value");
            return (valueNode == null || valueNode.isNull())
                    ? ""
                    : valueNode.asText();
        } catch (Exception e) {
            // optionally log the parse error
            return "";
        }
    }

    private static String getSegment(String topic, int index) {
        if (topic == null || topic.isEmpty()) {
            return "";
        }
        String[] parts = topic.split("/");
        return (parts.length > index) ? parts[index] : "";
    }
}
