package com.seurteaching.digitaltwin.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttReceiverConfig {

    @Value("${spring.mqtt.broker-url}")
    private String brokerUrl;

    @Value("${spring.mqtt.client-id}")
    private String clientId;

    @Value("${spring.mqtt.username:}")
    private String username;

    @Value("${spring.mqtt.password:}")
    private String password;

    @Value("${spring.mqtt.topic}")
    private String[] topics;

    private final MachineRegistry MR;

    public MqttReceiverConfig(MachineRegistry MR){
        this.MR = MR;
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setServerURIs(new String[]{brokerUrl});
        // no username/password for anonymous local broker
        factory.setConnectionOptions(opts);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer mqttInboundAdapter(MqttPahoClientFactory factory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
            new MqttPahoMessageDrivenChannelAdapter(clientId, factory, topics);
        adapter.setOutputChannel(mqttInputChannel());
        adapter.setQos(1);
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler messageHandler() {
        return message -> {
            String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);
            String payload = (String) message.getPayload();

            String machineName = MqttUtils.getMachineName(topic);
            String attributeName = MqttUtils.getAttributeName(topic);
            String value = MqttUtils.getValue(payload);

            MR.publishUpdate(machineName, attributeName, value);
        };
    }
}
