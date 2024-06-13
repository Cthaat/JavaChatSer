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

public class publicRoomfind implements publicRoomFindSQL
{
    @Override
    public void loadPublicRoomToRedis()
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
                String sqlCount = "select count(*) from public_chat_room";
                String sql = "select * from public_chat_room limit ? offset ?";
                int totalCount = jdbcTemplate.queryForObject(sqlCount, Integer.class);
                int pageSize = 10;
                int totalPages = (int) Math.ceil((double) totalCount / pageSize);
                Jedis jedis = pool.getResource();
                jedis.del("text");
                for (int currentPage = 1; currentPage <= totalPages; currentPage++)
                {
                    List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, pageSize, (currentPage - 1) * pageSize);
                    for (Map<String, Object> row : result)
                    {
                        // 以json形式存入redis
                        JavaTimeModule javaTimeModule = new JavaTimeModule();
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        javaTimeModule.addSerializer(LocalDateTime.class,new LocalDateTimeSerializer(dateTimeFormatter));
                        mapper.registerModule(javaTimeModule);
                        String value = mapper.writeValueAsString(row);
                        jedis.rpush("text" , value);
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
