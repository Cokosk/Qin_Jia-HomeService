#!/bin/bash

# 家政服务平台部署脚本

echo "========== 开始部署家政服务平台 =========="

# 1. 构建项目
echo "[1/5] 构建Spring Boot项目..."
cd "$(dirname "$0")"
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "构建失败！"
    exit 1
fi

# 2. 构建Docker镜像
echo "[2/5] 构建Docker镜像..."
docker build -t homeserve-backend:latest .

# 3. 启动服务
echo "[3/5] 启动Docker服务..."
docker-compose up -d

# 4. 等待服务启动
echo "[4/5] 等待服务启动..."
sleep 30

# 5. 检查服务状态
echo "[5/5] 检查服务状态..."
docker-compose ps

echo ""
echo "========== 部署完成 =========="
echo "服务地址："
echo "  - 后端API: http://你的服务器IP:8080"
echo "  - Nginx:   http://你的服务器IP (80端口)"
echo ""
echo "查看日志: docker-compose logs -f"
echo "停止服务: docker-compose down"