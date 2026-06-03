package com.smarthome.web.controller;

import com.smarthome.alert.service.AlertService;
import com.smarthome.common.result.PageResult;
import com.smarthome.common.result.R;
import com.smarthome.model.entity.AlertLog;
import com.smarthome.model.entity.AlertRule;
import com.smarthome.model.vo.AlertStatsVO;
import com.smarthome.common.annotation.OperationLog;
import com.smarthome.web.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警管理 API
 */
@Tag(name = "告警管理")
@RestController
@RequestMapping("/api/alert")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final ExportService exportService;

    // ===== 告警规则 =====

    @Operation(summary = "创建告警规则")
    @OperationLog(module = "alert", operation = "创建")
    @PostMapping("/rule")
    public R<AlertRule> createRule(@RequestBody AlertRule rule) {
        return R.ok(alertService.createAlertRule(rule));
    }

    @Operation(summary = "更新告警规则")
    @OperationLog(module = "alert", operation = "修改")
    @PostMapping("/rule/update")
    public R<Void> updateRule(@RequestBody AlertRule rule) {
        alertService.updateAlertRule(rule);
        return R.ok();
    }

    @Operation(summary = "删除告警规则")
    @OperationLog(module = "alert", operation = "删除")
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
    @OperationLog(module = "alert", operation = "处理")
    @PostMapping("/log/{logId}/handle")
    public R<Void> handleAlert(@PathVariable Long logId, @RequestParam(required = false) String remark) {
        alertService.handleAlert(logId, remark);
        return R.ok();
    }

    @Operation(summary = "忽略告警")
    @OperationLog(module = "alert", operation = "忽略")
    @PostMapping("/log/{logId}/ignore")
    public R<Void> ignoreAlert(@PathVariable Long logId, @RequestParam(required = false) String remark) {
        alertService.ignoreAlert(logId, remark);
        return R.ok();
    }

    @Operation(summary = "导出告警日志")
    @GetMapping("/log/export")
    public void exportAlertLogs(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            HttpServletResponse response) throws IOException {
        exportService.exportAlertLogs(startTime, endTime, response);
    }

    // ===== 告警统计 =====

    @Operation(summary = "告警趋势（最近7天每天告警数量）")
    @GetMapping("/stats/trend")
    public R<List<AlertStatsVO>> getAlertTrend() {
        return R.ok(alertService.getAlertTrend());
    }

    @Operation(summary = "按告警级别统计")
    @GetMapping("/stats/level")
    public R<List<AlertStatsVO>> getAlertCountByLevel() {
        return R.ok(alertService.getAlertCountByLevel());
    }

    @Operation(summary = "按设备统计告警数量（Top10）")
    @GetMapping("/stats/device")
    public R<List<Map<String, Object>>> getAlertCountByDevice() {
        return R.ok(alertService.getAlertCountByDevice());
    }

    @Operation(summary = "告警概览（今日/待处理/已处理数量）")
    @GetMapping("/stats/summary")
    public R<Map<String, Object>> getAlertSummary() {
        return R.ok(alertService.getAlertSummary());
    }
}
