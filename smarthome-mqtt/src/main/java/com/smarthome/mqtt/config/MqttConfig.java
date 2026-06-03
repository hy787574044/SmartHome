package com.smarthome.mqtt.config;

import com.smarthome.mqtt.handler.MqttMessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * MQTT 配置类
 * 使用 Spring Integration MQTT 对接 EMQX
 * 通过 mqtt.enabled=true 开启，默认关闭（方便开发调试）
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
@RequiredArgsConstructor
public class MqttConfig {

    private final MqttProperties mqttProperties;
    private final MqttMessageHandler mqttMessageHandler;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{mqttProperties.getBroker()});
        options.setUserName(mqttProperties.getUsername());
        options.setPassword(mqttProperties.getPassword().toCharArray());
        options.setConnectionTimeout(mqttProperties.getConnectionTimeout());
        options.setKeepAliveInterval(mqttProperties.getKeepAliveInterval());
        options.setAutomaticReconnect(mqttProperties.isAutomaticReconnect());
        options.setCleanSession(mqttProperties.isCleanSession());
        options.setMaxInflight(1000);
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        mqttProperties.getClientId() + "-inbound",
                        mqttClientFactory(),
                        "/#"
                );
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(mqttProperties.getQos());
        adapter.setOutputChannel(mqttInputChannel());
        log.info("MQTT 入站适配器已启动，订阅 Topic: /#");
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler inboundMessageHandler() {
        return message -> {
            try {
                String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
                String payload = message.getPayload().toString();
                mqttMessageHandler.handleMessage(topic, payload);
            } catch (Exception e) {
                log.error("处理 MQTT 消息异常", e);
            }
        };
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler outbound() {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler(
                        mqttProperties.getClientId() + "-outbound",
                        mqttClientFactory()
                );
        handler.setAsync(true);
        handler.setDefaultTopic("/default");
        handler.setDefaultQos(mqttProperties.getQos());
        log.info("MQTT 出站处理器已启动");
        return handler;
    }
}
