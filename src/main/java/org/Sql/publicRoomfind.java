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
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * @Auther: Edge
 * @Date: 2024/6/22 14:00
 * @Description: TODO
 * @version: 1.0
 **/


public class publicRoomfind
{
    // 将公共聊天室中的数据存入redis
    public void loadPublicRoomToRedis()
    {

        /**
         * @description: 从mysql中查询公共聊天室数据，并存入redis中
         * @param:
         * @return: void
         * @author Edge
         * @date: 2024/6/22 14:00
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
                // 创建JdbcTemplate对象
                JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
                // 查询总数SQL语句
                String sqlCount = "select count(*) from public_chat_room";
                // 查询数据SQL语句
                String sql = "select * from public_chat_room limit ? offset ?";
                // 查询总数
                int totalCount = jdbcTemplate.queryForObject(sqlCount , Integer.class);
                // mysql分页查询，每页十个
                int pageSize = 10;
                // 计算总页数
                int totalPages = (int) Math.ceil((double) totalCount / pageSize);
                // 创建redis连接
                Jedis jedis = pool.getResource();
                // 清空redis
                jedis.del("text");
                // 遍历每页数据
                for (int currentPage = 1 ; currentPage <= totalPages ; currentPage++)
                {
                    // 查询数据
                    List<Map<String, Object>> result = jdbcTemplate.queryForList(sql , pageSize , (currentPage - 1) * pageSize);
                    // 遍历每条数据，转换为json格式，存入redis
                    for (Map<String, Object> row : result)
                    {
                        // 以json形式存入redis
                        JavaTimeModule javaTimeModule = new JavaTimeModule();
                        // 时间格式化
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        javaTimeModule.addSerializer(LocalDateTime.class , new LocalDateTimeSerializer(dateTimeFormatter));
                        mapper.registerModule(javaTimeModule);
                        String value = mapper.writeValueAsString(row);
                        // 存入redis
                        jedis.rpush("text" , value);
                    }
                }
                // 关闭redis连接
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
}
