package org.Sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class addFriend implements addFriendSQL
{
    @Override
    public boolean addFriendByUsername(String username , String friendName) throws SQLException
    {
        userFind find = new userFind();
        if (!find.userNameExists(friendName))
        {
            return false;
        }
        TransactionSynchronizationManager.initSynchronization();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
        DataSource dataSource = jdbcTemplate.getDataSource();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try
        {
            connection.setAutoCommit(false);
            String sql = "insert into p2p_relationship  (user_name , friend_name) values (?,?)";
            int result1 = jdbcTemplate.update(sql , username , friendName);
            int result2 = jdbcTemplate.update(sql , friendName , username);
            if (result1 == 1 && result2 == 1)
            {
                // 创建mapper对象，用于转换json格式
                ObjectMapper mapper = new ObjectMapper();
                // 读取redis.properties配置文件
                try (InputStream is = this.getClass().getResourceAsStream("/redis.properties") ;
                )
                {
                    // 注册 LocalDateTime 序列化器
                    Properties properties = new Properties();
                    // 加载redis配置文件
                    properties.load(is);
                    // 通过配置文件创建连接池
                    JedisPoolConfig config = new JedisPoolConfig();
                    // 配置
                    try (JedisPool pool = new JedisPool(
                            config , properties.getProperty("redis.host") ,
                            Integer.parseInt(properties.getProperty("redis.port")) ,
                            Integer.parseInt(properties.getProperty("redis.timeout")) ,
                            properties.getProperty("redis.password") ,
                            Integer.parseInt(properties.getProperty("redis.database"))
                    ) ;)
                    {
                        Jedis jedis = pool.getResource();
                        jedis.del(username);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                connection.commit();
                return true;
            }
            connection.rollback();
            return false;
        }
        catch (Exception e)
        {
            connection.rollback();
            return false;
        }
        finally
        {
            connection.setAutoCommit(true);
            connection.close();
        }
    }
}
