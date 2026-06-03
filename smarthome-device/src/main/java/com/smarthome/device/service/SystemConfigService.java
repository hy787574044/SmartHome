package com.smarthome.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthome.model.entity.AlertLog;
import com.smarthome.model.entity.Device;
import com.smarthome.model.entity.SystemConfig;
import com.smarthome.model.mapper.AlertLogMapper;
import com.smarthome.model.mapper.DeviceMapper;
import com.smarthome.model.mapper.SystemConfigMapper;
import com.smarthome.model.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;
    private final DeviceMapper deviceMapper;
    private final UserMapper userMapper;
    private final AlertLogMapper alertLogMapper;

    /**
     * 根据key获取配置值
     */
    public String getConfig(String key) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key)
        );
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 批量获取配置
     */
    public Map<String, String> getConfigs(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SystemConfig> configs = systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>().in(SystemConfig::getConfigKey, keys)
        );
        return configs.stream().collect(
                Collectors.toMap(SystemConfig::getConfigKey, SystemConfig::getConfigValue, (a, b) -> b)
        );
    }

    /**
     * 获取所有配置
     */
    public List<SystemConfig> listAll() {
        return systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>().orderByAsc(SystemConfig::getConfigId)
        );
    }

    /**
     * 更新配置（批量）
     */
    public void updateConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            SystemConfig config = systemConfigMapper.selectOne(
                    new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, entry.getKey())
            );
            if (config != null) {
                config.setConfigValue(entry.getValue());
                systemConfigMapper.updateById(config);
            } else {
                config = new SystemConfig();
                config.setConfigKey(entry.getKey());
                config.setConfigValue(entry.getValue());
                config.setConfigName(entry.getKey());
                systemConfigMapper.insert(config);
            }
        }
        log.info("系统配置已更新: keys={}", configs.keySet());
    }

    /**
     * 获取系统信息
     */
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();

        // 版本信息
        info.put("systemName", getConfig("system.name"));
        info.put("version", getConfig("system.version"));

        // 运行时间
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        long days = uptimeMs / (1000 * 60 * 60 * 24);
        long hours = (uptimeMs / (1000 * 60 * 60)) % 24;
        long minutes = (uptimeMs / (1000 * 60)) % 60;
        info.put("uptime", String.format("%d天%d小时%d分钟", days, hours, minutes));
        info.put("startTime", ManagementFactory.getRuntimeMXBean().getStartTime());

        // 设备统计
        long totalDevices = deviceMapper.selectCount(null);
        long onlineDevices = deviceMapper.selectCount(
                new LambdaQueryWrapper<Device>().eq(Device::getStatus, 3)
        );
        info.put("totalDevices", totalDevices);
        info.put("onlineDevices", onlineDevices);

        // 用户数
        long totalUsers = userMapper.selectCount(null);
        info.put("totalUsers", totalUsers);

        // 告警统计
        long totalAlerts = alertLogMapper.selectCount(null);
        long pendingAlerts = alertLogMapper.selectCount(
                new LambdaQueryWrapper<AlertLog>().eq(AlertLog::getStatus, 1)
        );
        info.put("totalAlerts", totalAlerts);
        info.put("pendingAlerts", pendingAlerts);

        // JVM信息
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("maxMemory", runtime.maxMemory() / (1024 * 1024) + " MB");
        jvm.put("totalMemory", runtime.totalMemory() / (1024 * 1024) + " MB");
        jvm.put("freeMemory", runtime.freeMemory() / (1024 * 1024) + " MB");
        jvm.put("usedMemory", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + " MB");
        jvm.put("availableProcessors", runtime.availableProcessors());
        info.put("jvm", jvm);

        return info;
    }

    /**
     * 清理过期日志
     */
    public int cleanExpiredLogs(int days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        int deleted = alertLogMapper.delete(
                new LambdaQueryWrapper<AlertLog>()
                        .le(AlertLog::getCreateTime, expireTime)
                        .ne(AlertLog::getStatus, 1)  // 不删除未处理的
        );
        log.info("已清理{}天前的告警日志, 删除{}条", days, deleted);
        return deleted;
    }
}
