package org.Sql;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

public class userFind implements userFindSQL
{
    // 通过账户密码判断用户是否存在
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

    // 判断用户名是否存在
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

    // 通过用户名获取用户信息
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
