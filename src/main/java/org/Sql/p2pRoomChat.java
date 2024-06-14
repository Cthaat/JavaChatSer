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

public class p2pRoomChat implements p2pRoomChatSQL
{
    @Override
    public void loadMessagesFromTwoUsers(String user1 , String user2)
    {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = this.getClass().getResourceAsStream("/redis.properties") ;
        )
        {
            Properties properties = new Properties();
            properties.load(is);
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
                String sqlCount = "select count(*) from p2p_text where send_user_name = ? and recive_user_name = ? or send_user_name = ? and recive_user_name = ?";
                String sql = "select * from p2p_text where send_user_name = ? and recive_user_name = ? or send_user_name = ? and recive_user_name = ? limit ? offset ?";
                int totalCount = jdbcTemplate.queryForObject(sqlCount, Integer.class , user1 , user2 , user2 , user1);
                int pageSize = 10;
                int totalPages = (int) Math.ceil((double) totalCount / pageSize);
                Jedis jedis = pool.getResource();
                jedis.del("admin");
                for (int currentPage = 1; currentPage <= totalPages; currentPage++)
                {
                    List<Map<String, Object>> result = jdbcTemplate.queryForList(sql,user1 , user2 , user2 , user1, pageSize, (currentPage - 1) * pageSize);
                    for (Map<String, Object> row : result)
                    {
                        // 以json形式存入redis
                        JavaTimeModule javaTimeModule = new JavaTimeModule();
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        javaTimeModule.addSerializer(LocalDateTime.class,new LocalDateTimeSerializer(dateTimeFormatter));
                        mapper.registerModule(javaTimeModule);
                        String value = mapper.writeValueAsString(row);
                        Map<String, Object> map = mapper.readValue(value, Map.class);
                        jedis.rpush(user1+user2 , value);
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
}
