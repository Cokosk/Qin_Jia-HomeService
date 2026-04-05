# 勤家家政服务平台

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
cd Qin_Jia-HomeService/home-serve

# 2. 初始化数据库
mysql -u root -p < sql/init.sql

# 3. 启动后端
mvn spring-boot:run

# 4. 启动前端 (选择其中一个)
cd front-admin && npm install && npm run dev  # 管理后台 :3000
cd front-user && npm install && npm run dev   # 用户端 :5173
cd front-worker && npm install && npm run dev # 工人端 :5174
```

### Docker 部署

```bash
# 一键启动
docker compose up -d

# 访问
# 前端: http://localhost
# API: http://localhost:8080
```

## 项目结构

```
home-serve/
├── backend/                 # Spring Boot 后端
│   └── src/main/java/com/cokosk/homeserve/
│       ├── controller/      # REST API
│       ├── service/        # 业务逻辑
│       ├── mapper/          # 数据访问
│       ├── entity/          # 实体类
│       └── lock/            # 分布式锁
├── front-user/             # 用户端 Vue3
├── front-worker/           # 工人端 Vue3
├── front-admin/            # 管理后台 Vue3
├── ts-miniapp/             # 微信小程序 (uni-app)
├── sql/                    # 数据库脚本
├── nginx/                  # Nginx 配置
├── docker-compose.yml      # Docker 编排
└── docs/                   # 开发文档
```

## API 概览

### 用户模块
- `POST /api/user/login` - 用户登录
- `POST /api/user/register` - 用户注册
- `GET /api/user/info` - 获取用户信息

### 服务模块
- `GET /api/service/category` - 服务分类
- `GET /api/service/hot` - 热门服务
- `GET /api/service/list` - 服务列表

### 订单模块
- `POST /api/order/create` - 创建订单
- `POST /api/order/grab` - 工人抢单
- `GET /api/order/grab-pool` - 抢单池

### 管理模块
- `GET /api/admin/stats` - 数据统计
- `GET /api/admin/users` - 用户管理
- `GET /api/admin/orders` - 订单管理

## 功能特性

- ✅ JWT 令牌认证
- ✅ Redis 缓存 (服务分类、热门服务)
- ✅ 分布式锁 (抢单防重复)
- ✅ 接口限流
- ✅ 订单状态流转
- ✅ 评价系统
- ✅ 支付集成 (Mock)

## 文档

- [开发文档](./docs/DEVELOPMENT.md)
- [更新日志](./docs/CHANGELOG.md)

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