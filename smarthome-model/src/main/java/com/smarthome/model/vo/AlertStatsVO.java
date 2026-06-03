package com.smarthome.model.vo;

import lombok.Data;

/**
 * 告警统计 VO
 */
@Data
public class AlertStatsVO {

    /** 日期 (yyyy-MM-dd) */
    private String date;

    /** 数量 */
    private Integer count;

    /** 告警级别 */
    private Integer level;

    public AlertStatsVO() {
    }

    public AlertStatsVO(String date, Integer count) {
        this.date = date;
        this.count = count;
    }

    public AlertStatsVO(Integer level, Integer count) {
        this.level = level;
        this.count = count;
    }
}
