package com.smarthome.alert.service.notify;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthome.model.entity.Device;
import com.smarthome.model.entity.NotificationConfig;
import com.smarthome.model.mapper.NotificationConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 邮件通知服务
 * 通过 JavaMailSender 发送告警邮件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotifyService implements NotifyService {

    private final NotificationConfigMapper notificationConfigMapper;
    private final JavaMailSender mailSender;

    @Override
    public boolean supports(String notifyType) {
        return "email".equals(notifyType);
    }

    @Override
    public void send(Long userId, Device device, String message, int alertLevel) {
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

        // 解析邮箱地址
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

    /**
     * 从 JSON 配置中解析邮箱地址
     */
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
