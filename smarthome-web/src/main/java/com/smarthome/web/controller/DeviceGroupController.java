package com.smarthome.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthome.common.exception.BusinessException;
import com.smarthome.common.result.R;
import com.smarthome.device.service.DeviceService;
import com.smarthome.model.entity.DeviceGroup;
import com.smarthome.model.entity.DeviceGroupDevice;
import com.smarthome.model.mapper.DeviceGroupDeviceMapper;
import com.smarthome.model.mapper.DeviceGroupMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备分组管理 API
 */
@Tag(name = "设备分组")
@RestController
@RequestMapping("/api/deviceGroup")
@RequiredArgsConstructor
public class DeviceGroupController {

    private final DeviceGroupMapper deviceGroupMapper;
    private final DeviceGroupDeviceMapper deviceGroupDeviceMapper;
    private final DeviceService deviceService;

    @Operation(summary = "添加分组")
    @PostMapping
    public R<DeviceGroup> add(@RequestBody DeviceGroup group) {
        deviceGroupMapper.insert(group);
        return R.ok(group);
    }

    @Operation(summary = "更新分组")
    @PutMapping
    public R<Void> update(@RequestBody DeviceGroup group) {
        deviceGroupMapper.updateById(group);
        return R.ok();
    }

    @Operation(summary = "删除分组")
    @DeleteMapping("/{groupId}")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> delete(@PathVariable Long groupId) {
        deviceGroupMapper.deleteById(groupId);
        deviceGroupDeviceMapper.delete(
                new LambdaQueryWrapper<DeviceGroupDevice>().eq(DeviceGroupDevice::getGroupId, groupId));
        return R.ok();
    }

    @Operation(summary = "获取分组详情")
    @GetMapping("/{groupId}")
    public R<DeviceGroup> getById(@PathVariable Long groupId) {
        return R.ok(deviceGroupMapper.selectById(groupId));
    }

    @Operation(summary = "获取所有分组")
    @GetMapping("/list")
    public R<List<DeviceGroup>> list() {
        return R.ok(deviceGroupMapper.selectList(
                new LambdaQueryWrapper<DeviceGroup>().orderByAsc(DeviceGroup::getSortOrder)));
    }

    @Operation(summary = "获取分组下的设备ID列表")
    @GetMapping("/{groupId}/devices")
    public R<List<DeviceGroupDevice>> listDevices(@PathVariable Long groupId) {
        return R.ok(deviceGroupDeviceMapper.selectList(
                new LambdaQueryWrapper<DeviceGroupDevice>().eq(DeviceGroupDevice::getGroupId, groupId)));
    }

    @Operation(summary = "向分组添加设备")
    @PostMapping("/{groupId}/devices")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> addDevices(@PathVariable Long groupId, @RequestBody List<Long> deviceIds) {
        for (Long deviceId : deviceIds) {
            long count = deviceGroupDeviceMapper.selectCount(
                    new LambdaQueryWrapper<DeviceGroupDevice>()
                            .eq(DeviceGroupDevice::getGroupId, groupId)
                            .eq(DeviceGroupDevice::getDeviceId, deviceId));
            if (count == 0) {
                DeviceGroupDevice link = new DeviceGroupDevice();
                link.setGroupId(groupId);
                link.setDeviceId(deviceId);
                deviceGroupDeviceMapper.insert(link);
            }
        }
        return R.ok();
    }

    @Operation(summary = "从分组移除设备")
    @DeleteMapping("/{groupId}/devices/{deviceId}")
    public R<Void> removeDevice(@PathVariable Long groupId, @PathVariable Long deviceId) {
        deviceGroupDeviceMapper.delete(
                new LambdaQueryWrapper<DeviceGroupDevice>()
                        .eq(DeviceGroupDevice::getGroupId, groupId)
                        .eq(DeviceGroupDevice::getDeviceId, deviceId));
        return R.ok();
    }

    @Operation(summary = "批量控制分组内设备")
    @PostMapping("/{groupId}/control")
    public R<Void> controlGroup(@PathVariable Long groupId, @RequestBody GroupControlRequest request) {
        DeviceGroup group = deviceGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("分组不存在");
        }
        List<DeviceGroupDevice> links = deviceGroupDeviceMapper.selectList(
                new LambdaQueryWrapper<DeviceGroupDevice>().eq(DeviceGroupDevice::getGroupId, groupId));
        for (DeviceGroupDevice link : links) {
            deviceService.controlDevice(link.getDeviceId(), request.getIdentifier(), request.getValue());
        }
        return R.ok();
    }

    /**
     * 分组控制请求体
     */
    @lombok.Data
    public static class GroupControlRequest {
        /** 物模型标识符 */
        private String identifier;
        /** 控制值 */
        private String value;
    }
}
