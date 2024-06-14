package org.Sql;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

public class userFind implements userFindSQL
{

    @Override
    public boolean userIsExist(String username , String password)
    {
        boolean userExists = false;
        try
        {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            String sql = "select 1 from users where username = ? and password = ?";
            userExists = Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql , Boolean.class , username , password));
            return userExists;
        }
        catch (DataAccessException e)
        {
            return userExists;
        }
    }

    @Override
    public boolean userNameExists(String username)
    {
        boolean usernameExists = false;
        try
        {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            String sql = "select 1 from users where username = ?";
            usernameExists = Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql , Boolean.class , username));
            // true 就是存在，false就是不存在
            return usernameExists;
        }
        catch (DataAccessException e)
        {
            return usernameExists;
        }
    }

    @Override
    public Map<String, Object> getUserInfo(String username)
    {
        try
        {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            String sql = "select * from users where username = ?";
            return jdbcTemplate.queryForMap(sql , username);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
