# 使用 Maven 的官方镜像构建 Java 应用
FROM maven:3.9.6-openjdk-21 AS builder

# 设置工作目录
WORKDIR /app

# 将项目的 pom.xml 文件复制到容器中
COPY pom.xml .

# 下载依赖以便于后续构建使用（提高构建效率）
RUN mvn dependency:go-offline

# 将整个项目复制到容器中
COPY . .

# 使用 Maven 构建项目，生成 WAR 文件
RUN mvn clean package

# 使用 Tomcat 的官方镜像运行应用
FROM tomcat:9.0

# 将构建的 WAR 文件复制到 Tomcat 的 webapps 目录
COPY --from=builder /app/target/JavaChat.war /usr/local/tomcat/webapps/

# 暴露 Tomcat 的默认端口
EXPOSE 8080

# 使用 Tomcat 默认的 ENTRYPOINT 启
