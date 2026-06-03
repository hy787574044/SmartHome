package com.smarthome.web.controller;

import com.smarthome.common.result.PageResult;
import com.smarthome.common.result.R;
import com.smarthome.device.service.DeviceService;
import com.smarthome.model.entity.Device;
import com.smarthome.model.entity.DevicePropertyLog;
import com.smarthome.model.vo.DeviceDetailVO;
import com.smarthome.model.vo.DeviceStatusVO;
import com.smarthome.common.annotation.OperationLog;
import com.smarthome.web.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备管理 API
 */
@Tag(name = "设备管理")
@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final ExportService exportService;

    @Operation(summary = "添加设备")
    @OperationLog(module = "device", operation = "创建")
    @PostMapping
    public R<Device> add(@RequestBody Device device) {
        return R.ok(deviceService.addDevice(device));
    }

    @Operation(summary = "更新设备")
    @OperationLog(module = "device", operation = "修改")
    @PostMapping("/update")
    public R<Void> update(@RequestBody Device device) {
        deviceService.updateDevice(device);
        return R.ok();
    }

    @Operation(summary = "删除设备")
    @OperationLog(module = "device", operation = "删除")
    @DeleteMapping("/{deviceId}")
    public R<Void> delete(@PathVariable Long deviceId) {
        deviceService.deleteDevice(deviceId);
        return R.ok();
    }

    @Operation(summary = "获取设备详情")
    @GetMapping("/{deviceId}")
    public R<Device> getById(@PathVariable Long deviceId) {
        return R.ok(deviceService.getById(deviceId));
    }

    @Operation(summary = "分页查询设备")
    @GetMapping("/list")
    public R<PageResult<Device>> list(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(deviceService.listDevices(roomId, productId, status, pageNum, pageSize));
    }

    @Operation(summary = "获取设备状态（含属性值）")
    @GetMapping("/{deviceId}/status")
    public R<DeviceStatusVO> getStatus(@PathVariable Long deviceId) {
        return R.ok(deviceService.getDeviceStatus(deviceId));
    }

    @Operation(summary = "获取设备详情（含产品、物模型、属性值、房间）")
    @GetMapping("/{deviceId}/detail")
    public R<DeviceDetailVO> getDetail(@PathVariable Long deviceId) {
        return R.ok(deviceService.getDeviceDetail(deviceId));
    }

    @Operation(summary = "获取设备属性历史数据")
    @GetMapping("/{deviceId}/history")
    public R<PageResult<DevicePropertyLog>> getHistory(
            @PathVariable Long deviceId,
            @RequestParam(required = false) String identifier,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(deviceService.getDevicePropertyHistory(
                deviceId, identifier, startTime, endTime, pageNum, pageSize));
    }

    @Operation(summary = "按房间获取设备列表")
    @GetMapping("/room/{roomId}")
    public R<List<DeviceStatusVO>> listByRoom(@PathVariable Long roomId) {
        return R.ok(deviceService.listDevicesByRoom(roomId));
    }

    @Operation(summary = "获取在线设备")
    @GetMapping("/online")
    public R<List<Device>> listOnline() {
        return R.ok(deviceService.listOnlineDevices());
    }

    @Operation(summary = "控制设备")
    @OperationLog(module = "device", operation = "控制")
    @PostMapping("/{deviceId}/control")
    public R<Void> control(
            @PathVariable Long deviceId,
            @RequestParam String identifier,
            @RequestParam String value) {
        deviceService.controlDevice(deviceId, identifier, value);
        return R.ok();
    }

    @Operation(summary = "导出设备列表")
    @GetMapping("/export")
    public void exportDeviceList(HttpServletResponse response) throws IOException {
        exportService.exportDeviceList(response);
    }

    @Operation(summary = "导出设备历史数据")
    @GetMapping("/{deviceId}/history/export")
    public void exportDeviceHistory(
            @PathVariable Long deviceId,
            @RequestParam(required = false) String identifier,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            HttpServletResponse response) throws IOException {
        exportService.exportDeviceHistory(deviceId, identifier, startTime, endTime, response);
    }
}
