package com.smarthome.web.controller;

import com.smarthome.model.vo.DeviceStatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * WebSocket 推送服务
 * 用于将设备状态变更实时推送到前端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 推送设备状态变更
     */
    public void pushDeviceStatus(DeviceStatusVO status) {
        messagingTemplate.convertAndSend("/topic/device/status", status);
    }

    /**
     * 推送告警消息
     */
    public void pushAlert(Object alert) {
        messagingTemplate.convertAndSend("/topic/alert", alert);
    }

    /**
     * 推送场景执行结果
     */
    public void pushSceneEvent(Object event) {
        messagingTemplate.convertAndSend("/topic/scene", event);
    }
}
