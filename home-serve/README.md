# Home Serve - 家政服务平台

基于Redis高并发响应的家政服务平台（毕业设计项目）

## 项目简介

开发一个家政服务预约平台，核心业务包括用户预约、服务者抢单、订单管理、支付模拟、双向评价。重点突出Redis在高并发场景下的应用：缓存热点数据、分布式锁控制抢单、接口限流、异步队列处理，确保系统能支撑高并发（如抢单时100+ QPS）且数据一致。

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 后端框架 |
| MyBatis-Plus | 3.5.5 | ORM框架 |
| Redis (Lettuce) | - | 缓存 |
| Redisson | 3.25.2 | 分布式锁 |
| MySQL | 8.0 | 数据库 |
| Maven | 3.9+ | 构建工具 |
| JDK | 22 | Java版本 |

### 前端（管理后台）
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4+ | 前端框架 |
| Element Plus | 2.5+ | UI组件库 |
| Vite | 5.0+ | 构建工具 |
| Axios | 1.6+ | HTTP客户端 |
| Pinia | 2.1+ | 状态管理 |
| Vue Router | 4.2+ | 路由管理 |

## 项目结构

```
home-serve/
├── backend/                      # 后端项目
│   ├── src/main/
│   │   ├── java/com/cokosk/homeserve/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/       # 控制器
│   │   │   ├── service/         # 业务逻辑
│   │   │   ├── mapper/          # 数据访问
│   │   │   ├── entity/           # 实体类
│   │   │   ├── lock/            # 分布式锁
│   │   │   └── utils/           # 工具类
│   │   └── resources/
│   │       └── application.yml  # 配置文件
│   └── pom.xml
│
├── front-admin/                 # 前端管理后台
│   ├── src/
│   │   ├── api/                 # API封装
│   │   ├── router/              # 路由配置
│   │   ├── views/               # 页面组件
│   │   │   ├── Home.vue         # 首页/数据概览
│   │   │   ├── Orders.vue       # 订单管理
│   │   │   ├── Services.vue     # 服务管理
│   │   │   ├── Users.vue        # 用户管理
│   │   │   └── Workers.vue      # 服务者管理
│   │   ├── App.vue              # 根组件
│   │   └── main.js              # 入口文件
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
│
├── docs/                        # 文档
├── sql/                         # 数据库脚本
├── nginx/                       # Nginx配置
├── docker-compose.yml          # Docker编排
├── Dockerfile                   # 后端镜像
├── deploy.sh                    # 部署脚本
└── README.md
```

## 核心功能

### 后端API
- ✅ 用户模块：登录/注册/信息缓存
- ✅ 服务模块：分类列表、服务列表、详情（Redis缓存）
- ✅ 订单模块：创建订单、抢单（分布式锁+限流）
- ✅ 订单查询：用户/服务者订单列表
- ✅ 异步队列：抢单通知、支付处理

### 前端管理后台
- ✅ 首页：数据概览、热门服务、最新订单
- ✅ 订单管理：订单列表、搜索筛选、详情查看、取消订单
- ✅ 服务管理：分类管理、服务项目管理
- ✅ 用户管理：用户列表、状态管理
- ✅ 服务者管理：服务者列表、审核管理

## Redis应用设计（核心亮点）

### 1. 缓存热点数据
| 缓存Key | 过期时间 | 说明 |
|---------|----------|------|
| `service:category:list` | 30分钟 | 分类列表 |
| `service:hot` | 30分钟 | 热门服务 |
| `service:category:{id}` | 30分钟 | 分类下服务 |
| `service:detail:{id}` | 1小时 | 服务详情 |
| `user:info:{id}` | 24小时 | 用户信息 |

### 2. 分布式锁（抢单核心）
```
key: order:grab:lock:{orderId}
value: UUID唯一标识
实现: SET NX EX + Lua脚本原子释放
```

### 3. 接口限流
```
key: rate:limit:{ip}:{api}
算法: 滑动窗口
限制: 每IP每分钟10次
```

### 4. 异步队列
| 队列名 | 用途 |
|--------|------|
| `queue:order:grabbed` | 抢单成功通知 |
| `queue:payment:success` | 支付成功处理 |

## 快速启动

### 方式一：分别启动（开发模式）

