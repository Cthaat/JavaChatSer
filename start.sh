#!/bin/bash

# 启动 MySQL 服务
service mysql start

# 设置 MySQL 密码和创建数据库和用户
MYSQL_ROOT_PASSWORD="123456..a"  # 设置 root 密码
MYSQL_USER="root"
MYSQL_PASSWORD="123456..a"
DATABASE="javachat"

# 创建数据库和用户，并执行 SQL 文件
mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD}';"
mysql -u root -p"${MYSQL_ROOT_PASSWORD}" -e "CREATE DATABASE IF NOT EXISTS ${DATABASE};"
mysql -u root -p"${MYSQL_ROOT_PASSWORD}" -e "CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}';"
mysql -u root -p"${MYSQL_ROOT_PASSWORD}" -e "GRANT ALL PRIVILEGES ON ${DATABASE}.* TO '${MYSQL_USER}'@'%';"
mysql -u root -p"${MYSQL_ROOT_PASSWORD}" -e "FLUSH PRIVILEGES;"

# 执行 SQL 文件
mysql -u root -p"${MYSQL_ROOT_PASSWORD}" ${DATABASE} < /docker-entrypoint-initdb.d/javaChat.sql

# 启动 Redis 服务
service redis-server start

# 启动 Tomcat
catalina.sh run
