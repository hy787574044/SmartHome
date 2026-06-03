package com.smarthome.alert.service.notify;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthome.model.entity.Device;
import com.smarthome.model.entity.NotificationConfig;
import com.smarthome.model.mapper.NotificationConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 企业微信通知服务
 * 通过 Webhook 发送企业微信消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatNotifyService implements NotifyService {

    private final NotificationConfigMapper notificationConfigMapper;

    @Override
    public boolean supports(String notifyType) {
        return "wechat".equals(notifyType);
    }

    @Override
    public void send(Long userId, Device device, String message, int alertLevel) {
        NotificationConfig config = notificationConfigMapper.selectOne(
                new LambdaQueryWrapper<NotificationConfig>()
                        .eq(NotificationConfig::getUserId, userId)
                        .eq(NotificationConfig::getNotifyType, "wechat")
                        .eq(NotificationConfig::getEnable, 1)
                        .last("LIMIT 1")
        );
        if (config == null || config.getConfig() == null) {
            log.warn("用户 {} 未配置企业微信通知", userId);
            return;
        }

        // 解析 webhook URL
        String webhookUrl = parseWebhookUrl(config.getConfig());
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            log.warn("用户 {} 的企业微信 webhook URL 为空", userId);
            return;
        }

        // 构建消息内容
        String levelText = getLevelText(alertLevel);
        String content = String.format("【SmartHome 告警】\n级别: %s\n设备: %s\n详情: %s",
                levelText, device.getDeviceName(), message);

        // 发送企业微信 Webhook 消息
        JSONObject body = new JSONObject();
        body.set("msgtype", "text");
        JSONObject text = new JSONObject();
        text.set("content", content);
        body.set("text", text);

        try {
            HttpResponse response = HttpRequest.post(webhookUrl)
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .timeout(10000)
                    .execute();
            if (response.isOk()) {
                JSONObject resp = JSONUtil.parseObj(response.body());
                int errcode = resp.getInt("errcode", -1);
                if (errcode == 0) {
                    log.info("企业微信通知发送成功 - 用户: {}, 设备: {}", userId, device.getDeviceName());
                } else {
                    log.error("企业微信通知发送失败 - errcode: {}, errmsg: {}", errcode, resp.getStr("errmsg"));
                }
            } else {
                log.error("企业微信通知 HTTP 请求失败 - status: {}", response.getStatus());
            }
        } catch (Exception e) {
            log.error("企业微信通知发送异常 - 用户: {}", userId, e);
        }
    }

    @Override
    public List<Long> getSupportedUserIds(String notifyType) {
        if (!"wechat".equals(notifyType)) {
            return java.util.Collections.emptyList();
        }
        List<NotificationConfig> configs = notificationConfigMapper.selectList(
                new LambdaQueryWrapper<NotificationConfig>()
                        .eq(NotificationConfig::getNotifyType, "wechat")
                        .eq(NotificationConfig::getEnable, 1)
        );
        return configs.stream()
                .map(NotificationConfig::getUserId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 从 JSON 配置中解析 webhook URL
     */
    private String parseWebhookUrl(String configJson) {
        try {
            JSONObject json = JSONUtil.parseObj(configJson);
            return json.getStr("webhookUrl");
        } catch (Exception e) {
            log.error("解析企业微信配置失败", e);
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
