package com.smarthome.alert.service.notify;

import com.smarthome.model.entity.Device;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotifyService {

    /**
     * 是否支持该通知类型
     */
    boolean supports(String notifyType);

    /**
     * 发送通知（指定用户）
     *
     * @param userId     用户ID，用于查找用户的通知配置
     * @param device     设备信息
     * @param message    通知消息
     * @param alertLevel 告警级别: 1=提示 2=警告 3=严重
     */
    void send(Long userId, Device device, String message, int alertLevel);

    /**
     * 获取该通知服务支持的所有用户ID列表（用于告警广播）
     * 返回所有已启用该类型通知的用户ID
     */
    List<Long> getSupportedUserIds(String notifyType);
}
