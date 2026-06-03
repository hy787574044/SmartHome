package com.smarthome.web.controller;

import com.smarthome.common.result.R;
import com.smarthome.device.service.DeviceService;
import com.smarthome.model.entity.Device;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
