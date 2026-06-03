package com.smarthome.mqtt.service;

import com.alibaba.fastjson2.JSON;
import com.smarthome.model.dto.DevicePropertyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * MQTT 消息发送服务
 */
@Slf4j
@Service
public class MqttSendService {

    @Autowired(required = false)
    @Qualifier("mqttOutboundChannel")
    private MessageChannel mqttOutboundChannel;

    /**
     * 发送属性读取指令
     */
    public void sendPropertyGet(String productId, String serialNumber, List<String> identifiers) {
        String topic = String.format("/%s/%s/property/get", productId, serialNumber);
        String payload = JSON.toJSONString(identifiers);
        publish(topic, payload);
    }

    /**
     * 发送功能调用指令
     */
    public void sendFunction(String productId, String serialNumber, List<DevicePropertyDTO> functions) {
        String topic = String.format("/%s/%s/functions/post", productId, serialNumber);
        String payload = JSON.toJSONString(functions);
        publish(topic, payload);
    }

    /**
     * 发送单个属性控制指令
     */
    public void sendPropertyControl(String productId, String serialNumber, String identifier, String value) {
        DevicePropertyDTO dto = new DevicePropertyDTO();
        dto.setId(identifier);
        dto.setValue(value);
        sendFunction(productId, serialNumber, Collections.singletonList(dto));
    }

    /**
     * 发布 MQTT 消息
     */
    public void publish(String topic, String payload) {
        publish(topic, payload, 1, false);
    }

    /**
     * 发布 MQTT 消息（完整参数）
     */
    public void publish(String topic, String payload, int qos, boolean retained) {
        if (mqttOutboundChannel == null) {
            log.warn("MQTT 未连接，消息未发送 - Topic: {}", topic);
            return;
        }
        try {
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(MqttHeaders.TOPIC, topic)
                    .setHeader(MqttHeaders.QOS, qos)
                    .setHeader(MqttHeaders.RETAINED, retained)
                    .build();
            mqttOutboundChannel.send(message);
            log.debug("MQTT 消息已发送 - Topic: {}, Payload: {}", topic, payload);
        } catch (Exception e) {
            log.error("MQTT 消息发送失败 - Topic: {}", topic, e);
        }
    }
}
