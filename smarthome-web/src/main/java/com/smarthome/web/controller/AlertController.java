package com.smarthome.web.controller;

import com.smarthome.alert.service.AlertService;
import com.smarthome.common.result.PageResult;
import com.smarthome.common.result.R;
import com.smarthome.model.entity.AlertLog;
import com.smarthome.model.entity.AlertRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 告警管理 API
 */
@Tag(name = "告警管理")
@RestController
@RequestMapping("/api/alert")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // ===== 告警规则 =====

    @Operation(summary = "创建告警规则")
    @PostMapping("/rule")
    public R<AlertRule> createRule(@RequestBody AlertRule rule) {
        return R.ok(alertService.createAlertRule(rule));
    }

    @Operation(summary = "更新告警规则")
    @PutMapping("/rule")
    public R<Void> updateRule(@RequestBody AlertRule rule) {
        alertService.updateAlertRule(rule);
        return R.ok();
    }

    @Operation(summary = "删除告警规则")
    @DeleteMapping("/rule/{alertId}")
    public R<Void> deleteRule(@PathVariable Long alertId) {
        alertService.deleteAlertRule(alertId);
        return R.ok();
    }

    @Operation(summary = "获取告警规则列表")
    @GetMapping("/rule/list")
    public R<List<AlertRule>> listRules(@RequestParam(required = false) Long deviceId) {
        return R.ok(alertService.listAlertRules(deviceId));
    }

    // ===== 告警日志 =====

    @Operation(summary = "获取告警日志")
    @GetMapping("/log/list")
    public R<PageResult<AlertLog>> listLogs(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer level,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(alertService.listAlertLogs(status, level, pageNum, pageSize));
    }

    @Operation(summary = "处理告警")
    @PostMapping("/log/{logId}/handle")
    public R<Void> handleAlert(@PathVariable Long logId, @RequestParam(required = false) String remark) {
        alertService.handleAlert(logId, remark);
        return R.ok();
    }

    @Operation(summary = "忽略告警")
    @PostMapping("/log/{logId}/ignore")
    public R<Void> ignoreAlert(@PathVariable Long logId, @RequestParam(required = false) String remark) {
        alertService.ignoreAlert(logId, remark);
        return R.ok();
    }
}
