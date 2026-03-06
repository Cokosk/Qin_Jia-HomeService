# 简化版后端Dockerfile
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# 复制jar包
COPY target/home-serve-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]