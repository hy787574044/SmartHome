package com.smarthome.web.controller;

import com.smarthome.alert.service.NotificationConfigService;
import com.smarthome.common.result.R;
import com.smarthome.model.entity.NotificationConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知配置 API
 */
@Tag(name = "通知配置")
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationConfigService notificationConfigService;

    @Operation(summary = "获取当前用户通知配置")
    @GetMapping("/config")
    public R<List<NotificationConfig>> getConfig() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return R.ok(notificationConfigService.listByUserId(userId));
    }

    @Operation(summary = "保存通知配置")
    @PostMapping("/config")
    public R<NotificationConfig> saveConfig(@RequestBody NotificationConfig config) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        config.setUserId(userId);
        return R.ok(notificationConfigService.save(config));
    }

    @Operation(summary = "删除通知配置")
    @DeleteMapping("/config/{configId}")
    public R<Void> deleteConfig(@PathVariable Long configId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        notificationConfigService.delete(configId, userId);
        return R.ok();
    }

    @Operation(summary = "测试发送通知")
    @PostMapping("/test")
    public R<Void> testNotification(@RequestParam String notifyType,
                                    @RequestParam(defaultValue = "这是一条测试通知消息") String message) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        notificationConfigService.testSend(userId, notifyType, message);
        return R.ok();
    }

    @Operation(summary = "获取免打扰设置")
    @GetMapping("/quiet-hours")
    public R<Map<String, Object>> getQuietHours() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String, Object> result = notificationConfigService.getQuietHours(userId);
        return R.ok(result);
    }

    @Operation(summary = "保存免打扰设置")
    @PostMapping("/quiet-hours")
    public R<Void> saveQuietHours(@RequestBody Map<String, String> params) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String startTime = params.get("startTime");
        String endTime = params.get("endTime");
        notificationConfigService.saveQuietHours(userId, startTime, endTime);
        return R.ok();
    }
}
