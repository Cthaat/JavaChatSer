package org.Sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * @Auther: Edge
 * @Date: 2024/6/22 13:58
 * @Description: TODO
 * @version: 1.0
 **/


public class p2pRoomChat
{
    // 加载两个人对话的redis缓存
    public void loadMessagesFromTwoUsers(String user1 , String user2)
    {

        /**
         * @description: 加载对话
         * @param:
         * @param user1
         * @param user2
         * @return: void
         * @author Edge
         * @date: 2024/6/22 13:58
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
            try (JedisPool pool = new JedisPool(
                    config , properties.getProperty("redis.host") ,
                    Integer.parseInt(properties.getProperty("redis.port")) ,
                    Integer.parseInt(properties.getProperty("redis.timeout")) ,
                    properties.getProperty("redis.password") ,
                    Integer.parseInt(properties.getProperty("redis.database"))
            ) ;)
            {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
                // 查询两人对话的消息总数
                String sqlCount = "select count(*) from p2p_text where send_user_name = ? and recive_user_name = ? or send_user_name = ? and recive_user_name = ?";
                // 查询两人对话的消息列表
                String sql = "select * from p2p_text where send_user_name = ? and recive_user_name = ? or send_user_name = ? and recive_user_name = ? limit ? offset ?";
                // 执行SQL语句
                int totalCount = jdbcTemplate.queryForObject(sqlCount , Integer.class , user1 , user2 , user2 , user1);
                int pageSize = 10;
                // 计算总页数
                int totalPages = (int) Math.ceil((double) totalCount / pageSize);
                // 连接redis
                Jedis jedis = pool.getResource();
                jedis.del(user1 + user2);
                // 遍历每一页
                for (int currentPage = 1 ; currentPage <= totalPages ; currentPage++)
                {
                    // 存入redis
                    List<Map<String, Object>> result = jdbcTemplate.queryForList(sql , user1 , user2 , user2 , user1 , pageSize , (currentPage - 1) * pageSize);
                    for (Map<String, Object> row : result)
                    {
                        // 以json形式存入redis
                        JavaTimeModule javaTimeModule = new JavaTimeModule();
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        javaTimeModule.addSerializer(LocalDateTime.class , new LocalDateTimeSerializer(dateTimeFormatter));
                        mapper.registerModule(javaTimeModule);
                        String value = mapper.writeValueAsString(row);
                        Map<String, Object> map = mapper.readValue(value , Map.class);
                        jedis.rpush(user1 + user2 , value);
                        // 设置存活时间
                        jedis.expire(user1 + user2 , 60 * 60 * 24);
                    }
                }
                jedis.close();
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


    public List<Map<String, Object>> getMessages(String user1 , String user2)
    {

        /**
         * @description: 获取对话
         * @param:
         * @param user1
         * @param user2
         * @return: java.util.List<java.util.Map < java.lang.String , java.lang.Object>>
         * @author Edge
         * @date: 2024/6/22 13:59
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
                if (!jedis.exists(user1 + user2) && !jedis.exists(user2 + user1))
                {
                    this.loadMessagesFromTwoUsers(user1 , user2);
                }
                if (jedis.exists(user1 + user2) || jedis.exists(user2 + user1))
                {
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (jedis.exists(user1 + user2))
                    {
                        // 从redis中获取数据
                        List<String> messages = jedis.lrange(user1 + user2 , 0 , -1);
                        JavaTimeModule javaTimeModule = new JavaTimeModule();
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        javaTimeModule.addSerializer(LocalDateTime.class , new LocalDateTimeSerializer(dateTimeFormatter));
                        mapper.registerModule(javaTimeModule);
                        for (String message : messages)
                        {
                            // 转换json格式
                            Map<String, Object> map = mapper.readValue(message , Map.class);
                            result.add(map);
                        }
                    }
                    if (jedis.exists(user2 + user1))
                    {
                        // 从redis中获取数据
                        List<String> messages = jedis.lrange(user2 + user1 , 0 , -1);
                        JavaTimeModule javaTimeModule = new JavaTimeModule();
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        dateTimeFormatter.withZone(TimeZone.getTimeZone("Asia/Shanghai").toZoneId());
                        javaTimeModule.addSerializer(LocalDateTime.class , new LocalDateTimeSerializer(dateTimeFormatter));
                        mapper.registerModule(javaTimeModule);
                        for (String message : messages)
                        {
                            // 转换json格式
                            Map<String, Object> map = mapper.readValue(message , Map.class);
                            result.add(map);
                        }
                    }
                    return result;
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
        return null;
    }

    public boolean getSendMessage(String sendUser , String getUser , String message)
    {

        /**
         * @description: 发送消息
         * @param:
         * @param sendUser
         * @param getUser
         * @param message
         * @return: boolean
         * @author Edge
         * @date: 2024/6/22 13:59
         **/

        // 尝试执行代码
        try
        {
            // 创建JdbcTemplate对象，用于操作数据库
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            // 定义SQL语句，插入数据
            String sql = "insert into p2p_text (send_user_name , recive_user_name , text) values ( ? , ? , ? )";
            // 执行插入操作，返回受影响的行数
            int result = jdbcTemplate.update(sql , sendUser , getUser , message);
            // 如果受影响的行数为1，则加载消息，并返回true
            if (result == 1)
            {
                this.loadMessagesFromTwoUsers(sendUser , getUser);
                return true;
            }
            // 否则，返回false
            else
            {
                return false;
            }
        }
        // 发生异常，返回false
        catch (Exception e)
        {
            return false;
        }
    }
}
