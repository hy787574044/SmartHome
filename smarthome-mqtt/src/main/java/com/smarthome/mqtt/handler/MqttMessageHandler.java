package com.smarthome.mqtt.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.smarthome.common.constant.Constants;
import com.smarthome.model.dto.DevicePropertyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * MQTT 消息处理器
 * 解析 Topic 和 Payload，分发到对应的业务处理器
 */
@Slf4j
@Component
public class MqttMessageHandler {

    /**
     * 消息监听器注册表
     * key: Topic 后缀（如 property/post, events/post）
     * value: 处理函数 (serialNumber, properties)
     */
    private final Map<String, BiConsumer<String, List<DevicePropertyDTO>>> listeners = new ConcurrentHashMap<>();

    /**
     * 设备状态监听器
     */
    private final List<BiConsumer<String, Boolean>> statusListeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * 注册属性消息监听器
     */
    public void addPropertyListener(String topicSuffix, BiConsumer<String, List<DevicePropertyDTO>> listener) {
        listeners.put(topicSuffix, listener);
    }

    /**
     * 注册设备状态监听器
     */
    public void addStatusListener(BiConsumer<String, Boolean> listener) {
        statusListeners.add(listener);
    }

    /**
     * 处理 MQTT 消息
     * Topic 格式: /{productId}/{serialNumber}/{type}/{direction}
     */
    public void handleMessage(String topic, String payload) {
        log.debug("收到 MQTT 消息 - Topic: {}, Payload: {}", topic, payload);

        if (topic == null || topic.isEmpty()) {
            return;
        }

        // 解析 Topic: /productId/serialNumber/type/direction
        String[] parts = topic.split("/");
        if (parts.length < 4) {
            log.warn("Topic 格式不正确: {}", topic);
            return;
        }

        String productId = parts[1];
        String serialNumber = parts[2];
        String type = parts[3];
        String direction = parts.length > 4 ? parts[4] : "";

        String topicSuffix = type + "/" + direction;

        try {
            switch (type) {
                case "property":
                    handlePropertyMessage(topicSuffix, serialNumber, payload);
                    break;
                case "functions":
                    handleFunctionMessage(serialNumber, payload);
                    break;
                case "events":
                    handleEventMessage(serialNumber, payload);
                    break;
                case "status":
                    handleStatusMessage(serialNumber, payload);
                    break;
                default:
                    log.debug("未处理的 Topic 类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理消息异常 - Topic: {}, Error: {}", topic, e.getMessage(), e);
        }
    }

    /**
     * 处理属性消息
     * Payload: [{"id":"temperature","value":"25.5"},{"id":"humidity","value":"60"}]
     */
    private void handlePropertyMessage(String topicSuffix, String serialNumber, String payload) {
        List<DevicePropertyDTO> properties = parseProperties(payload);
        if (properties.isEmpty()) {
            return;
        }

        log.info("设备属性上报 - 设备: {}, 属性数: {}", serialNumber, properties.size());

        // 通知所有监听器
        BiConsumer<String, List<DevicePropertyDTO>> listener = listeners.get(topicSuffix);
        if (listener != null) {
            listener.accept(serialNumber, properties);
        }

        // 通用监听器
        BiConsumer<String, List<DevicePropertyDTO>> allListener = listeners.get("property/all");
        if (allListener != null) {
            allListener.accept(serialNumber, properties);
        }
    }

    /**
     * 处理功能消息
     */
    private void handleFunctionMessage(String serialNumber, String payload) {
        log.info("设备功能响应 - 设备: {}", serialNumber);
        // 功能响应通常更新设备状态
        handlePropertyMessage("property/post", serialNumber, payload);
    }

    /**
     * 处理事件消息
     */
    private void handleEventMessage(String serialNumber, String payload) {
        log.info("设备事件上报 - 设备: {}, Payload: {}", serialNumber, payload);
        // 事件消息可触发告警和场景联动
        BiConsumer<String, List<DevicePropertyDTO>> listener = listeners.get("events/post");
        if (listener != null) {
            List<DevicePropertyDTO> events = parseProperties(payload);
            listener.accept(serialNumber, events);
        }
    }

    /**
     * 处理设备状态消息
     * Payload: {"status":"online"} 或 {"status":"offline"}
     */
    private void handleStatusMessage(String serialNumber, String payload) {
        try {
            JSONObject json = JSON.parseObject(payload);
            String status = json.getString("status");
            boolean online = "online".equals(status);
            log.info("设备状态变更 - 设备: {}, 状态: {}", serialNumber, status);

            for (BiConsumer<String, Boolean> listener : statusListeners) {
                listener.accept(serialNumber, online);
            }
        } catch (Exception e) {
            log.error("解析设备状态消息异常", e);
        }
    }

    /**
     * 解析属性 JSON 数组
     */
    private List<DevicePropertyDTO> parseProperties(String payload) {
        List<DevicePropertyDTO> result = new ArrayList<>();
        try {
            JSONArray array = JSON.parseArray(payload);
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = array.getJSONObject(i);
                DevicePropertyDTO dto = new DevicePropertyDTO();
                dto.setId(obj.getString("id"));
                dto.setValue(obj.getString("value"));
                dto.setRemark(obj.getString("remark"));
                result.add(dto);
            }
        } catch (Exception e) {
            log.error("解析属性消息异常: {}", payload, e);
        }
        return result;
    }
}
