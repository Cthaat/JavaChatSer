package org.Sql;

import org.springframework.jdbc.core.JdbcTemplate;

public class addNewUser implements addNewUserSQL
{
    @Override
    public boolean addNewUser(String name , String username , String password)
    {
        try
        {
            userFind userFind = new userFind();
            if(userFind.userNameExists(username))
            {
                // 用户名已存在
                return false;
            }
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            String sql = "insert into users (name , username , password) values (?, ?, ?)";
            int result = jdbcTemplate.update(sql , name , username , password);
            return result > 0;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
