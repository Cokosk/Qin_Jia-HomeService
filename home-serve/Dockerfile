# 构建阶段
FROM maven:3.9.5-eclipse-temurin-22 AS builder

WORKDIR /app

# 复制pom.xml并下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY backend/src ./backend/src
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:22-jdk-alpine

WORKDIR /app

# 安装必要工具
RUN apk add --no-cache curl

# 从构建阶段复制jar包
COPY --from=builder /app/backend/target/*.jar app.jar

# 创建非root用户
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -s /bin/sh -D appuser

USER appuser

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]