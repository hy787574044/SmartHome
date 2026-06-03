package com.smarthome;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 全屋智能控制系统 - 启动类
 */
@SpringBootApplication(scanBasePackages = "com.smarthome")
@MapperScan("com.smarthome.model.mapper")
@EnableScheduling
public class SmartHomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartHomeApplication.class, args);
        System.out.println("========================================");
        System.out.println("  SmartHome 全屋智能控制系统启动成功！");
        System.out.println("  API 文档: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
