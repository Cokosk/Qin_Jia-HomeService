# 勤家家政服务 (Qin_Jia Home Service)

> 基于 Redis 高并发响应的家政服务平台

## 项目概述

勤家家政服务平台是一个面向用户和服务者的双向预约抢单系统。用户可以浏览服务、预约下单；服务者可以接单抢单、管理订单。系统重点突出 Redis 在高并发场景下的应用：缓存热点数据、分布式锁控制抢单、接口限流。

**技术栈：** Spring Boot 2.7 + Redis + MySQL 8.0 + Vue3 + Element Plus

## 访问地址

| 端 | 地址 | 状态 |
|------|------|------|
| 管理后台 | http://101.200.180.182 | ✅ 已部署 |
| 用户端 | http://101.200.180.182/user | ✅ 已部署 |
| 服务者端 | http://101.200.180.182/worker | ✅ 已部署 |

## 功能特性

### 用户端 ✅ 已上线
- 🏠 浏览家政服务分类
- 📅 预约服务、选择时间
- 📋 订单管理与查询
- 👤 个人中心

### 服务者端 ✅ 已上线
- 📋 查看抢单池
- ⚡ 高并发抢单（Redis 分布式锁）
- 📊 订单管理与收入统计
- 💰 收入统计与评分展示

### 管理后台 ✅ 已上线
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
Qin_Jia-HomeService/
├── home-serve/                # 项目主目录
│   ├── backend/               # 后端服务
│   │   ├── src/main/java/     # Java 源码
│   │   ├── src/test/java/     # 测试用例
│   │   └── pom.xml            # Maven 配置
│   ├── front-admin/           # 管理后台前端 ✅
│   │   ├── src/               # Vue3 源码
│   │   └── package.json       # npm 配置
│   ├── front-user/            # 用户端前端 ✅
│   │   ├── src/               # Vue3 源码
│   │   │   ├── views/         # 页面组件
│   │   │   ├── api/           # API 封装
│   │   │   ├── router/        # 路由配置
│   │   │   └── stores/        # Pinia 状态
│   │   └── package.json
│   ├── front-worker/          # 服务者端前端 ✅
│   │   ├── src/               # Vue3 源码
│   │   │   ├── views/         # 页面组件
│   │   │   ├── api/           # API 封装
│   │   │   ├── router/        # 路由配置
│   │   │   └── stores/        # Pinia 状态
│   │   └── package.json
│   ├── docs/                  # 开发文档
│   ├── sql/                   # 数据库脚本
│   ├── nginx/                 # Nginx 配置
│   └── docker-compose.yml     # Docker 编排
├── skills/                    # OpenClaw Skills
└── README.md
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+
- Docker & Docker Compose

### 本地开发

```bash
# 1. 克隆项目
git clone https://github.com/Cokosk/Qin_Jia-HomeService.git
cd Qin_Jia-HomeService/home-serve

# 2. 初始化数据库
mysql -u root -p < sql/init.sql

# 3. 启动后端
mvn spring-boot:run

# 4. 启动前端（选择一个）
cd front-admin && npm run dev   # 管理后台
cd front-user && npm run dev    # 用户端
cd front-worker && npm run dev  # 服务者端
```

### Docker 部署

```bash
cd home-serve
docker compose up -d
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
| `/api/order/start` | POST | 开始服务 |
| `/api/order/finish` | POST | 完成服务 |

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
- 🎨 用户端/服务者端前端开发

## 开发进度

- [x] 后端 API 开发
- [x] 管理后台前端
- [x] Redis 缓存与分布式锁
- [x] 单元测试用例
- [x] 阿里云部署
- [x] 用户端前端
- [x] 服务者端前端
- [ ] 支付模块（模拟支付）
- [ ] 评价模块（双向评价）
- [ ] 性能优化与压测

## 部署状态

| 环境 | 状态 | 地址 |
|------|------|------|
| 阿里云 | ✅ 已部署 | http://101.200.180.182 |
| GitHub | ✅ 已同步 | https://github.com/Cokosk/Qin_Jia-HomeService |

## 更新日志

### v1.1.0 (2026-03-09)
- ✅ 用户端前端开发完成（首页、服务列表、预约下单、订单管理、用户中心）
- ✅ 服务者端前端开发完成（抢单池、抢单功能、订单管理、收入统计）
- ✅ 飞书通道配置完成

### v1.0.0 (2026-03-06)
- ✅ 完成后端核心功能开发
- ✅ 完成 Vue3 + Element Plus 管理后台
- ✅ Redis 缓存、分布式锁、限流功能实现
- ✅ 抢单核心流程实现
- ✅ 阿里云部署成功

## 许可证

MIT License

---

<p align="center">
  勤家家政 · 让服务更简单
</p>

<p align="center">
  Made with ❤️ by WYH | Assisted by 🦞 麻辣小龙虾
</p>