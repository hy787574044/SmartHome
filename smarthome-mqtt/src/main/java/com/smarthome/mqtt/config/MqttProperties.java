package com.smarthome.mqtt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MQTT 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

    /** MQTT Broker 地址 */
    private String broker = "tcp://localhost:1883";

    /** 客户端ID */
    private String clientId = "smarthome-server";

    /** 用户名 */
    private String username = "smarthome";

    /** 密码 */
    private String password = "smarthome";

    /** 连接超时（秒） */
    private int connectionTimeout = 30;

    /** 心跳间隔（秒） */
    private int keepAliveInterval = 60;

    /** 是否自动重连 */
    private boolean automaticReconnect = true;

    /** 是否清除会话 */
    private boolean cleanSession = true;

    /** QoS 级别: 0, 1, 2 */
    private int qos = 1;
}
