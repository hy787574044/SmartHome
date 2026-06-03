# SmartHome 全屋智能控制系统

基于 Spring Boot + Vue 3 + EMQX 的全屋智能控制系统，支持设备管理、场景联动、告警监控、家庭管理、数据分析等完整功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 2.7 + JDK 8 + MyBatis-Plus 3.5 |
| 前端 | Vue 3 + Element Plus + ECharts |
| MQTT | EMQX (开源版) / 可选 |
| 数据库 | MySQL 5.7+ + Redis 6+ |
| 认证 | Spring Security + JWT |
| API 文档 | springdoc-openapi |
| 部署 | Docker Compose |

## 项目结构

```
SmartHome/
├── smarthome-common/        # 公共模块（工具类、配置、异常处理）
│   ├── config/              #   MyBatis-Plus、Security、CORS、JWT配置
│   ├── constant/            #   系统常量
│   ├── exception/           #   全局异常处理
│   └── utils/               #   Redis工具、JWT工具
├── smarthome-model/         # 实体模块
│   ├── entity/              #   实体类（Device、Product、Scene、User等）
│   ├── mapper/              #   MyBatis-Plus Mapper接口
│   ├── dto/                 #   数据传输对象
│   └── vo/                  #   视图对象
├── smarthome-mqtt/          # MQTT模块（对接EMQX）
│   ├── config/              #   MQTT配置、连接参数
│   ├── handler/             #   消息处理器
│   └── service/             #   消息发送服务
├── smarthome-device/        # 设备管理模块
│   └── service/             #   设备、产品、房间、认证、能耗服务
├── smarthome-scene/         # 场景联动模块
│   └── service/             #   场景服务（模板、触发、执行）
├── smarthome-alert/         # 告警监控模块
│   └── service/             #   告警服务、通知服务（微信/邮件/短信）
├── smarthome-web/           # Web模块
│   ├── controller/          #   REST API控制器
│   ├── config/              #   WebSocket配置
│   ├── aspect/              #   操作日志切面
│   ├── listener/            #   事件监听器
│   └── service/             #   导出服务、操作日志服务
├── smarthome-app/           # 启动模块
│   ├── SmartHomeApplication.java
│   ├── application.yml
│   └── schema.sql           #   数据库初始化脚本
└── web/                     # 前端项目
    ├── src/
    │   ├── api/             #   API封装
    │   ├── components/      #   公共组件（粒子背景等）
    │   ├── layout/          #   布局组件
    │   ├── router/          #   路由配置
    │   ├── styles/          #   全局样式（暗色主题）
    │   ├── utils/           #   工具类（WebSocket等）
    │   └── views/           #   页面组件
    └── package.json
```

## 功能清单

### ✅ 已完成功能

| 模块 | 功能 |
|------|------|
| **用户系统** | 登录/注册、JWT鉴权、个人资料、修改密码、头像设置 |
| **设备管理** | 设备CRUD、产品管理、物模型定义、状态监控、设备详情页 |
| **设备控制** | 开关控制、滑块调节、数值输入、批量控制 |
| **设备分组** | 设备分组管理、一键批量控制 |
| **设备历史** | 属性历史查询、ECharts趋势图表 |
| **房间管理** | 房间CRUD、按房间分组设备 |
| **场景联动** | 手动/自动场景、设备触发、定时触发、条件触发 |
| **场景模板** | 6个预置模板（回家/离家/睡眠/起床/影院/阅读） |
| **场景复制** | 复制已有场景、场景执行日志 |
| **告警监控** | 告警规则、告警日志、告警处理/忽略 |
| **告警统计** | 告警趋势、级别分布、设备排行 |
| **通知设置** | 企业微信/邮件/短信通知、免打扰时段 |
| **家庭管理** | 创建家庭、邀请码加入、成员管理、角色权限 |
| **操作日志** | 设备控制/场景执行等操作自动记录 |
| **能耗统计** | 每日/每周/每月用电量、设备排行、房间分布 |
| **数据导出** | 设备列表/告警日志/操作日志/历史数据导出Excel |
| **系统设置** | MQTT配置、告警参数、数据清理 |
| **首页仪表盘** | 统计卡片、房间概览、快捷场景、实时数据 |
| **移动端** | 响应式布局、移动端侧边栏 |

