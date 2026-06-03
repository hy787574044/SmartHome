package com.smarthome.web.controller;

import com.smarthome.common.result.R;
import com.smarthome.device.service.SystemConfigService;
import com.smarthome.model.entity.SystemConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统管理 API
 */
@Tag(name = "系统管理")
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemConfigService systemConfigService;

    @Operation(summary = "获取所有系统配置")
    @GetMapping("/config")
    public R<List<SystemConfig>> listConfigs() {
        return R.ok(systemConfigService.listAll());
    }

    @Operation(summary = "更新系统配置（批量）")
    @PutMapping("/config")
    public R<Void> updateConfigs(@RequestBody Map<String, String> configs) {
        systemConfigService.updateConfigs(configs);
        return R.ok();
    }

    @Operation(summary = "获取系统信息")
    @GetMapping("/info")
    public R<Map<String, Object>> getSystemInfo() {
        return R.ok(systemConfigService.getSystemInfo());
    }

    @Operation(summary = "清理过期日志")
    @PostMapping("/cleanLogs")
    public R<Integer> cleanLogs(@RequestParam(defaultValue = "90") int days) {
        int deleted = systemConfigService.cleanExpiredLogs(days);
        return R.ok(deleted, "已清理 " + deleted + " 条过期日志");
    }
}
