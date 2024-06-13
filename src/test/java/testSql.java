import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.Sql.SQLUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class testSql
{
    @Test
    public void getAllUser()
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            String sql = "select * from users";
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            String json = mapper.writeValueAsString(result);
            System.out.println(result);
            System.out.println(json);
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test2()
    {
        boolean userExists = false;
        try
        {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            String sql = "select 1 from users where username = ? and password = ?";
            userExists = Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql , Boolean.class , "admin" , "123456..aa"));
            System.out.println(userExists);
        }
        catch (DataAccessException e)
        {
            System.out.println(userExists);
        }
    }

    @Test
    public void test3()
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
                for (int currentPage = 1; currentPage <= totalPages; currentPage++)
                {
                    List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, pageSize, (currentPage - 1) * pageSize);
                    Jedis jedis = pool.getResource();
                    for (Map<String, Object> row : result)
                    {
                        // 以json形式存入redis
                        System.out.println(row);
                        JavaTimeModule javaTimeModule = new JavaTimeModule();
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        javaTimeModule.addSerializer(LocalDateTime.class,new LocalDateTimeSerializer(dateTimeFormatter));
                        mapper.registerModule(javaTimeModule);
                        String value = mapper.writeValueAsString(row);
                        System.out.println(value);
                        Map<String, Object> map = mapper.readValue(value, Map.class);
                        System.out.println(map);
                        jedis.rpush("text" , value);
                    }
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
