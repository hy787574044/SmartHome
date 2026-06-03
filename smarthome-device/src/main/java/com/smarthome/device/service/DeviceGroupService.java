package com.smarthome.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthome.common.exception.BusinessException;
import com.smarthome.model.entity.Device;
import com.smarthome.model.entity.DeviceGroup;
import com.smarthome.model.entity.DeviceGroupDevice;
import com.smarthome.model.mapper.DeviceGroupDeviceMapper;
import com.smarthome.model.mapper.DeviceGroupMapper;
import com.smarthome.model.mapper.DeviceMapper;
import com.smarthome.mqtt.service.MqttSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 设备分组服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceGroupService {

    private final DeviceGroupMapper groupMapper;
    private final DeviceGroupDeviceMapper groupDeviceMapper;
    private final DeviceMapper deviceMapper;
    private final MqttSendService mqttSendService;
    private final ProductService productService;

    public DeviceGroup add(DeviceGroup group) {
        groupMapper.insert(group);
        return group;
    }

    public void update(DeviceGroup group) {
        groupMapper.updateById(group);
    }

    public void delete(Long groupId) {
        groupMapper.deleteById(groupId);
        groupDeviceMapper.delete(
                new LambdaQueryWrapper<DeviceGroupDevice>()
                        .eq(DeviceGroupDevice::getGroupId, groupId)
        );
    }

    public List<DeviceGroup> listAll() {
        return groupMapper.selectList(
                new LambdaQueryWrapper<DeviceGroup>().orderByAsc(DeviceGroup::getSortOrder)
        );
    }

    public List<DeviceGroupDevice> listDevices(Long groupId) {
        return groupDeviceMapper.selectList(
                new LambdaQueryWrapper<DeviceGroupDevice>()
                        .eq(DeviceGroupDevice::getGroupId, groupId)
        );
    }

    public void addDevices(Long groupId, List<Long> deviceIds) {
        for (Long deviceId : deviceIds) {
            // 检查是否已存在
            Long count = groupDeviceMapper.selectCount(
                    new LambdaQueryWrapper<DeviceGroupDevice>()
                            .eq(DeviceGroupDevice::getGroupId, groupId)
                            .eq(DeviceGroupDevice::getDeviceId, deviceId)
            );
            if (count == 0) {
                DeviceGroupDevice gd = new DeviceGroupDevice();
                gd.setGroupId(groupId);
                gd.setDeviceId(deviceId);
                groupDeviceMapper.insert(gd);
            }
        }
    }

    public void removeDevice(Long groupId, Long deviceId) {
        groupDeviceMapper.delete(
                new LambdaQueryWrapper<DeviceGroupDevice>()
                        .eq(DeviceGroupDevice::getGroupId, groupId)
                        .eq(DeviceGroupDevice::getDeviceId, deviceId)
        );
    }

    public void controlGroup(Long groupId, String identifier, String value) {
        List<DeviceGroupDevice> groupDevices = listDevices(groupId);
        for (DeviceGroupDevice gd : groupDevices) {
            Device device = deviceMapper.selectById(gd.getDeviceId());
            if (device != null && device.getStatus() == 3) { // 在线
                try {
                    com.smarthome.model.entity.Product product = productService.getById(device.getProductId());
                    if (product != null) {
                        mqttSendService.sendPropertyControl(
                                String.valueOf(product.getProductId()),
                                device.getSerialNumber(),
                                identifier,
                                value
                        );
                    }
                } catch (Exception e) {
                    log.error("分组控制失败 - deviceId: {}", device.getDeviceId(), e);
                }
            }
        }
        log.info("分组控制完成 - groupId: {}, {}={}", groupId, identifier, value);
    }
}
