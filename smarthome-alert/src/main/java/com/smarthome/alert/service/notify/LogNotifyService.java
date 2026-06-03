package com.smarthome.alert.service.notify;

import com.smarthome.model.entity.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 日志通知服务（默认实现，仅记录日志）
 */
@Slf4j
@Service
public class LogNotifyService implements NotifyService {

    @Override
    public boolean supports(String notifyType) {
        return "log".equals(notifyType) || notifyType == null;
    }

    @Override
    public void send(Long userId, Device device, String message, int alertLevel) {
        String levelText;
        switch (alertLevel) {
            case 1:
                levelText = "提示";
                break;
            case 2:
                levelText = "警告";
                break;
            case 3:
                levelText = "严重";
                break;
            default:
                levelText = "未知";
        }
        log.warn("[告警通知][{}] 设备: {}, 消息: {}", levelText, device.getDeviceName(), message);
    }

    @Override
    public List<Long> getSupportedUserIds(String notifyType) {
        // 日志通知不关联特定用户
        return Collections.emptyList();
    }
}
