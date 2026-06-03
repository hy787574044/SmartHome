package com.smarthome.web.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthome.common.result.PageResult;
import com.smarthome.model.entity.OperationLog;
import com.smarthome.model.mapper.OperationLogMapper;
import com.smarthome.model.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /**
     * 保存操作日志
     */
    public void saveLog(Long userId, String username, String module, String operation,
                        String target, String detail, String ip, boolean success) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(userId);
            operationLog.setUsername(username);
            operationLog.setModule(module);
            operationLog.setOperation(operation);
            operationLog.setTarget(target);
            operationLog.setDetail(detail);
            operationLog.setIp(ip);
            operationLog.setStatus(success ? 1 : 0);
            operationLog.setCreateTime(LocalDateTime.now());
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    /**
     * 分页查询操作日志
     *
     * @param userId    用户ID（可选）
     * @param module    模块（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param pageNum   页码
     * @param pageSize  每页大小
     */
    public PageResult<OperationLogVO> listLogs(Long userId, String module,
                                               LocalDateTime startTime, LocalDateTime endTime,
                                               int pageNum, int pageSize) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(OperationLog::getUserId, userId);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(OperationLog::getModule, module);
        }
        if (startTime != null) {
            wrapper.ge(OperationLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperationLog::getCreateTime, endTime);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);

        Page<OperationLog> page = operationLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<OperationLogVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList);
    }

    /**
     * 导出操作日志为 Excel
     */
    public void exportExcel(Long userId, String module,
                            LocalDateTime startTime, LocalDateTime endTime,
                            HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(OperationLog::getUserId, userId);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(OperationLog::getModule, module);
        }
        if (startTime != null) {
            wrapper.ge(OperationLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperationLog::getCreateTime, endTime);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);

        List<OperationLog> logs = operationLogMapper.selectList(wrapper);

        // 使用 Hutool ExcelWriter 写出
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 设置表头别名
        writer.addHeaderAlias("logId", "日志ID");
        writer.addHeaderAlias("username", "操作用户");
        writer.addHeaderAlias("module", "操作模块");
        writer.addHeaderAlias("operation", "操作类型");
        writer.addHeaderAlias("target", "操作目标");
        writer.addHeaderAlias("detail", "操作详情");
        writer.addHeaderAlias("ip", "操作IP");
        writer.addHeaderAlias("status", "状态");
        writer.addHeaderAlias("createTime", "操作时间");

        // 转换为 VO 列表写入
        List<OperationLogVO> voList = logs.stream().map(this::toVO).collect(Collectors.toList());
        writer.write(voList, true);

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String fileName = URLEncoder.encode("操作日志", StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }

    /**
     * 实体转 VO
     */
    private OperationLogVO toVO(OperationLog entity) {
        OperationLogVO vo = new OperationLogVO();
        vo.setLogId(entity.getLogId());
        vo.setUsername(entity.getUsername());
        vo.setModule(entity.getModule());
        vo.setOperation(entity.getOperation());
        vo.setTarget(entity.getTarget());
        vo.setDetail(entity.getDetail());
        vo.setIp(entity.getIp());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}
