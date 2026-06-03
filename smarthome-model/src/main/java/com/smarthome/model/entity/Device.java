package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthome.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 设备
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("device")
public class Device extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long deviceId;

    /** 设备名称 */
    private String deviceName;

    /** 所属产品ID */
    private Long productId;

    /** 设备序列号（唯一标识） */
    private String serialNumber;

    /** 设备类型: 1=直连 2=网关 3=监测 */
    private Integer deviceType;

    /** 设备状态: 1=未激活 2=禁用 3=在线 4=离线 */
    private Integer status;

    /** 所属房间ID */
    private Long roomId;

    /** 网关设备序列号（子设备用） */
    private String gwSerialNumber;

    /** 子设备地址（Modbus等） */
    private Integer slaveId;

    /** 最后上线时间 */
    private LocalDateTime lastOnlineTime;

    /** 最后离线时间 */
    private LocalDateTime lastOfflineTime;
}
