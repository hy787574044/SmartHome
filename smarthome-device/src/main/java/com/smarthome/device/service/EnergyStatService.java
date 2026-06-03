package com.smarthome.device.service;

import com.smarthome.model.mapper.DevicePropertyLogMapper;
import com.smarthome.model.vo.EnergyStatVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 能耗统计服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnergyStatService {

    private final DevicePropertyLogMapper propertyLogMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 获取某设备某天的用电量
     */
    public EnergyStatVO getDailyEnergy(Long deviceId, String date) {
        BigDecimal energy = propertyLogMapper.getDailyEnergy(deviceId, date);
        return new EnergyStatVO(date, formatEnergy(energy));
    }

    /**
     * 获取某设备最近7天用电量
     */
    public List<EnergyStatVO> getWeeklyEnergy(Long deviceId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        return getDailyEnergyList(deviceId, startDate, endDate);
    }

    /**
     * 获取某设备最近30天用电量
     */
    public List<EnergyStatVO> getMonthlyEnergy(Long deviceId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);
        return getDailyEnergyList(deviceId, startDate, endDate);
    }

    /**
     * 获取某房间总用电量（最近30天）
     */
    public EnergyStatVO getTotalEnergy(Long roomId) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(30).with(LocalTime.MIN);
        BigDecimal energy = propertyLogMapper.getRoomTotalEnergy(roomId, startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return new EnergyStatVO("最近30天", formatEnergy(energy));
    }

    /**
     * 获取设备用电排行（Top10，默认最近30天）
     */
    public List<Map<String, Object>> getEnergyRanking() {
        return getEnergyRanking(10);
    }

    /**
     * 获取设备用电排行
     */
    public List<Map<String, Object>> getEnergyRanking(int limit) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(30).with(LocalTime.MIN);
        List<Map<String, Object>> ranking = propertyLogMapper.getDeviceEnergyRanking(
                startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                limit
        );
        // 格式化电量值
        for (Map<String, Object> item : ranking) {
            Object energyObj = item.get("totalEnergy");
            if (energyObj instanceof BigDecimal) {
                item.put("totalEnergy", formatEnergy((BigDecimal) energyObj));
            }
        }
        return ranking;
    }

    /**
     * 获取总用电概览（今日/本周/本月）
     */
    public Map<String, Object> getEnergySummary() {
        LocalDateTime now = LocalDateTime.now();

        // 今日用电（所有设备）
        LocalDateTime todayStart = now.with(LocalTime.MIN);
        BigDecimal todayEnergy = sumAllDeviceEnergy(todayStart, now);

        // 本周用电
        LocalDateTime weekStart = now.minusDays(6).with(LocalTime.MIN);
        BigDecimal weekEnergy = sumAllDeviceEnergy(weekStart, now);

        // 本月用电
        LocalDateTime monthStart = now.minusDays(29).with(LocalTime.MIN);
        BigDecimal monthEnergy = sumAllDeviceEnergy(monthStart, now);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("today", formatEnergy(todayEnergy));
        result.put("week", formatEnergy(weekEnergy));
        result.put("month", formatEnergy(monthEnergy));
        result.put("unit", "kWh");
        return result;
    }

    /**
     * 获取指定日期范围内的每日用电列表
     */
    private List<EnergyStatVO> getDailyEnergyList(Long deviceId, LocalDate startDate, LocalDate endDate) {
        String startTime = startDate.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endTime = endDate.plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<Map<String, Object>> energyList = propertyLogMapper.getDailyEnergyList(deviceId, startTime, endTime);

        // 转换为 Map<date, energy> 方便查找
        java.util.Map<String, BigDecimal> energyMap = new java.util.HashMap<>();
        for (Map<String, Object> item : energyList) {
            String date = String.valueOf(item.get("date"));
            Object energyObj = item.get("energy");
            if (energyObj != null) {
                energyMap.put(date, new BigDecimal(energyObj.toString()));
            }
        }

        // 填充所有日期（包括没有数据的日期）
        List<EnergyStatVO> result = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateStr = current.format(DATE_FORMATTER);
            BigDecimal energy = energyMap.getOrDefault(dateStr, BigDecimal.ZERO);
            result.add(new EnergyStatVO(dateStr, formatEnergy(energy)));
            current = current.plusDays(1);
        }

        return result;
    }

    /**
     * 汇总所有设备在指定时间范围内的总用电量
     */
    private BigDecimal sumAllDeviceEnergy(LocalDateTime startTime, LocalDateTime endTime) {
        // 使用自定义查询获取所有设备的总用电量
        List<Map<String, Object>> ranking = propertyLogMapper.getDeviceEnergyRanking(
                startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                1000 // 获取所有设备
        );
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> item : ranking) {
            Object energyObj = item.get("totalEnergy");
            if (energyObj != null) {
                total = total.add(new BigDecimal(energyObj.toString()));
            }
        }
        return total;
    }

    /**
     * 格式化电量值（保留2位小数）
     */
    private BigDecimal formatEnergy(BigDecimal energy) {
        if (energy == null) {
            return BigDecimal.ZERO;
        }
        return energy.setScale(2, RoundingMode.HALF_UP);
    }
}
