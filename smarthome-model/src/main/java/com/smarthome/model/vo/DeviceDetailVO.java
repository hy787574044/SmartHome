package com.smarthome.model.vo;

import com.smarthome.model.entity.Device;
import com.smarthome.model.entity.Product;
import com.smarthome.model.entity.ThingsModel;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 设备详情 VO（含产品信息、物模型、当前属性值、房间信息）
 */
@Data
public class DeviceDetailVO {

    /** 设备信息 */
    private Device device;

    /** 产品信息 */
    private Product product;

    /** 房间名称 */
    private String roomName;

    /** 物模型列表 */
    private List<ThingsModel> thingsModels;

    /** 设备当前属性值 Map<identifier, value> */
    private Map<String, String> properties;
}
