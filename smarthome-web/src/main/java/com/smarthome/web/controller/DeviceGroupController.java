package com.smarthome.web.controller;

import com.smarthome.common.result.R;
import com.smarthome.model.entity.DeviceGroup;
import com.smarthome.model.entity.DeviceGroupDevice;
import com.smarthome.device.service.DeviceGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备分组 API
 */
@Tag(name = "设备分组")
@RestController
@RequestMapping("/api/deviceGroup")
@RequiredArgsConstructor
public class DeviceGroupController {

    private final DeviceGroupService deviceGroupService;

    @Operation(summary = "添加设备分组")
    @PostMapping
    public R<DeviceGroup> add(@RequestBody DeviceGroup group) {
        return R.ok(deviceGroupService.add(group));
    }

    @Operation(summary = "更新设备分组")
    @PostMapping("/update")
    public R<Void> update(@RequestBody DeviceGroup group) {
        deviceGroupService.update(group);
        return R.ok();
    }

    @Operation(summary = "删除设备分组")
    @DeleteMapping("/{groupId}")
    public R<Void> delete(@PathVariable Long groupId) {
        deviceGroupService.delete(groupId);
        return R.ok();
    }

    @Operation(summary = "获取设备分组列表")
    @GetMapping("/list")
    public R<List<DeviceGroup>> list() {
        return R.ok(deviceGroupService.listAll());
    }

    @Operation(summary = "获取分组下的设备")
    @GetMapping("/{groupId}/devices")
    public R<List<DeviceGroupDevice>> listDevices(@PathVariable Long groupId) {
        return R.ok(deviceGroupService.listDevices(groupId));
    }

    @Operation(summary = "添加设备到分组")
    @PostMapping("/{groupId}/devices")
    public R<Void> addDevice(@PathVariable Long groupId, @RequestBody List<Long> deviceIds) {
        deviceGroupService.addDevices(groupId, deviceIds);
        return R.ok();
    }

    @Operation(summary = "从分组移除设备")
    @DeleteMapping("/{groupId}/devices/{deviceId}")
    public R<Void> removeDevice(@PathVariable Long groupId, @PathVariable Long deviceId) {
        deviceGroupService.removeDevice(groupId, deviceId);
        return R.ok();
    }

    @Operation(summary = "批量控制分组设备")
    @PostMapping("/{groupId}/control")
    public R<Void> controlGroup(@PathVariable Long groupId,
                                @RequestParam String identifier,
                                @RequestParam String value) {
        deviceGroupService.controlGroup(groupId, identifier, value);
        return R.ok();
    }
}
