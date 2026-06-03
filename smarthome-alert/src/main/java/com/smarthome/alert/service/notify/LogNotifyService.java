package com.smarthome.alert.service.notify;

import com.smarthome.model.entity.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 日志通知服务（默认实现，仅记录日志）
 * 可扩展：邮件、短信、微信推送等
 */
@Slf4j
@Service
public class LogNotifyService implements NotifyService {

    @Override
    public boolean supports(String notifyType) {
        // 作为默认通知方式，处理所有类型
        return "log".equals(notifyType) || notifyType == null;
    }

    @Override
    public void send(Device device, String message, int alertLevel) {
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

        // TODO: 扩展其他通知方式
        // if ("email".equals(notifyType)) { 发送邮件 }
        // if ("sms".equals(notifyType)) { 发送短信 }
        // if ("wechat".equals(notifyType)) { 微信推送 }
    }
}
