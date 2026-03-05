# Home Serve - 家政服务平台

基于Redis高并发响应的家政服务平台

## 项目简介

开发一个家政服务预约平台，核心业务包括用户预约、服务者抢单、订单管理、支付模拟、双向评价。重点突出Redis在高并发场景下的应用：缓存热点数据、分布式锁控制抢单、接口限流、异步队列处理。

## 技术栈

### 后端
- Spring Boot 2.x
- MyBatis-Plus
- Redis (Lettuce) + Redisson
- MySQL 8.0
- Maven

### 前端（待开发）
- 用户端：微信小程序（原生）
- 服务者端：微信小程序
- 管理后台：Vue 3 + Element Plus

## 核心功能

### 用户端
- 首页服务分类、热门服务列表（Redis缓存）
- 服务详情页（Redis缓存）
- 预约下单
- 订单列表/详情
- 评价订单

### 服务者端
- 可抢订单列表（从Redis抢单池获取）
- 抢单（Redis分布式锁 + 限流）
- 我的日程
- 收入查看

### 管理后台
- 用户管理
- 服务者审核
- 订单管理
- 服务项目管理

## Redis应用设计

### 1. 缓存热点数据
- 首页/服务列表：`service:hot`, `service:category:{id}`
- 服务详情：`service:detail:{id}`

### 2. 分布式锁（抢单）
- 使用Redis SET NX EX 实现锁
- key: `order:grab:lock:{orderId}`
- Lua脚本保证原子性释放

### 3. 接口限流
- 滑动窗口/令牌桶
- key: `rate:limit:{ip}:{api}`

### 4. 异步队列
- 支付成功后的通知处理
- 队列: `queue:payment:success`

## 快速启动

### 1. 环境要求
- JDK 11+
- MySQL 8.0+
- Redis 5.0+

### 2. 初始化数据库
```bash
mysql -u root -p < sql/init.sql
```

### 3. 配置修改
修改 `backend/src/main/resources/application.yml` 中的数据库和Redis配置

### 4. 启动项目
```bash
cd backend
mvn spring-boot:run
```

## 项目结构

```
home-serve/
├── backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/cokosk/homeserve/
│   │   │   │       ├── config/     # 配置类
│   │   │   │       ├── controller/  # 控制器
│   │   │   │       ├── service/     # 业务逻辑
│   │   │   │       ├── mapper/      # 数据访问
│   │   │   │       ├── entity/      # 实体类
│   │   │   │       ├── dto/        # 数据传输对象
│   │   │   │       ├── utils/      # 工具类
│   │   │   │       └── lock/       # 分布式锁
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   └── pom.xml
├── docs/                    # 文档
├── sql/                     # 数据库脚本
└── README.md
```

## 接口文档

| 模块 | 接口 | 说明 |
|------|------|------|
| 用户 | POST /api/user/login | 登录 |
| 用户 | POST /api/user/register | 注册 |
| 服务 | GET /api/service/category | 获取分类 |
| 服务 | GET /api/service/list | 获取服务列表 |
| 订单 | POST /api/order/create | 创建订单 |
| 订单 | POST /api/order/grab | 抢单 |
| 订单 | GET /api/order/list | 订单列表 |

## 性能目标

- 抢单接口在100并发下，平均响应时间 < 500ms
- 缓存命中率 > 90%
- 数据库QPS降低50%以上

## License

MIT