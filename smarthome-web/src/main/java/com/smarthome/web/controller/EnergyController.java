package com.smarthome.web.controller;

import com.smarthome.common.result.R;
import com.smarthome.device.service.EnergyStatService;
import com.smarthome.model.vo.EnergyStatVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 能耗统计 API
 */
@Tag(name = "能耗统计")
@RestController
@RequestMapping("/api/energy")
@RequiredArgsConstructor
public class EnergyController {

    private final EnergyStatService energyStatService;

    @Operation(summary = "获取某设备每日用电")
    @GetMapping("/device/{deviceId}/daily")
    public R<EnergyStatVO> getDailyEnergy(
            @PathVariable Long deviceId,
            @RequestParam(required = false) String date) {
        if (date == null || date.isEmpty()) {
            date = java.time.LocalDate.now().toString();
        }
        return R.ok(energyStatService.getDailyEnergy(deviceId, date));
    }

    @Operation(summary = "获取某设备每周用电（最近7天）")
    @GetMapping("/device/{deviceId}/weekly")
    public R<List<EnergyStatVO>> getWeeklyEnergy(@PathVariable Long deviceId) {
        return R.ok(energyStatService.getWeeklyEnergy(deviceId));
    }

    @Operation(summary = "获取某设备每月用电（最近30天）")
    @GetMapping("/device/{deviceId}/monthly")
    public R<List<EnergyStatVO>> getMonthlyEnergy(@PathVariable Long deviceId) {
        return R.ok(energyStatService.getMonthlyEnergy(deviceId));
    }

    @Operation(summary = "获取某房间用电统计")
    @GetMapping("/room/{roomId}")
    public R<EnergyStatVO> getRoomEnergy(@PathVariable Long roomId) {
        return R.ok(energyStatService.getTotalEnergy(roomId));
    }

    @Operation(summary = "设备用电排行（Top10）")
    @GetMapping("/ranking")
    public R<List<Map<String, Object>>> getEnergyRanking() {
        return R.ok(energyStatService.getEnergyRanking());
    }

    @Operation(summary = "总用电概览（今日/本周/本月）")
    @GetMapping("/summary")
    public R<Map<String, Object>> getEnergySummary() {
        return R.ok(energyStatService.getEnergySummary());
    }
}
