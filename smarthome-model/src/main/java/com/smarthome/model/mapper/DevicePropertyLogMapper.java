package com.smarthome.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthome.model.entity.DevicePropertyLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface DevicePropertyLogMapper extends BaseMapper<DevicePropertyLog> {

    /**
     * 查询某设备某天的用电量（最大值 - 最小值）
     */
    @Select("SELECT " +
            "MAX(CAST(value AS DECIMAL(18,2))) - MIN(CAST(value AS DECIMAL(18,2))) AS energy " +
            "FROM device_property_log " +
            "WHERE device_id = #{deviceId} " +
            "AND identifier = 'electricity' " +
            "AND DATE(create_time) = #{date}")
    BigDecimal getDailyEnergy(@Param("deviceId") Long deviceId, @Param("date") String date);

    /**
     * 查询某设备指定日期范围内的每日用电量
     */
    @Select("SELECT " +
            "DATE(create_time) AS date, " +
            "MAX(CAST(value AS DECIMAL(18,2))) - MIN(CAST(value AS DECIMAL(18,2))) AS energy " +
            "FROM device_property_log " +
            "WHERE device_id = #{deviceId} " +
            "AND identifier = 'electricity' " +
            "AND create_time >= #{startTime} " +
            "AND create_time < #{endTime} " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY DATE(create_time)")
    List<Map<String, Object>> getDailyEnergyList(@Param("deviceId") Long deviceId,
                                                  @Param("startTime") String startTime,
                                                  @Param("endTime") String endTime);

    /**
     * 查询某房间所有设备的总用电量
     */
    @Select("SELECT " +
            "COALESCE(SUM(energy), 0) FROM (" +
            "  SELECT MAX(CAST(value AS DECIMAL(18,2))) - MIN(CAST(value AS DECIMAL(18,2))) AS energy " +
            "  FROM device_property_log " +
            "  WHERE device_id IN (SELECT device_id FROM device WHERE room_id = #{roomId}) " +
            "  AND identifier = 'electricity' " +
            "  AND create_time >= #{startTime} " +
            "  GROUP BY device_id, DATE(create_time)" +
            ") t")
    BigDecimal getRoomTotalEnergy(@Param("roomId") Long roomId, @Param("startTime") String startTime);

    /**
     * 查询设备用电排行（按总用电量降序）
     */
    @Select("SELECT " +
            "d.device_id AS deviceId, " +
            "d.device_name AS deviceName, " +
            "COALESCE(SUM(energy), 0) AS totalEnergy " +
            "FROM device d " +
            "LEFT JOIN (" +
            "  SELECT device_id, " +
            "  MAX(CAST(value AS DECIMAL(18,2))) - MIN(CAST(value AS DECIMAL(18,2))) AS energy " +
            "  FROM device_property_log " +
            "  WHERE identifier = 'electricity' " +
            "  AND create_time >= #{startTime} " +
            "  GROUP BY device_id, DATE(create_time)" +
            ") t ON d.device_id = t.device_id " +
            "GROUP BY d.device_id, d.device_name " +
            "ORDER BY totalEnergy DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getDeviceEnergyRanking(@Param("startTime") String startTime, @Param("limit") Integer limit);
}
