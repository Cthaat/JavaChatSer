# 使用 Maven 的官方镜像构建 Java 应用
FROM maven AS builder

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
COPY --from=builder /app/target/JavaChatSer-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/JavaChat.war

# 安装 MySQL 和 Redis
RUN apt-get update && apt-get install -y \
    mysql-server \
    redis-server \
    && apt-get clean

# 复制 MySQL 和 Redis 配置文件
COPY ./my.cnf /etc/mysql/my.cnf
COPY ./redis.conf /etc/redis/redis.conf

# 创建 MySQL 用户的家目录
RUN mkdir -p /var/lib/mysql && chown -R mysql:mysql /var/lib/mysql

# 复制 SQL 文件
COPY --from=builder /app/src/main/resources/javaChat.sql /docker-entrypoint-initdb.d/

# 暴露服务端口
EXPOSE 8080 3306 6379

# 启动 MySQL 和 Redis 的脚本
COPY start.sh /start.sh
RUN chmod +x /start.sh

# 使用 start.sh 启动服务
CMD ["/start.sh"]

# 使用 Tomcat 默认的 ENTRYPOINT 启动服务
CMD ["catalina.sh", "run"]
