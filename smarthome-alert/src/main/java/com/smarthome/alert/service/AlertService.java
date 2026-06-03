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
import com.smarthome.model.mapper.AlertLogMapper;
import com.smarthome.model.mapper.AlertRuleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRuleMapper alertRuleMapper;
    private final AlertLogMapper alertLogMapper;
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
        alertLog.setHandleTime(LocalDateTime.now());
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
        alertLog.setHandleTime(LocalDateTime.now());
        alertLog.setHandleRemark(remark);
        alertLogMapper.updateById(alertLog);
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
        alertLog.setCreateTime(LocalDateTime.now());
        alertLogMapper.insert(alertLog);
        log.warn("告警触发 - {}", message);
    }

    /**
     * 发送告警通知
     */
    private void sendNotification(AlertRule rule, Device device, String message) {
        for (NotifyService notifyService : notifyServices) {
            if (notifyService.supports(rule.getNotifyType())) {
                try {
                    notifyService.send(device, message, rule.getAlertLevel());
                } catch (Exception e) {
                    log.error("告警通知发送失败 - 类型: {}", rule.getNotifyType(), e);
                }
            }
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
