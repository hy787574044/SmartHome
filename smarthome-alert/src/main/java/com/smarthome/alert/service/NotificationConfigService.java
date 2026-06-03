package com.smarthome.alert.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthome.alert.service.notify.NotifyService;
import com.smarthome.common.exception.BusinessException;
import com.smarthome.model.entity.Device;
import com.smarthome.model.entity.NotificationConfig;
import com.smarthome.model.mapper.NotificationConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 通知配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConfigService {

    private final NotificationConfigMapper notificationConfigMapper;
    private final List<NotifyService> notifyServices;

    /**
     * 获取用户的通知配置列表
     */
    public List<NotificationConfig> listByUserId(Long userId) {
        return notificationConfigMapper.selectList(
                new LambdaQueryWrapper<NotificationConfig>()
                        .eq(NotificationConfig::getUserId, userId)
                        .orderByDesc(NotificationConfig::getCreateTime)
        );
    }

    /**
     * 保存通知配置（新增或更新）
     */
    public NotificationConfig save(NotificationConfig config) {
        if (config.getConfigId() != null) {
            notificationConfigMapper.updateById(config);
        } else {
            notificationConfigMapper.insert(config);
        }
        return config;
    }

    /**
     * 删除通知配置
     */
    public void delete(Long configId, Long userId) {
        NotificationConfig config = notificationConfigMapper.selectById(configId);
        if (config == null) {
            throw new BusinessException("通知配置不存在");
        }
        if (!config.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该通知配置");
        }
        notificationConfigMapper.deleteById(configId);
    }

    /**
     * 测试发送通知
     */
    public void testSend(Long userId, String notifyType, String message) {
        // 查找该用户对应类型且启用的配置
        NotificationConfig config = notificationConfigMapper.selectOne(
                new LambdaQueryWrapper<NotificationConfig>()
                        .eq(NotificationConfig::getUserId, userId)
                        .eq(NotificationConfig::getNotifyType, notifyType)
                        .eq(NotificationConfig::getEnable, 1)
                        .last("LIMIT 1")
        );
        if (config == null) {
            throw new BusinessException("未找到启用的" + notifyType + "通知配置");
        }

        for (NotifyService notifyService : notifyServices) {
            if (notifyService.supports(notifyType)) {
                // 构造一个临时设备对象用于测试
                Device testDevice = new Device();
                testDevice.setDeviceId(0L);
                testDevice.setDeviceName("测试设备");
                notifyService.send(userId, testDevice, message, 1);
                return;
            }
        }
        throw new BusinessException("不支持的通知类型: " + notifyType);
    }

    /**
     * 根据用户ID和通知类型获取启用的配置
     */
    public NotificationConfig getEnabledConfig(Long userId, String notifyType) {
        return notificationConfigMapper.selectOne(
                new LambdaQueryWrapper<NotificationConfig>()
                        .eq(NotificationConfig::getUserId, userId)
                        .eq(NotificationConfig::getNotifyType, notifyType)
                        .eq(NotificationConfig::getEnable, 1)
                        .last("LIMIT 1")
        );
    }

    /**
     * 获取免打扰设置
     */
    public Map<String, Object> getQuietHours(Long userId) {
        Map<String, Object> result = new java.util.HashMap<>();
        // 从用户的任意一条通知配置中获取免打扰时间
        NotificationConfig config = notificationConfigMapper.selectOne(
                new LambdaQueryWrapper<NotificationConfig>()
                        .eq(NotificationConfig::getUserId, userId)
                        .last("LIMIT 1")
        );
        if (config != null) {
            result.put("startTime", config.getQuietStart());
            result.put("endTime", config.getQuietEnd());
        } else {
            result.put("startTime", "");
            result.put("endTime", "");
        }
        return result;
    }

    /**
     * 保存免打扰设置
     */
    public void saveQuietHours(Long userId, String startTime, String endTime) {
        // 更新用户所有通知配置的免打扰时间
        List<NotificationConfig> configs = listByUserId(userId);
        for (NotificationConfig config : configs) {
            config.setQuietStart(startTime);
            config.setQuietEnd(endTime);
            notificationConfigMapper.updateById(config);
        }
        // 如果没有配置，创建一条默认配置
        if (configs.isEmpty()) {
            NotificationConfig config = new NotificationConfig();
            config.setUserId(userId);
            config.setNotifyType("log");
            config.setConfig("{}");
            config.setEnable(1);
            config.setQuietStart(startTime);
            config.setQuietEnd(endTime);
            notificationConfigMapper.insert(config);
        }
    }
}
