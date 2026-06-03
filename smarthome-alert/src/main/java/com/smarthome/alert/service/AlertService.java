package com.smarthome.alert.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthome.alert.service.notify.NotifyService;
import com.smarthome.common.constant.Constants;
import com.smarthome.common.exception.BusinessException;
import com.smarthome.common.result.PageResult;
import com.smarthome.device.service.DeviceService;
import com.smarthome.model.dto.DevicePropertyDTO;
import com.smarthome.model.entity.AlertLog;
import com.smarthome.model.entity.AlertRule;
import com.smarthome.model.entity.Device;
import com.smarthome.model.entity.NotificationConfig;
import com.smarthome.model.mapper.AlertLogMapper;
import com.smarthome.model.mapper.AlertRuleMapper;
import com.smarthome.model.mapper.NotificationConfigMapper;
import com.smarthome.model.vo.AlertStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 告警服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRuleMapper alertRuleMapper;
    private final AlertLogMapper alertLogMapper;
    private final NotificationConfigMapper notificationConfigMapper;
    private final List<NotifyService> notifyServices;

    /**
     * 监听设备属性上报事件 → 检查告警
     */
    @EventListener
    public void onDeviceProperty(DeviceService.DevicePropertyEvent event) {
        Device device = event.getDevice();
        List<DevicePropertyDTO> properties = event.getProperties();

        List<AlertRule> rules = alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>()
                        .eq(AlertRule::getDeviceId, device.getDeviceId())
                        .eq(AlertRule::getEnable, 1)
        );

        for (AlertRule rule : rules) {
            for (DevicePropertyDTO prop : properties) {
                if (prop.getId().equals(rule.getModelIdentifier())) {
                    checkAlert(rule, device, prop.getValue());
                }
            }
        }
    }

    /**
     * 创建告警规则
     */
    public AlertRule createAlertRule(AlertRule rule) {
        rule.setEnable(1);
        alertRuleMapper.insert(rule);
        return rule;
    }

    /**
     * 更新告警规则
     */
    public void updateAlertRule(AlertRule rule) {
        alertRuleMapper.updateById(rule);
    }

    /**
     * 删除告警规则
     */
    public void deleteAlertRule(Long alertId) {
        alertRuleMapper.deleteById(alertId);
    }

    /**
     * 获取告警规则列表
     */
    public List<AlertRule> listAlertRules(Long deviceId) {
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        if (deviceId != null) {
            wrapper.eq(AlertRule::getDeviceId, deviceId);
        }
        wrapper.orderByDesc(AlertRule::getCreateTime);
        return alertRuleMapper.selectList(wrapper);
    }

    /**
     * 获取告警日志（分页）
     */
    public PageResult<AlertLog> listAlertLogs(Integer status, Integer level, int pageNum, int pageSize) {
        LambdaQueryWrapper<AlertLog> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(AlertLog::getStatus, status);
        }
        if (level != null) {
            wrapper.eq(AlertLog::getAlertLevel, level);
        }
        wrapper.orderByDesc(AlertLog::getCreateTime);
        Page<AlertLog> page = alertLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    /**
     * 处理告警
     */
    public void handleAlert(Long logId, String remark) {
        AlertLog alertLog = alertLogMapper.selectById(logId);
        if (alertLog == null) {
            throw new BusinessException("告警记录不存在");
        }
        alertLog.setStatus(Constants.ALERT_STATUS_HANDLED);
        alertLog.setHandleTime(java.time.LocalDateTime.now());
        alertLog.setHandleRemark(remark);
        alertLogMapper.updateById(alertLog);
    }

    /**
     * 忽略告警
     */
    public void ignoreAlert(Long logId, String remark) {
        AlertLog alertLog = alertLogMapper.selectById(logId);
        if (alertLog == null) {
            throw new BusinessException("告警记录不存在");
        }
        alertLog.setStatus(Constants.ALERT_STATUS_IGNORED);
        alertLog.setHandleTime(java.time.LocalDateTime.now());
        alertLog.setHandleRemark(remark);
        alertLogMapper.updateById(alertLog);
    }

    // ===== 告警统计 =====

    /**
     * 告警趋势 - 最近7天每天告警数量
     */
    public List<AlertStatsVO> getAlertTrend() {
        return alertLogMapper.selectAlertTrend(7);
    }

    /**
     * 按告警级别统计
     */
    public List<AlertStatsVO> getAlertCountByLevel() {
        return alertLogMapper.selectAlertCountByLevel();
    }

    /**
     * 按设备统计告警数量（Top10）
     */
    public List<Map<String, Object>> getAlertCountByDevice() {
        return alertLogMapper.selectAlertCountByDevice(10);
    }

    /**
     * 告警概览（今日/待处理/已处理数量）
     */
    public Map<String, Object> getAlertSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("today", alertLogMapper.selectTodayAlertCount());
        summary.put("pending", alertLogMapper.selectAlertCountByStatus(Constants.ALERT_STATUS_PENDING));
        summary.put("handled", alertLogMapper.selectAlertCountByStatus(Constants.ALERT_STATUS_HANDLED));
        return summary;
    }

    /**
     * 检查告警条件
     */
    private void checkAlert(AlertRule rule, Device device, String actualValue) {
        boolean triggered = compareValue(actualValue, rule.getOperator(), rule.getThreshold());
        if (triggered) {
            String message = String.format("设备[%s]的[%s]值 %s %s (当前值: %s)",
                    device.getDeviceName(), rule.getModelIdentifier(),
                    rule.getOperator(), rule.getThreshold(), actualValue);

            createAlertLog(device, rule, actualValue, message);
            sendNotification(rule, device, message);
        }
    }

    /**
     * 创建告警日志
     */
    private void createAlertLog(Device device, AlertRule rule, String value, String message) {
        AlertLog alertLog = new AlertLog();
        alertLog.setAlertId(rule.getAlertId());
        alertLog.setDeviceId(device.getDeviceId());
        alertLog.setDeviceName(device.getDeviceName());
        alertLog.setAlertValue(value);
        alertLog.setAlertLevel(rule.getAlertLevel());
        alertLog.setAlertMessage(message);
        alertLog.setStatus(Constants.ALERT_STATUS_PENDING);
        alertLog.setCreateTime(java.time.LocalDateTime.now());
        alertLogMapper.insert(alertLog);
        log.warn("告警触发 - {}", message);
    }

    /**
     * 发送告警通知
     * 查找所有已启用该通知类型的用户，检查免打扰时段后发送
     */
    private void sendNotification(AlertRule rule, Device device, String message) {
        for (NotifyService notifyService : notifyServices) {
            if (notifyService.supports(rule.getNotifyType())) {
                List<Long> userIds = notifyService.getSupportedUserIds(rule.getNotifyType());
                for (Long userId : userIds) {
                    // 检查免打扰时段
                    if (isInQuietPeriod(userId, rule.getNotifyType())) {
                        log.info("用户 {} 当前处于免打扰时段，跳过 {} 通知", userId, rule.getNotifyType());
                        continue;
                    }
                    try {
                        notifyService.send(userId, device, message, rule.getAlertLevel());
                    } catch (Exception e) {
                        log.error("告警通知发送失败 - 用户: {}, 类型: {}", userId, rule.getNotifyType(), e);
                    }
                }
            }
        }
    }

    /**
     * 检查当前时间是否在用户的免打扰时段内
     *
     * @param userId     用户ID
     * @param notifyType 通知类型
     * @return true=在免打扰时段内，应跳过通知
     */
    private boolean isInQuietPeriod(Long userId, String notifyType) {
        NotificationConfig config = notificationConfigMapper.selectOne(
                new LambdaQueryWrapper<NotificationConfig>()
                        .eq(NotificationConfig::getUserId, userId)
                        .eq(NotificationConfig::getNotifyType, notifyType)
                        .eq(NotificationConfig::getEnable, 1)
                        .last("LIMIT 1")
        );
        if (config == null) {
            return false;
        }

        String quietStart = config.getQuietStart();
        String quietEnd = config.getQuietEnd();
        if (quietStart == null || quietEnd == null || quietStart.isEmpty() || quietEnd.isEmpty()) {
            return false;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime start = LocalTime.parse(quietStart, formatter);
            LocalTime end = LocalTime.parse(quietEnd, formatter);
            LocalTime now = LocalTime.now();

            // 处理跨天的情况，例如 23:00 ~ 07:00
            if (start.isBefore(end) || start.equals(end)) {
                // 不跨天: start <= now < end
                return !now.isBefore(start) && now.isBefore(end);
            } else {
                // 跨天: now >= start || now < end
                return !now.isBefore(start) || now.isBefore(end);
            }
        } catch (Exception e) {
            log.error("解析免打扰时间失败 - userId: {}, quietStart: {}, quietEnd: {}", userId, quietStart, quietEnd, e);
            return false;
        }
    }

    /**
     * 比较值
     */
    private boolean compareValue(String actual, String operator, String expected) {
        if (actual == null || operator == null || expected == null) {
            return false;
        }
        try {
            double actualNum = Double.parseDouble(actual);
            double expectedNum = Double.parseDouble(expected);
            switch (operator) {
                case "=": return actualNum == expectedNum;
                case "!=": return actualNum != expectedNum;
                case ">": return actualNum > expectedNum;
                case "<": return actualNum < expectedNum;
                case ">=": return actualNum >= expectedNum;
                case "<=": return actualNum <= expectedNum;
                default: return actual.equals(expected);
            }
        } catch (NumberFormatException e) {
            return actual.equals(expected);
        }
    }
}
