package com.smarthome.model.vo;

import lombok.Data;

import java.util.Map;

/**
 * 设备状态 VO（返回给前端）
 */
@Data
public class DeviceStatusVO {

    private Long deviceId;

    private String deviceName;

    private String serialNumber;

    private Long productId;

    private String productName;

    private Long roomId;

    private String roomName;

    /** 设备状态: 1=未激活 2=禁用 3=在线 4=离线 */
    private Integer status;

    /** 设备属性值 Map<identifier, value> */
    private Map<String, String> properties;
}
