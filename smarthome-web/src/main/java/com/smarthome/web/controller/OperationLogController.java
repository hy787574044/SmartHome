package com.smarthome.web.controller;

import com.smarthome.common.result.PageResult;
import com.smarthome.common.result.R;
import com.smarthome.model.vo.OperationLogVO;
import com.smarthome.web.service.ExportService;
import com.smarthome.web.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 操作日志 API
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/operationLog")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;
    private final ExportService exportService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping("/list")
    public R<PageResult<OperationLogVO>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(operationLogService.listLogs(userId, module, startTime, endTime, pageNum, pageSize));
    }

    @Operation(summary = "导出操作日志Excel")
    @GetMapping("/export")
    public void exportExcel(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            HttpServletResponse response) throws IOException {
        exportService.exportOperationLogs(startTime, endTime, response);
    }
}
