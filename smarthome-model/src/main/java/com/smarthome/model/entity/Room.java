package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthome.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 房间
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room")
public class Room extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long roomId;

    /** 房间名称 */
    private String roomName;

    /** 房间类型: living_room/bedroom/kitchen/bathroom/study/balcony */
    private String roomType;

    /** 楼层 */
    private Integer floor;

    /** 排序 */
    private Integer sortOrder;
}
