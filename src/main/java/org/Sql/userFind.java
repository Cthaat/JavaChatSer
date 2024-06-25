package org.Sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * @Auther: Edge
 * @Date: 2024/6/22 14:01
 * @Description: TODO
 * @version: 1.0
 **/


public class userFind
{
    // 通过账户密码判断用户是否存在
    public boolean userIsExist(String username , String password)
    {

        /**
         * @description: 判断用户是否存在
         * @param:
         * @param username
         * @param password
         * @return: boolean
         * @author Edge
         * @date: 2024/6/22 14:01
         **/

        // 是否存在的标志
        boolean userExists = false;
        try
        {
            // 获取连接
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            // SQL语句
            String sql = "select 1 from users where username = ? and password = ?";
            // 传入参数
            userExists = Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql , Boolean.class , username , password));
            // 返回结果
            return userExists;
        }
        catch (DataAccessException e)
        {
            // 报错
            return userExists;
        }
    }

    // 判断用户名是否存在
    public boolean userNameExists(String username)
    {
        // 定义一个布尔值变量，默认为false
        boolean usernameExists = false;
        try
        {
            // 创建JdbcTemplate对象，用于操作数据库
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            // 定义sql语句，查询用户名是否存在
            String sql = "select 1 from users where username = ?";
            // 执行查询，将查询结果转换为Boolean类型
            usernameExists = Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql , Boolean.class , username));
            // true 就是存在，false就是不存在
            return usernameExists;
        }
        catch (DataAccessException e)
        {
            return usernameExists;
        }
    }

    // 通过用户名获取用户信息
    public Map<String, Object> getUserInfo(String username)
    {

        /**
         * @description: 通过用户名获取用户信息
         * @param:
         * @param username
         * @return: java.util.Map<java.lang.String , java.lang.Object>
         * @author Edge
         * @date: 2024/6/22 14:01
         **/

        try
        {
            //创建JdbcTemplate对象，用于操作数据库
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            //定义sql语句，根据用户名查询用户信息
            String sql = "select * from users where username = ?";
            //执行查询，返回查询结果
            return jdbcTemplate.queryForMap(sql , username);
        }
        catch (Exception e)
        {
            //捕获异常，返回null
            return null;
        }
    }

    public List<Map<String, Object>> getFriendList(String username)
    {

        /**
         * @description: 获取用户列表
         * @param:
         * @param username
         * @return: java.util.List<java.util.Map < java.lang.String , java.lang.Object>>
         * @author Edge
         * @date: 2024/6/22 14:01
         **/

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
                if (jedis.exists(username))
                {
                    List<Map<String, Object>> result = new ArrayList<>();
                    // 从redis中获取好友列表
                    List<String> friendListJson = jedis.lrange(username , 0 , -1);
                    // 反序列化json格式
                    for (String json : friendListJson)
                    {
                        Map<String, Object> map1 = mapper.readValue(json , Map.class);
                        result.add(map1);
                    }
                    return result;
                }
                try
                {
                    // 创建一个JdbcTemplate对象
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
                    // 定义SQL语句
                    String sql = "select friend_name from p2p_relationship where user_name = ?";
                    // 执行SQL语句，获取好友列表
                    List<Map<String, Object>> friendList = jdbcTemplate.queryForList(sql , username);
                    // 将好友信息存入redis
                    for (Map<String, Object> map : friendList)
                    {
                        // 转换json格式
                        String json = mapper.writeValueAsString(map);
                        jedis.rpush(username , json);
                    }
                    // 设置过期时间
                    jedis.expire(username , 60);
                    // 关闭redis连接
                    jedis.close();
                    return friendList;
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
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
    }
}
