# 项目摘要 - 2026-03-06

## 已完成项目

### 1. Home_Serve 家政服务平台
- **GitHub**: https://github.com/Cokosk/Home_Serve
- **阿里云部署**: http://101.200.180.182
- **技术栈**: Spring Boot 2.7 + Redis + MySQL 8.0 + Vue3 + Element Plus
- **核心功能**:
  - 用户预约、服务者抢单
  - Redis分布式锁（抢单核心）
  - 接口限流、异步队列
  - 管理后台（订单/服务/用户/服务者管理）
- **部署位置**: /root/home-serve

### 2. Binance 监控仪表板
- **访问地址**: http://101.200.180.182:5000
- **技术栈**: Python Flask + Binance API
- **功能**:
  - 账户余额查询
  - 实时价格监控
  - USDT估值计算
- **脚本位置**: /root/binance-dashboard.py

### 3. 本地监控脚本
- **位置**: ~/.openclaw/workspace/scripts/
  - binance-monitor.sh (基础版)
  - binance-monitor-safe.sh (安全版，使用read -s静默输入)
  - binance-dashboard.py (Web仪表板)

## 已安装Skill (14个)

### 系统健康/防卡死
- active-maintenance - 系统健康维护
- anti-amnesia - 防遗忘系统

### 安全
- arc-security-audit - 安全审计
- arc-skill-scanner - 漏洞扫描
- config-validator - 配置验证

### 编程能力
- auto-test-generator - 自动生成测试
- auto-pr-merger - 自动合并PR
- code-stats - 代码统计
- agent-mode-upgrades - OpenClaw增强模式

### 个人成长
- agent-evolver - 自进化引擎
- agent-reflect - 自我反思
- daily-review-ritual - 每日复盘
- deepthink - 知识库
- morning-routine - 晨间习惯

## 阿里云服务器信息
- **IP**: 101.200.180.182
- **运行服务**:
  - Nginx (80端口)
  - Java后端 (8080端口)
  - Python监控 (5000端口)
  - MySQL (3306端口)
  - Redis (6379端口)

## Telegram配置
- **Bot Token**: 8363279072:AAE2QSMb01nItGGILgpQpetFuKmJAfheNpw
- **dmPolicy**: pairing
- **groupPolicy**: allowlist
- **streaming**: on (流式响应已开启)

## 当前模型
- **默认**: bailian/MiniMax-M2.5
- **推荐**: qwen3.5-plus (日常使用)
- **编程**: qwen3-coder-plus

## 重要配置
- **SSH密钥**: ~/.ssh/id_ed25519_openclaw_wyh
- **阿里云密码**: Wyh021114
- **GitHub**: Cokosk/Home_Serve

---
*最后更新: 2026-03-06 15:50*