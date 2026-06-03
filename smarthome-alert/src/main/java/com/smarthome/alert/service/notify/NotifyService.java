package com.smarthome.alert.service.notify;

import com.smarthome.model.entity.Device;

/**
 * 通知服务接口
 */
public interface NotifyService {

    /**
     * 是否支持该通知类型
     */
    boolean supports(String notifyType);

    /**
     * 发送通知
     */
    void send(Device device, String message, int alertLevel);
}
