package com.smarthome.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 能耗统计 VO
 */
@Data
public class EnergyStatVO {

    /** 日期 */
    private String date;

    /** 用电量 */
    private BigDecimal value;

    /** 单位 */
    private String unit;

    public EnergyStatVO() {
        this.unit = "kWh";
    }

    public EnergyStatVO(String date, BigDecimal value) {
        this.date = date;
        this.value = value;
        this.unit = "kWh";
    }

    public EnergyStatVO(String date, BigDecimal value, String unit) {
        this.date = date;
        this.value = value;
        this.unit = unit;
    }
}
