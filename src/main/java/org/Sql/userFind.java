package org.Sql;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

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
            userExists = Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql , Boolean.class , "admin" , "123456..aa"));
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
}
