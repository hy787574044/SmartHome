package com.smarthome.model.dto;

import lombok.Data;

/**
 * 设备属性上报/下发 DTO
 */
@Data
public class DevicePropertyDTO {

    /** 物模型标识符 */
    private String id;

    /** 属性值 */
    private String value;

    /** 备注 */
    private String remark;
}
