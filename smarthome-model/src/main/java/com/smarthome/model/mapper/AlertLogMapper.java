package com.smarthome.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthome.model.entity.AlertLog;
import com.smarthome.model.vo.AlertStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AlertLogMapper extends BaseMapper<AlertLog> {

    /**
     * 告警趋势 - 最近N天每天告警数量
     */
    @Select("SELECT DATE(create_time) AS date, COUNT(*) AS count " +
            "FROM alert_log " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY DATE(create_time)")
    List<AlertStatsVO> selectAlertTrend(@Param("days") int days);

    /**
     * 按告警级别统计
     */
    @Select("SELECT alert_level AS level, COUNT(*) AS count " +
            "FROM alert_log " +
            "GROUP BY alert_level " +
            "ORDER BY alert_level")
    List<AlertStatsVO> selectAlertCountByLevel();

    /**
     * 按设备统计告警数量（Top N）
     */
    @Select("SELECT device_id AS deviceId, device_name AS deviceName, COUNT(*) AS count " +
            "FROM alert_log " +
            "GROUP BY device_id, device_name " +
            "ORDER BY count DESC " +
            "LIMIT #{topN}")
    List<Map<String, Object>> selectAlertCountByDevice(@Param("topN") int topN);

    /**
     * 统计今日告警数量
     */
    @Select("SELECT COUNT(*) FROM alert_log WHERE DATE(create_time) = CURDATE()")
    int selectTodayAlertCount();

    /**
     * 统计待处理告警数量
     */
    @Select("SELECT COUNT(*) FROM alert_log WHERE status = #{status}")
    int selectAlertCountByStatus(@Param("status") int status);
}
