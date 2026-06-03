package com.smarthome.web.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.write.style.row.SimpleRowHeightStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthome.model.entity.AlertLog;
import com.smarthome.model.entity.Device;
import com.smarthome.model.entity.DevicePropertyLog;
import com.smarthome.model.entity.OperationLog;
import com.smarthome.model.mapper.AlertLogMapper;
import com.smarthome.model.mapper.DeviceMapper;
import com.smarthome.model.mapper.DevicePropertyLogMapper;
import com.smarthome.model.mapper.OperationLogMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据导出服务 - 使用 EasyExcel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final DeviceMapper deviceMapper;
    private final AlertLogMapper alertLogMapper;
    private final OperationLogMapper operationLogMapper;
    private final DevicePropertyLogMapper devicePropertyLogMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 导出方法 ====================

    /**
     * 导出设备列表
     */
    public void exportDeviceList(HttpServletResponse response) throws IOException {
        List<Device> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<Device>().orderByDesc(Device::getCreateTime)
        );

        List<DeviceExportVO> voList = devices.stream()
                .map(this::toDeviceExportVO)
                .collect(Collectors.toList());

        String fileName = "设备列表_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        writeExcel(response, fileName, DeviceExportVO.class, voList);
        log.info("导出设备列表成功，共 {} 条记录", voList.size());
    }

    /**
     * 导出告警日志
     */
    public void exportAlertLogs(LocalDateTime startTime, LocalDateTime endTime,
                                HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<AlertLog> wrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            wrapper.ge(AlertLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AlertLog::getCreateTime, endTime);
        }
        wrapper.orderByDesc(AlertLog::getCreateTime);

        List<AlertLog> logs = alertLogMapper.selectList(wrapper);

        List<AlertLogExportVO> voList = logs.stream()
                .map(this::toAlertLogExportVO)
                .collect(Collectors.toList());

        String fileName = "告警日志_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        writeExcel(response, fileName, AlertLogExportVO.class, voList);
        log.info("导出告警日志成功，共 {} 条记录", voList.size());
    }

    /**
     * 导出操作日志
     */
    public void exportOperationLogs(LocalDateTime startTime, LocalDateTime endTime,
                                    HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            wrapper.ge(OperationLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperationLog::getCreateTime, endTime);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);

        List<OperationLog> logs = operationLogMapper.selectList(wrapper);

        List<OperationLogExportVO> voList = logs.stream()
                .map(this::toOperationLogExportVO)
                .collect(Collectors.toList());

        String fileName = "操作日志_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        writeExcel(response, fileName, OperationLogExportVO.class, voList);
        log.info("导出操作日志成功，共 {} 条记录", voList.size());
    }

    /**
     * 导出设备历史数据
     */
    public void exportDeviceHistory(Long deviceId, String identifier,
                                    LocalDateTime startTime, LocalDateTime endTime,
                                    HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<DevicePropertyLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DevicePropertyLog::getDeviceId, deviceId);
        if (identifier != null && !identifier.isEmpty()) {
            wrapper.eq(DevicePropertyLog::getIdentifier, identifier);
        }
        if (startTime != null) {
            wrapper.ge(DevicePropertyLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(DevicePropertyLog::getCreateTime, endTime);
        }
        wrapper.orderByDesc(DevicePropertyLog::getCreateTime);

        List<DevicePropertyLog> logs = devicePropertyLogMapper.selectList(wrapper);

        List<DeviceHistoryExportVO> voList = logs.stream()
                .map(this::toDeviceHistoryExportVO)
                .collect(Collectors.toList());

        String fileName = "设备历史数据_" + deviceId + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        writeExcel(response, fileName, DeviceHistoryExportVO.class, voList);
        log.info("导出设备历史数据成功，设备ID: {}，共 {} 条记录", deviceId, voList.size());
    }

    // ==================== 写入Excel工具方法 ====================

    private <T> void writeExcel(HttpServletResponse response, String fileName,
                                Class<T> clazz, List<T> data) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), clazz)
                .registerWriteHandler(new SimpleRowHeightStyleStrategy((short) 20, (short) 16))
                .sheet("Sheet1")
                .doWrite(data);
    }

    // ==================== VO 转换方法 ====================

    private DeviceExportVO toDeviceExportVO(Device device) {
        DeviceExportVO vo = new DeviceExportVO();
        vo.setDeviceId(device.getDeviceId());
        vo.setDeviceName(device.getDeviceName());
        vo.setProductId(device.getProductId());
        vo.setSerialNumber(device.getSerialNumber());
        vo.setDeviceType(getDeviceTypeName(device.getDeviceType()));
        vo.setStatus(getDeviceStatusName(device.getStatus()));
        vo.setRoomId(device.getRoomId());
        vo.setGwSerialNumber(device.getGwSerialNumber());
        vo.setSlaveId(device.getSlaveId());
        vo.setLastOnlineTime(device.getLastOnlineTime() != null ?
                device.getLastOnlineTime().format(DATE_FORMATTER) : "");
        vo.setLastOfflineTime(device.getLastOfflineTime() != null ?
                device.getLastOfflineTime().format(DATE_FORMATTER) : "");
        vo.setCreateTime(device.getCreateTime() != null ?
                device.getCreateTime().format(DATE_FORMATTER) : "");
        return vo;
    }

    private AlertLogExportVO toAlertLogExportVO(AlertLog log) {
        AlertLogExportVO vo = new AlertLogExportVO();
        vo.setLogId(log.getLogId());
        vo.setAlertId(log.getAlertId());
        vo.setDeviceId(log.getDeviceId());
        vo.setDeviceName(log.getDeviceName());
        vo.setAlertValue(log.getAlertValue());
        vo.setAlertLevel(getAlertLevelName(log.getAlertLevel()));
        vo.setAlertMessage(log.getAlertMessage());
        vo.setStatus(getAlertStatusName(log.getStatus()));
        vo.setCreateTime(log.getCreateTime() != null ?
                log.getCreateTime().format(DATE_FORMATTER) : "");
        vo.setHandleTime(log.getHandleTime() != null ?
                log.getHandleTime().format(DATE_FORMATTER) : "");
        vo.setHandleRemark(log.getHandleRemark());
        return vo;
    }

    private OperationLogExportVO toOperationLogExportVO(OperationLog log) {
        OperationLogExportVO vo = new OperationLogExportVO();
        vo.setLogId(log.getLogId());
        vo.setUserId(log.getUserId());
        vo.setUsername(log.getUsername());
        vo.setModule(log.getModule());
        vo.setOperation(log.getOperation());
        vo.setTarget(log.getTarget());
        vo.setDetail(log.getDetail());
        vo.setIp(log.getIp());
        vo.setStatus(log.getStatus() == 1 ? "成功" : "失败");
        vo.setCreateTime(log.getCreateTime() != null ?
                log.getCreateTime().format(DATE_FORMATTER) : "");
        return vo;
    }

    private DeviceHistoryExportVO toDeviceHistoryExportVO(DevicePropertyLog log) {
        DeviceHistoryExportVO vo = new DeviceHistoryExportVO();
        vo.setLogId(log.getLogId());
        vo.setDeviceId(log.getDeviceId());
        vo.setIdentifier(log.getIdentifier());
        vo.setValue(log.getValue());
        vo.setCreateTime(log.getCreateTime() != null ?
                log.getCreateTime().format(DATE_FORMATTER) : "");
        return vo;
    }

    // ==================== 状态名称转换 ====================

    private String getDeviceTypeName(Integer type) {
        if (type == null) return "";
        switch (type) {
            case 1: return "直连设备";
            case 2: return "网关设备";
            case 3: return "监测设备";
            default: return "未知";
        }
    }

    private String getDeviceStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "未激活";
            case 2: return "禁用";
            case 3: return "在线";
            case 4: return "离线";
            default: return "未知";
        }
    }

    private String getAlertLevelName(Integer level) {
        if (level == null) return "";
        switch (level) {
            case 1: return "低";
            case 2: return "中";
            case 3: return "高";
            default: return "未知";
        }
    }

    private String getAlertStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "未处理";
            case 2: return "已处理";
            case 3: return "已忽略";
            default: return "未知";
        }
    }

    // ==================== EasyExcel 导出数据类 ====================

    /**
     * 设备列表导出 VO
     */
    @Data
    public static class DeviceExportVO {
        @ExcelProperty("设备ID")
        @ColumnWidth(12)
        private Long deviceId;

        @ExcelProperty("设备名称")
        @ColumnWidth(20)
        private String deviceName;

        @ExcelProperty("产品ID")
        @ColumnWidth(12)
        private Long productId;

        @ExcelProperty("设备序列号")
        @ColumnWidth(25)
        private String serialNumber;

        @ExcelProperty("设备类型")
        @ColumnWidth(12)
        private String deviceType;

        @ExcelProperty("设备状态")
        @ColumnWidth(10)
        private String status;

        @ExcelProperty("房间ID")
        @ColumnWidth(10)
        private Long roomId;

        @ExcelProperty("网关序列号")
        @ColumnWidth(25)
        private String gwSerialNumber;

        @ExcelProperty("从机地址")
        @ColumnWidth(10)
        private Integer slaveId;

        @ExcelProperty("最后上线时间")
        @ColumnWidth(20)
        private String lastOnlineTime;

        @ExcelProperty("最后离线时间")
        @ColumnWidth(20)
        private String lastOfflineTime;

        @ExcelProperty("创建时间")
        @ColumnWidth(20)
        private String createTime;
    }

    /**
     * 告警日志导出 VO
     */
    @Data
    public static class AlertLogExportVO {
        @ExcelProperty("日志ID")
        @ColumnWidth(12)
        private Long logId;

        @ExcelProperty("规则ID")
        @ColumnWidth(12)
        private Long alertId;

        @ExcelProperty("设备ID")
        @ColumnWidth(12)
        private Long deviceId;

        @ExcelProperty("设备名称")
        @ColumnWidth(20)
        private String deviceName;

        @ExcelProperty("告警值")
        @ColumnWidth(15)
        private String alertValue;

        @ExcelProperty("告警级别")
        @ColumnWidth(10)
        private String alertLevel;

        @ExcelProperty("告警消息")
        @ColumnWidth(40)
        private String alertMessage;

        @ExcelProperty("状态")
        @ColumnWidth(10)
        private String status;

        @ExcelProperty("创建时间")
        @ColumnWidth(20)
        private String createTime;

        @ExcelProperty("处理时间")
        @ColumnWidth(20)
        private String handleTime;

        @ExcelProperty("处理备注")
        @ColumnWidth(30)
        private String handleRemark;
    }

    /**
     * 操作日志导出 VO
     */
    @Data
    public static class OperationLogExportVO {
        @ExcelProperty("日志ID")
        @ColumnWidth(12)
        private Long logId;

        @ExcelProperty("用户ID")
        @ColumnWidth(12)
        private Long userId;

        @ExcelProperty("操作用户")
        @ColumnWidth(15)
        private String username;

        @ExcelProperty("操作模块")
        @ColumnWidth(12)
        private String module;

        @ExcelProperty("操作类型")
        @ColumnWidth(12)
        private String operation;

        @ExcelProperty("操作目标")
        @ColumnWidth(20)
        private String target;

        @ExcelProperty("操作详情")
        @ColumnWidth(40)
        private String detail;

        @ExcelProperty("操作IP")
        @ColumnWidth(15)
        private String ip;

        @ExcelProperty("状态")
        @ColumnWidth(10)
        private String status;

        @ExcelProperty("操作时间")
        @ColumnWidth(20)
        private String createTime;
    }

    /**
     * 设备历史数据导出 VO
     */
    @Data
    public static class DeviceHistoryExportVO {
        @ExcelProperty("日志ID")
        @ColumnWidth(12)
        private Long logId;

        @ExcelProperty("设备ID")
        @ColumnWidth(12)
        private Long deviceId;

        @ExcelProperty("属性标识符")
        @ColumnWidth(20)
        private String identifier;

        @ExcelProperty("属性值")
        @ColumnWidth(20)
        private String value;

        @ExcelProperty("记录时间")
        @ColumnWidth(20)
        private String createTime;
    }
}
