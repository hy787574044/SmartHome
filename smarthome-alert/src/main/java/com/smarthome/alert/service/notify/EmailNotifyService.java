package com.smarthome.alert.service.notify;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthome.model.entity.Device;
import com.smarthome.model.entity.NotificationConfig;
import com.smarthome.model.mapper.NotificationConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 邮件通知服务（仅在 JavaMailSender 可用时启用）
 */
@Slf4j
@Service
@ConditionalOnBean(JavaMailSender.class)
public class EmailNotifyService implements NotifyService {

    @Autowired
    private NotificationConfigMapper notificationConfigMapper;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public boolean supports(String notifyType) {
        return "email".equals(notifyType) && mailSender != null;
    }

    @Override
    public void send(Long userId, Device device, String message, int alertLevel) {
        if (mailSender == null) {
            log.warn("邮件服务未配置，无法发送邮件");
            return;
        }

        NotificationConfig config = notificationConfigMapper.selectOne(
                new LambdaQueryWrapper<NotificationConfig>()
                        .eq(NotificationConfig::getUserId, userId)
                        .eq(NotificationConfig::getNotifyType, "email")
                        .eq(NotificationConfig::getEnable, 1)
                        .last("LIMIT 1")
        );
        if (config == null || config.getConfig() == null) {
            log.warn("用户 {} 未配置邮件通知", userId);
            return;
        }

        String emailAddress = parseEmailAddress(config.getConfig());
        if (emailAddress == null || emailAddress.isEmpty()) {
            log.warn("用户 {} 的邮件地址为空", userId);
            return;
        }

        String levelText = getLevelText(alertLevel);
        String subject = String.format("[SmartHome] %s告警 - %s", levelText, device.getDeviceName());
        String content = String.format(
                "【SmartHome 全屋智能告警通知】\n\n" +
                "告警级别: %s\n" +
                "设备名称: %s\n" +
                "告警详情: %s\n\n" +
                "请及时处理。",
                levelText, device.getDeviceName(), message
        );

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(emailAddress);
            mailMessage.setSubject(subject);
            mailMessage.setText(content);
            mailSender.send(mailMessage);
            log.info("邮件通知发送成功 - 用户: {}, 收件人: {}", userId, emailAddress);
        } catch (Exception e) {
            log.error("邮件通知发送异常 - 用户: {}, 收件人: {}", userId, emailAddress, e);
        }
    }

    @Override
    public List<Long> getSupportedUserIds(String notifyType) {
        if (!"email".equals(notifyType)) {
            return Collections.emptyList();
        }
        List<NotificationConfig> configs = notificationConfigMapper.selectList(
                new LambdaQueryWrapper<NotificationConfig>()
                        .eq(NotificationConfig::getNotifyType, "email")
                        .eq(NotificationConfig::getEnable, 1)
        );
        return configs.stream()
                .map(NotificationConfig::getUserId)
                .distinct()
                .collect(Collectors.toList());
    }

    private String parseEmailAddress(String configJson) {
        try {
            JSONObject json = JSONUtil.parseObj(configJson);
            return json.getStr("emailAddress");
        } catch (Exception e) {
            log.error("解析邮件配置失败", e);
            return null;
        }
    }

    private String getLevelText(int alertLevel) {
        switch (alertLevel) {
            case 1: return "提示";
            case 2: return "警告";
            case 3: return "严重";
            default: return "未知";
        }
    }
}