#### 1. 启动后端
```bash
# 初始化数据库
mysql -u root -p < sql/init.sql

# 修改配置
vim backend/src/main/resources/application.yml

# 启动后端
cd backend
mvn spring-boot:run
```

#### 2. 启动前端
```bash
cd front-admin
npm install
npm run dev
```

访问 http://localhost:3000

### 方式二：Docker部署

```bash
# 克隆项目
git clone https://github.com/Cokosk/Home_Serve.git
cd Home_Serve

# 一键部署
chmod +x deploy.sh
./deploy.sh
```

访问 http://your-server-ip

## 前端页面预览

| 页面 | 说明 |
|------|------|
| 首页 | 数据统计卡片、热门服务表格、最新订单列表 |
| 订单管理 | 搜索筛选、订单表格、分页、详情弹窗 |
| 服务管理 | 分类Tab、服务项目Tab、状态管理 |
| 用户管理 | 用户列表、角色标签、状态开关 |
| 服务者管理 | 服务者列表、评分显示、审核操作 |

## API接口文档

### 用户模块
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/user/login` | POST | 用户登录 |
| `/api/user/register` | POST | 用户注册 |
| `/api/user/info` | GET | 获取用户信息 |

### 服务模块
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/service/category` | GET | 获取分类列表（缓存） |
| `/api/service/hot` | GET | 热门服务（缓存） |
| `/api/service/list` | GET | 服务列表（缓存） |
| `/api/service/detail` | GET | 服务详情（缓存） |

### 订单模块
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/order/create` | POST | 创建订单 |
| `/api/order/grab` | POST | 抢单 ⭐ |
| `/api/order/grab-pool` | GET | 抢单池 |
| `/api/order/list` | GET | 订单列表 |
| `/api/order/detail` | GET | 订单详情 |
| `/api/order/cancel` | POST | 取消订单 |
| `/api/order/start` | POST | 开始服务 |
| `/api/order/finish` | POST | 完成服务 |
| `/api/order/worker-list` | GET | 服务者订单 |

### 健康检查
| 接口 | 方法 | 说明 |
|------|------|------|
| `/health` | GET | 健康检查 |

## 抢单核心流程

```
1. 限流检查
   ↓
2. 获取分布式锁 (SET NX EX)
   ↓
3. 验证订单状态 (Redis缓存 → DB)
   ↓
4. 更新数据库订单状态
   ↓
5. 同步更新Redis缓存
   ↓
6. 从抢单池移除
   ↓
7. 异步通知 (队列)
   ↓
8. 释放分布式锁
```

## 开发进度

- [x] 后端核心功能开发
- [x] 管理后台前端开发
- [ ] 本地测试
- [ ] 阿里云部署

## 性能目标

| 指标 | 目标 |
|------|------|
| 抢单接口响应时间 | < 500ms (100并发) |
| 缓存命中率 | > 90% |
| 数据库QPS降低 | > 50% |

## 许可证

MIT
## 部署说明

### 阿里云部署步骤

```bash
# 1. 上传项目到服务器
scp -r home-serve root@your-server-ip:/opt/

# 2. 登录服务器
ssh root@your-server-ip

# 3. 安装依赖（如未安装）
apt update && apt install -y docker.io docker-compose maven

# 4. 配置 Docker 镜像源（解决拉取失败问题）
cat > /etc/docker/daemon.json << 'DAEMON'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://registry.cn-hangzhou.aliyuncs.com"
  ]
}
DAEMON
systemctl restart docker

# 5. 启动服务
cd /opt/home-serve
docker-compose up -d

# 6. 验证服务
curl http://localhost:8080/health
```

### 防火墙配置

```bash
# 开放必要端口
ufw allow 80      # Nginx
ufw allow 8080    # 后端 API
ufw allow 3306    # MySQL（可选，仅内网访问建议关闭）
ufw allow 6379    # Redis（可选，仅内网访问建议关闭）
```

### 阿里云安全组

在阿里云控制台 - 安全组添加入站规则：
- TCP 80（HTTP）
- TCP 8080（API）
- TCP 3306（MySQL，建议仅内网）
- TCP 6379（Redis，建议仅内网）

### 日志查看

```bash
# 查看服务状态
docker-compose ps

# 查看后端日志
docker-compose logs backend

# 实时日志
docker-compose logs -f backend
```
