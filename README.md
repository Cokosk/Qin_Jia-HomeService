# 勤家家政服务 (Qin_Jia Home Service)

> 基于 Redis 高并发响应的家政服务平台

## 项目概述

勤家家政服务平台是一个面向用户和服务者的双向预约抢单系统。用户可以浏览服务、预约下单；服务者可以接单抢单、管理订单。系统重点突出 Redis 在高并发场景下的应用：缓存热点数据、分布式锁控制抢单、接口限流。

**在线演示：** http://101.200.180.182

**技术栈：** Spring Boot 2.7 + Redis + MySQL 8.0 + Vue3 + Element Plus

## 功能特性

### 用户端（规划中）
- 🏠 浏览家政服务分类
- 📅 预约服务、选择时间
- 💰 模拟支付流程
- ⭐ 服务评价

### 服务者端（规划中）
- 📋 查看抢单池
- ⚡ 高并发抢单（Redis 分布式锁）
- 📊 订单管理与收入统计

### 管理后台（已上线）
- 📈 数据概览仪表盘
- 👥 用户/服务者管理
- 🗂️ 服务分类与项目管理
- 📋 订单监控与管理

## 核心亮点

### Redis 高并发应用

| 场景 | 实现方案 | 效果 |
|------|----------|------|
| 热点数据缓存 | String/Hash 缓存 | 响应时间 < 50ms |
| 抢单并发控制 | SET NX EX 分布式锁 | 无超卖/重复抢单 |
| 接口限流 | 滑动窗口算法 | 保护系统稳定 |

### 抢单流程

```
用户下单 → 订单进入抢单池 → 服务者并发抢单
                ↓
        ┌──────────────────┐
        │  1. 限流检查      │
        │  2. 获取分布式锁   │
        │  3. 验证订单状态   │
        │  4. 更新数据库     │
        │  5. 同步缓存       │
        │  6. 释放锁        │
        └──────────────────┘
                ↓
           抢单成功/失败
```

## 项目结构

```
qin-jia/
├── backend/                 # 后端服务
│   ├── src/main/java/       # Java 源码
│   ├── src/test/java/       # 测试用例
│   └── pom.xml              # Maven 配置
├── front-admin/             # 管理后台前端
│   ├── src/                 # Vue3 源码
│   └── package.json         # npm 配置
├── front-user/              # 用户端前端（规划）
├── front-worker/            # 服务者端前端（规划）
├── sql/                     # 数据库脚本
├── nginx/                   # Nginx 配置
└── docker-compose.yml       # Docker 编排
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+

### 本地开发

```bash
# 1. 克隆项目
git clone https://github.com/Cokosk/Home_Serve.git
cd Home_Serve

# 2. 初始化数据库
mysql -u root -p < sql/init.sql

# 3. 启动后端
cd backend
mvn spring-boot:run

# 4. 启动前端
cd ../front-admin
npm install
npm run dev
```

### Docker 部署

```bash
# 一键启动
chmod +x deploy.sh
./deploy.sh
```

## API 文档

### 用户模块

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/user/login` | POST | 用户登录 |
| `/api/user/register` | POST | 用户注册 |
| `/api/user/info` | GET | 获取用户信息 |

### 服务模块

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/service/category` | GET | 服务分类（缓存） |
| `/api/service/hot` | GET | 热门服务（缓存） |
| `/api/service/list` | GET | 服务列表 |
| `/api/service/detail` | GET | 服务详情 |

### 订单模块

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/order/create` | POST | 创建订单 |
| `/api/order/grab` | POST | 抢单 ⭐ |
| `/api/order/grab-pool` | GET | 抢单池 |
| `/api/order/list` | GET | 订单列表 |
| `/api/order/cancel` | POST | 取消订单 |

## 贡献者

| 角色 | 贡献者 | 职责 |
|:----:|:------:|------|
| 核心开发 | **WYH** ([@Cokosk](https://github.com/Cokosk)) | 架构设计、核心代码开发 |
| 协助开发 | 🦞 麻辣小龙虾 (AI Assistant) | 部署配置、测试用例、文档 |

### 协作说明

本项目由 **WYH** 主导开发，负责核心业务逻辑和架构设计。

AI 助手（麻辣小龙虾）协助完成：
- 🔧 环境配置与部署文档
- 🧪 单元测试与集成测试
- 📝 项目文档维护

## 开发进度

- [x] 后端 API 开发
- [x] 管理后台前端
- [x] Redis 缓存与分布式锁
- [x] 单元测试用例
- [ ] 用户端前端
- [ ] 服务者端前端
- [ ] 性能优化与压测

## 许可证

MIT License

---

<p align="center">
  勤家家政 · 让服务更简单
</p>

<p align="center">
  Made with ❤️ by WYH | Assisted by 🦞 麻辣小龙虾
</p>
