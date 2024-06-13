package org.Sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;

public class querySql
{
    public static boolean isHavaUser(String username , String password)
    {
        try
        {
            Connection conn = SQLUtils.getConnection();
            PreparedStatement ps = null;
            String sql = "select count(*) from users where username = ? and password = ?";
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            // 查询是否有该用户
            int count = jdbcTemplate.queryForObject(sql, Integer.class, username, password);
            if (count == 1)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    public static String getAllUserByJson()
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            String sql = "select * from users";
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            String json = mapper.writeValueAsString(result);
            return json;
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String getUserByID(int ID)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            String sql = "select * from users where id = ?";
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql , ID);
            String json = mapper.writeValueAsString(result);
            return json;
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
