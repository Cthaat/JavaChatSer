# 使用官方 Tomcat 镜像作为基础镜像
FROM tomcat:9.0

# 将构建的 WAR 文件复制到 Tomcat 的 webapps 目录
COPY target/JavaChat.war /usr/local/tomcat/webapps/

# 暴露 Tomcat 的默认端口
EXPOSE 8080

# 使用 Tomcat 默认的 ENTRYPOINT 启动服务
CMD ["catalina.sh", "run"]
