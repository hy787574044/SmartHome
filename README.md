# SmartHome 全屋智能控制系统

基于 Spring Boot 3 + Vue 3 + EMQX 的全屋智能控制系统。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2 + JDK 17 + MyBatis-Plus |
| 前端 | Vue 3 + Element Plus |
| MQTT | EMQX (开源版) |
| 数据库 | MySQL 8 + Redis 7 |
| API 文档 | Knife4j (Swagger) |

## 项目结构

```
smarthome/
├── smarthome-common/    # 公共工具、常量、枚举
├── smarthome-model/     # 实体类、Mapper、DTO
├── smarthome-mqtt/      # MQTT 客户端（对接 EMQX）
├── smarthome-device/    # 设备管理服务
├── smarthome-scene/     # 场景联动服务
├── smarthome-alert/     # 告警监控服务
├── smarthome-web/       # REST API、WebSocket
└── smarthome-app/       # Spring Boot 启动模块
```

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- EMQX 5.0+（MQTT Broker）

### 2. 初始化数据库

```bash
mysql -u root -p < smarthome-app/src/main/resources/schema.sql
```

### 3. 配置 EMQX

安装 EMQX 后，默认监听端口 1883。修改 `application.yml` 中的 MQTT 配置：

```yaml
mqtt:
  broker: tcp://localhost:1883
  username: smarthome
  password: smarthome
```

### 4. 启动项目

```bash
mvn clean package -DskipTests
java -jar smarthome-app/target/smarthome-app-1.0.0.jar
```

### 5. 访问

- API 文档: http://localhost:8080/doc.html
- WebSocket: ws://localhost:8080/ws

## 核心功能

### 设备管理
- 产品模板管理（物模型定义）
- 设备注册、状态监控
- 设备属性读取和控制
- 按房间分组管理

### 场景联动
- 手动场景（一键执行）
- 自动场景（条件触发）
- 设备触发、定时触发
- 串行/并行执行、延迟执行
- 静默期（防止重复触发）

### 告警监控
- 自定义告警规则
- 多级告警（提示/警告/严重）
- 告警日志记录
- 可扩展通知方式（日志/邮件/短信/微信）

## MQTT Topic 规范

```
/{productId}/{serialNumber}/property/post    ← 设备上报属性
/{productId}/{serialNumber}/property/get     ← 平台读取属性
/{productId}/{serialNumber}/functions/post   ← 平台下发指令
/{productId}/{serialNumber}/events/post      ← 设备上报事件
/{productId}/{serialNumber}/status           ← 设备上下线
```

## 设备上报数据格式

```json
[
  {"id": "temperature", "value": "25.5"},
  {"id": "humidity", "value": "60"}
]
```

## 许可证

Apache License 2.0
