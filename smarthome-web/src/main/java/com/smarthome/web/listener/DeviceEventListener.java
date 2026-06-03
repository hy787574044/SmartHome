package com.smarthome.web.listener;

import com.smarthome.device.service.DeviceService;
import com.smarthome.model.vo.DeviceStatusVO;
import com.smarthome.web.controller.WebSocketController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 设备事件监听器 → WebSocket 推送
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceEventListener {

    private final WebSocketController webSocketController;
    private final DeviceService deviceService;

    /**
     * 设备属性变更 → 推送到前端
     */
    @EventListener
    public void onDeviceProperty(DeviceService.DevicePropertyEvent event) {
        try {
            DeviceStatusVO status = deviceService.getDeviceStatus(event.getDevice().getDeviceId());
            webSocketController.pushDeviceStatus(status);
        } catch (Exception e) {
            log.error("WebSocket 推送设备属性失败", e);
        }
    }

    /**
     * 设备状态变更 → 推送到前端
     */
    @EventListener
    public void onDeviceStatus(DeviceService.DeviceStatusEvent event) {
        try {
            DeviceStatusVO status = deviceService.getDeviceStatus(event.getDevice().getDeviceId());
            webSocketController.pushDeviceStatus(status);
        } catch (Exception e) {
            log.error("WebSocket 推送设备状态失败", e);
        }
    }
}
