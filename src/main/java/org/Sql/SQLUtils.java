package org.Sql;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SQLUtils
{
    public static DataSource druidDataSource;

    // 构造方法私有化
    private SQLUtils()
    {
    }

    // 静态代码块，初始化数据源
    static
    {
        try
        {
            Properties pro = new Properties();
            pro.load(JdbcUtils.class.getClassLoader().getResourceAsStream("druid.properties"));
            druidDataSource = DruidDataSourceFactory.createDataSource(pro);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    // 获取数据库连接
    public static Connection getConnection() throws SQLException
    {
        return druidDataSource.getConnection();
    }

    // 关闭数据库连接
    public static void closeConnection(Statement stmt, Connection conn , ResultSet rs)
    {
        if (stmt!= null)
        {
            try
            {
                stmt.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        if (conn!= null)
        {
            try
            {
                conn.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        if (rs!= null)
        {
            try
            {
                rs.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    // 关闭数据库连接
    public static void closeConnection(Statement stmt, Connection conn)
    {
        closeConnection(stmt, conn, null);
    }

    // 获取连接池
    public static DataSource getDataSource()
    {
        return druidDataSource;
    }

}