### 页面列表

| 路由 | 页面 | 说明 |
|------|------|------|
| `/login` | 登录页 | 粒子动画背景、玻璃态卡片 |
| `/dashboard` | 首页 | 统计概览、设备状态、快捷操作 |
| `/device/list` | 设备列表 | 设备管理、筛选、控制 |
| `/device/detail/:id` | 设备详情 | 控制面板、属性展示、历史图表 |
| `/device/product` | 产品管理 | 产品CRUD、物模型管理 |
| `/room` | 房间管理 | 房间卡片、设备统计 |
| `/scene` | 场景列表 | 场景管理、手动执行 |
| `/scene/templates` | 场景模板 | 预置模板、一键创建 |
| `/alert/rules` | 告警规则 | 规则CRUD、启用/禁用 |
| `/alert/logs` | 告警日志 | 告警记录、处理/忽略 |
| `/analytics/energy` | 能耗统计 | 用电趋势、设备排行 |
| `/analytics/alert` | 告警统计 | 告警趋势、级别分布 |
| `/notification` | 通知设置 | 通知渠道配置、免打扰 |
| `/family` | 家庭管理 | 成员管理、邀请码 |
| `/log` | 操作日志 | 操作记录、筛选查询 |
| `/user/profile` | 个人中心 | 资料修改、密码修改 |
| `/system/settings` | 系统设置 | 参数配置、数据清理 |
| `/system/about` | 关于系统 | 版本信息、技术栈 |

## 快速开始

### 1. 环境准备

- JDK 8+
- Maven 3.5+
- MySQL 5.7+
- Redis 6.0+
- Node.js 16+（前端）
- EMQX（可选，MQTT Broker）

### 2. 初始化数据库

```bash
mysql -u root -p < smarthome-app/src/main/resources/schema.sql
```

默认管理员账号：`admin` / `admin123`

### 3. 修改配置

编辑 `smarthome-app/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smarthome?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379

# MQTT（可选，不安装EMQX也能运行）
mqtt:
  enabled: false
  broker: tcp://localhost:1883
```

### 4. 启动后端

```bash
# 方式一：Maven启动
cd SmartHome
mvn clean package -DskipTests
java -jar smarthome-app/target/smarthome-app-1.0.0.jar

# 方式二：IDE启动
# 运行 SmartHomeApplication.java
```

### 5. 启动前端

```bash
cd SmartHome/web
npm install
npm run dev
```

### 6. 访问系统

- 前端地址：http://localhost:3000
- API文档：http://localhost:8080/swagger-ui.html
- WebSocket：ws://localhost:8080/ws

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

## API 接口列表

| 模块 | 接口前缀 | 说明 |
|------|----------|------|
| 认证 | `/api/auth` | 登录、注册、获取用户信息 |
| 用户 | `/api/user` | 用户资料、密码修改、头像 |
| 设备 | `/api/device` | 设备CRUD、控制、历史数据 |
| 产品 | `/api/product` | 产品CRUD、物模型管理 |
| 房间 | `/api/room` | 房间CRUD |
| 场景 | `/api/scene` | 场景CRUD、执行、模板 |
| 告警 | `/api/alert` | 告警规则、告警日志 |
| 通知 | `/api/notification` | 通知配置、测试发送 |
| 家庭 | `/api/family` | 家庭管理、成员管理 |
| 能耗 | `/api/energy` | 能耗统计 |
| 操作日志 | `/api/operationLog` | 操作日志查询 |
| 系统 | `/api/system` | 系统配置、系统信息 |
| 设备分组 | `/api/deviceGroup` | 设备分组、批量控制 |
| 首页 | `/api/dashboard` | 统计数据 |

## 部署

### Docker 部署

```bash
cd docker
docker-compose up -d
```

### 手动部署

1. 执行数据库初始化脚本
2. 修改 application.yml 配置
3. `mvn clean package -DskipTests`
4. `java -jar smarthome-app/target/smarthome-app-1.0.0.jar`
5. 前端 `npm run build`，将 dist 目录部署到 Nginx

## 许可证

Apache License 2.0
