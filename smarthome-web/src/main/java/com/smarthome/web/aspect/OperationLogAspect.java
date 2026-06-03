package com.smarthome.web.aspect;

import com.smarthome.common.annotation.OperationLog;
import com.smarthome.web.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 操作日志切面
 * 拦截标注了 @OperationLog 注解的 Controller 方法，自动记录操作日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        // 获取当前登录用户信息
        Long userId = null;
        String username = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                userId = (Long) principal;
            }
            Object details = authentication.getDetails();
            if (details instanceof String) {
                username = (String) details;
            }
        }

        // 获取请求 IP
        String ip = getClientIp();

        // 从方法参数中提取目标和详情
        String target = extractTarget(joinPoint);
        String detail = buildDetail(joinPoint, operationLog);

        boolean success = true;
        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            success = false;
            throw e;
        } finally {
            // 异步保存日志（在 finally 中确保无论成功失败都记录）
            try {
                operationLogService.saveLog(
                        userId, username,
                        operationLog.module(), operationLog.operation(),
                        target, detail, ip, success
                );
            } catch (Exception ex) {
                log.error("记录操作日志失败", ex);
            }
        }
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "unknown";
            }
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            // 多个代理时取第一个
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 从方法参数中提取操作目标
     * 约定：方法参数中包含 deviceId / sceneId / logId 时，提取对应名称
     */
    private String extractTarget(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 尝试从参数中提取有意义的目标描述
        StringBuilder target = new StringBuilder();
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            // Long 类型的 ID 参数
            if (arg instanceof Long) {
                if (target.length() > 0) {
                    target.append(", ");
                }
                target.append("ID=").append(arg);
            }
            // String 类型的标识符参数
            if (arg instanceof String && !((String) arg).isEmpty()) {
                String strArg = (String) arg;
                // 排除 IP 地址格式和过长的字符串
                if (!strArg.matches("\\d+\\.\\d+\\.\\d+\\.\\d+") && strArg.length() < 100) {
                    if (target.length() > 0) {
                        target.append(", ");
                    }
                    target.append(strArg);
                }
            }
        }
        return target.length() > 0 ? target.toString() : null;
    }

    /**
     * 构建操作详情
     */
    private String buildDetail(ProceedingJoinPoint joinPoint, OperationLog operationLog) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        return String.format("[%s.%s] %s - %s",
                className, methodName,
                operationLog.module(), operationLog.operation());
    }
}
