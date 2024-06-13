import org.Sql.SQLUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.Sql.querySql.getAllUserByJson;

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
    public void test()
    {
        System.out.println(getAllUserByJson());
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
}
