package com.smarthome.web.controller;

import com.smarthome.common.result.R;
import com.smarthome.device.service.DeviceService;
import com.smarthome.model.entity.Device;
import com.smarthome.model.vo.DeviceStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 首页仪表盘 API
 */
@Tag(name = "首页仪表盘")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DeviceService deviceService;

    @Operation(summary = "获取首页统计数据")
    @GetMapping("/stats")
    public R<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Device> allDevices = deviceService.listDevices(null, null, null, 1, 10000).getRows();
        List<Device> onlineDevices = deviceService.listOnlineDevices();

        stats.put("totalDevices", allDevices.size());
        stats.put("onlineDevices", onlineDevices.size());
        stats.put("offlineDevices", allDevices.size() - onlineDevices.size());

        return R.ok(stats);
    }

    @Operation(summary = "获取实时传感器数据")
    @GetMapping("/realtime-sensors")
    public R<List<Map<String, Object>>> getRealtimeSensors() {
        // 获取所有在线的监测设备
        List<Device> devices = deviceService.listDevices(null, null, 3, 1, 100).getRows();

        List<Map<String, Object>> sensors = new ArrayList<>();
        for (Device device : devices) {
            DeviceStatusVO status = deviceService.getDeviceStatus(device.getDeviceId());
            if (status.getProperties() != null && !status.getProperties().isEmpty()) {
                Map<String, Object> sensor = new HashMap<>();
                sensor.put("deviceId", device.getDeviceId());
                sensor.put("deviceName", device.getDeviceName());
                sensor.put("roomId", device.getRoomId());
                sensor.put("properties", status.getProperties());
                sensors.add(sensor);
            }
        }

        return R.ok(sensors);
    }

    @Operation(summary = "获取设备状态分布")
    @GetMapping("/device-status")
    public R<Map<String, Object>> getDeviceStatus() {
        Map<String, Object> result = new HashMap<>();

        List<Device> allDevices = deviceService.listDevices(null, null, null, 1, 10000).getRows();

        long onlineCount = allDevices.stream().filter(d -> d.getStatus() == 3).count();
        long offlineCount = allDevices.stream().filter(d -> d.getStatus() == 4).count();
        long inactiveCount = allDevices.stream().filter(d -> d.getStatus() == 1).count();

        result.put("online", onlineCount);
        result.put("offline", offlineCount);
        result.put("inactive", inactiveCount);
        result.put("total", allDevices.size());

        return R.ok(result);
    }
}
