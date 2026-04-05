# 勤家家政服务 (Qin_Jia Home Service)

<div align="center">

![Vue](https://img.shields.io/badge/Vue-3.4+-42b883?style=flat&logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-6DB33F?style=flat&logo=spring)
![uni-app](https://img.shields.io/badge/uni-app-4.0+-007AFF?style=flat)
![License](https://img.shields.io/badge/License-MIT-green)

专业家政服务一站式解决方案，提供用户端、工人端、管理后台三端分离架构。

</div>

## 项目介绍

勤家 (Qin_Jia) 是一款面向家政服务行业的全栈解决方案，支持：

- 👤 **用户端** - 查找服务、在线下单、评价
- 👷 **工人端** - 抢单池、接单服务、收入管理
- ⚙️ **管理后台** - 数据统计、订单管理、用户管理
- 📱 **微信小程序** - 移动端服务入口

## 访问地址

| 端 | 地址 | 状态 |
|------|------|------|
| 管理后台 | http://101.200.180.182 | ✅ 已部署 |
| 用户端 | http://101.200.180.182/user | ✅ 已部署 |
| 服务者端 | http://101.200.180.182/worker | ✅ 已部署 |

## 技术架构

### 前端

| 项目 | 技术栈 | 端口 |
|------|--------|------|
| front-user | Vue 3 + Vite + Vant 4 | 5173 |
| front-worker | Vue 3 + Vite + Vant 4 | 5174 |
| front-admin | Vue 3 + Vite + Element Plus | 3000 |
| ts-miniapp | uni-app + Vue 3 + TypeScript | - |

### 后端

- Spring Boot 3.2 + Spring Security
- MyBatis-Plus + MySQL 8.0
- Redis (缓存 + 分布式锁)
- Redisson 分布式锁
- JWT 认证

## 功能特性

### 用户端 ✅ 已上线
- 🏠 浏览家政服务分类
- 📅 预约服务、选择时间
- 📋 订单管理与查询
- 👤 个人中心
- ⭐ 双向评价系统
- 💳 支付集成 (Mock)

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
│   │   └── src/main/java/com/cokosk/homeserve/
│   │       ├── controller/    # REST API
│   │       ├── service/       # 业务逻辑
│   │       ├── mapper/        # 数据访问
│   │       ├── entity/        # 实体类
│   │       └── lock/          # 分布式锁
│   ├── front-admin/           # 管理后台前端
│   ├── front-user/            # 用户端前端
│   ├── front-worker/          # 服务者端前端
│   ├── ts-miniapp/            # 微信小程序 (uni-app)
│   ├── docs/                  # 开发文档
│   ├── sql/                   # 数据库脚本
│   ├── nginx/                 # Nginx 配置
│   └── docker-compose.yml     # Docker 编排
├── skills/                    # OpenClaw Skills
└── README.md
```

## 快速开始

### 环境要求

| 软件 | 版本 |
|------|------|
| JDK | 21+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 7.0+ |

### 本地开发

```bash
# 1. 克隆项目
git clone https://github.com/Cokosk/Qin_Jia-HomeService.git
cd Qin_Jia-HomeService

# 2. 进入项目目录
cd home-serve

# 3. 初始化数据库
mysql -u root -p < sql/init.sql

# 4. 启动后端
mvn spring-boot:run

# 5. 启动前端 (选择其中一个)
cd front-admin && npm install && npm run dev  # 管理后台 :3000
cd front-user && npm install && npm run dev   # 用户端 :5173
cd front-worker && npm install && npm run dev # 工人端 :5174
```

### Docker 部署

```bash
cd home-serve
docker compose up -d

# 访问
# 前端: http://localhost
# API: http://localhost:8080
```

## API 文档

### 用户模块

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/user/login` | POST | 用户登录 |
| `/api/user/register` | POST | 用户注册 |
| `/api/user/info` | GET | 获取用户信息 |
| `/api/user/phone` | PUT | 修改手机号 |
| `/api/user/password` | PUT | 修改密码 |

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

### 管理模块

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/admin/stats` | GET | 数据统计 |
| `/api/admin/users` | GET | 用户管理 |
| `/api/admin/orders` | GET | 订单管理 |

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
- [x] 支付模块（模拟支付）
- [x] 评价模块（双向评价）
- [ ] 性能优化与压测

## 部署状态

| 环境 | 状态 | 地址 |
|------|------|------|
| 阿里云 | ✅ 已部署 | http://101.200.180.182 |
| GitHub | ✅ 已同步 | https://github.com/Cokosk/Qin_Jia-HomeService |

## 更新日志

### v1.1.0 (2026-04)

- 参数校验增强 (Order/UserController)
- 用户端/工人端登录区分
- 分布式锁优化 (防止重复抢单)
- 订单状态管理改进
- 缓存清除修复
- 单元测试覆盖
- IP限流优化
- 新增修改密码、手机号功能

### v1.0.1 (2026-04)

- 三端 UI 重构 (Lucide Icons)
- 新增骨架屏、空状态组件
- 微信小程序基础框架
- 后端分布式锁优化
- 单元测试覆盖

### v1.0.0 (2026-03)

- 后端核心功能完成
- 三端前端框架搭建
- Redis 缓存与分布式锁
- Docker 部署支持

## 许可证

MIT License - 查看 [LICENSE](LICENSE) 了解更多

---

<p align="center">
  勤家家政 · 让服务更简单
</p>

<p align="center">
  Made with ❤️ by WYH | Assisted by 🦞 麻辣小龙虾
</p>