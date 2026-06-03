package com.smarthome.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthome.common.constant.Constants;
import com.smarthome.common.exception.BusinessException;
import com.smarthome.common.result.PageResult;
import com.smarthome.common.utils.RedisUtils;
import com.smarthome.model.dto.DevicePropertyDTO;
import com.smarthome.model.entity.Device;
import com.smarthome.model.entity.DevicePropertyLog;
import com.smarthome.model.entity.Product;
import com.smarthome.model.entity.ThingsModel;
import com.smarthome.model.mapper.DeviceMapper;
import com.smarthome.model.mapper.DevicePropertyLogMapper;
import com.smarthome.model.mapper.ThingsModelMapper;
import com.smarthome.model.vo.DeviceStatusVO;
import com.smarthome.mqtt.handler.MqttMessageHandler;
import com.smarthome.mqtt.service.MqttSendService;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceMapper deviceMapper;
    private final ThingsModelMapper thingsModelMapper;
    private final DevicePropertyLogMapper propertyLogMapper;
    private final RedisUtils redisUtils;
    private final MqttSendService mqttSendService;
    private final MqttMessageHandler mqttMessageHandler;
    private final ApplicationEventPublisher eventPublisher;
    private final ProductService productService;

    /**
     * 初始化：注册 MQTT 消息监听
     */
    @PostConstruct
    public void init() {
        mqttMessageHandler.addPropertyListener("property/post", this::onPropertyReport);
        mqttMessageHandler.addStatusListener(this::onDeviceStatusChange);
        log.info("设备服务已初始化，MQTT 监听已注册");
    }

    /**
     * 添加设备
     */
    public Device addDevice(Device device) {
        Device existing = getBySerialNumber(device.getSerialNumber());
        if (existing != null) {
            throw new BusinessException("设备序列号已存在: " + device.getSerialNumber());
        }
        device.setStatus(1);
        deviceMapper.insert(device);
        initDevicePropertiesCache(device);
        return device;
    }

    /**
     * 更新设备
     */
    public void updateDevice(Device device) {
        deviceMapper.updateById(device);
    }

    /**
     * 删除设备
     */
    public void deleteDevice(Long deviceId) {
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        deviceMapper.deleteById(deviceId);
        redisUtils.delete(Constants.REDIS_DEVICE_PROPERTIES + device.getSerialNumber());
        redisUtils.delete(Constants.REDIS_DEVICE_STATUS + device.getSerialNumber());
    }

    /**
     * 获取设备详情
     */
    public Device getById(Long deviceId) {
        return deviceMapper.selectById(deviceId);
    }

    /**
     * 根据序列号获取设备
     */
    public Device getBySerialNumber(String serialNumber) {
        return deviceMapper.selectOne(
                new LambdaQueryWrapper<Device>().eq(Device::getSerialNumber, serialNumber)
        );
    }

    /**
     * 分页查询设备
     */
    public PageResult<Device> listDevices(Long roomId, Long productId, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        if (roomId != null) {
            wrapper.eq(Device::getRoomId, roomId);
        }
        if (productId != null) {
            wrapper.eq(Device::getProductId, productId);
        }
        if (status != null) {
            wrapper.eq(Device::getStatus, status);
        }
        wrapper.orderByDesc(Device::getCreateTime);
        Page<Device> page = deviceMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    /**
     * 获取设备状态（含属性值）
     */
    public DeviceStatusVO getDeviceStatus(Long deviceId) {
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        return buildDeviceStatusVO(device);
    }

    /**
     * 获取所有在线设备
     */
    public List<Device> listOnlineDevices() {
        return deviceMapper.selectList(
                new LambdaQueryWrapper<Device>().eq(Device::getStatus, Constants.DEVICE_ONLINE)
        );
    }

    /**
     * 按房间获取设备列表（含状态）
     */
    public List<DeviceStatusVO> listDevicesByRoom(Long roomId) {
        List<Device> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<Device>()
                        .eq(roomId != null, Device::getRoomId, roomId)
                        .orderByAsc(Device::getDeviceName)
        );
        return devices.stream().map(this::buildDeviceStatusVO).collect(Collectors.toList());
    }

    /**
     * 控制设备（发送功能指令）
     */
    public void controlDevice(Long deviceId, String identifier, String value) {
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        if (device.getStatus() != Constants.DEVICE_ONLINE) {
            throw new BusinessException("设备不在线，无法控制");
        }
        Product product = productService.getById(device.getProductId());
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        mqttSendService.sendPropertyControl(
                String.valueOf(product.getProductId()),
                device.getSerialNumber(),
                identifier,
                value
        );
        log.info("设备控制指令已发送 - 设备: {}, {}={}", device.getDeviceName(), identifier, value);
    }

    /**
     * 属性上报回调
     */
    private void onPropertyReport(String serialNumber, List<DevicePropertyDTO> properties) {
        Device device = getBySerialNumber(serialNumber);
        if (device == null) {
            log.warn("收到未知设备的属性上报: {}", serialNumber);
            return;
        }

        // 更新 Redis 缓存
        String redisKey = Constants.REDIS_DEVICE_PROPERTIES + serialNumber;
        for (DevicePropertyDTO prop : properties) {
            redisUtils.hSet(redisKey, prop.getId(), prop.getValue());

            // 保存属性历史记录
            DevicePropertyLog propertyLog = new DevicePropertyLog();
            propertyLog.setDeviceId(device.getDeviceId());
            propertyLog.setIdentifier(prop.getId());
            propertyLog.setValue(prop.getValue());
            propertyLog.setCreateTime(LocalDateTime.now());
            propertyLogMapper.insert(propertyLog);
        }

        // 确保设备在线状态
        if (device.getStatus() != Constants.DEVICE_ONLINE) {
            device.setStatus(Constants.DEVICE_ONLINE);
            device.setLastOnlineTime(LocalDateTime.now());
            deviceMapper.updateById(device);
            redisUtils.set(Constants.REDIS_DEVICE_STATUS + serialNumber, "online");
        }

        // 发布 Spring 事件（供场景、告警等模块监听）
        eventPublisher.publishEvent(new DevicePropertyEvent(device, properties));

        log.debug("设备属性已更新 - 设备: {}, 属性数: {}", serialNumber, properties.size());
    }

    /**
     * 设备状态变更回调
     */
    private void onDeviceStatusChange(String serialNumber, boolean online) {
        Device device = getBySerialNumber(serialNumber);
        if (device == null) {
            return;
        }

        int newStatus = online ? Constants.DEVICE_ONLINE : Constants.DEVICE_OFFLINE;
        if (device.getStatus().equals(newStatus)) {
            return;
        }

        device.setStatus(newStatus);
        if (online) {
            device.setLastOnlineTime(LocalDateTime.now());
        } else {
            device.setLastOfflineTime(LocalDateTime.now());
        }
        deviceMapper.updateById(device);
        redisUtils.set(Constants.REDIS_DEVICE_STATUS + serialNumber, online ? "online" : "offline");

        // 发布状态变更事件
        eventPublisher.publishEvent(new DeviceStatusEvent(device, online));

        log.info("设备状态变更 - 设备: {}, 状态: {}", serialNumber, online ? "在线" : "离线");
    }

    /**
     * 初始化设备属性缓存
     */
    private void initDevicePropertiesCache(Device device) {
        List<ThingsModel> models = thingsModelMapper.selectList(
                new LambdaQueryWrapper<ThingsModel>()
                        .eq(ThingsModel::getProductId, device.getProductId())
                        .eq(ThingsModel::getType, Constants.MODEL_TYPE_PROPERTY)
        );
        String redisKey = Constants.REDIS_DEVICE_PROPERTIES + device.getSerialNumber();
        for (ThingsModel model : models) {
            redisUtils.hSet(redisKey, model.getIdentifier(), "");
        }
    }

    /**
     * 构建设备状态 VO
     */
    private DeviceStatusVO buildDeviceStatusVO(Device device) {
        DeviceStatusVO vo = new DeviceStatusVO();
        vo.setDeviceId(device.getDeviceId());
        vo.setDeviceName(device.getDeviceName());
        vo.setSerialNumber(device.getSerialNumber());
        vo.setProductId(device.getProductId());
        vo.setRoomId(device.getRoomId());
        vo.setStatus(device.getStatus());

        // 获取产品名称
        Product product = productService.getById(device.getProductId());
        if (product != null) {
            vo.setProductName(product.getProductName());
        }

        // 从 Redis 获取属性值
        String redisKey = Constants.REDIS_DEVICE_PROPERTIES + device.getSerialNumber();
        Object props = redisUtils.hGetAll(redisKey);
        if (props instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> properties = (Map<String, String>) (Map<?, ?>) props;
            vo.setProperties(properties);
        }
        return vo;
    }

    /**
     * 设备属性上报事件
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class DevicePropertyEvent {
        private Device device;
        private List<DevicePropertyDTO> properties;
    }

    /**
     * 设备状态变更事件
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class DeviceStatusEvent {
        private Device device;
        private boolean online;
    }
}
