package org.Sql;

import org.springframework.jdbc.core.JdbcTemplate;


/**
 * @Auther: Edge
 * @Date: 2024/6/22 13:57
 * @Description: TODO
 * @version: 1.0
 **/


public class addNewUser implements addNewUserSQL
{
    @Override
    public boolean addNewUser(String name , String username , String password)
    {

        /**
         * @description: 添加新用户
         * @param:
         * @param name
         * @param username
         * @param password
         * @return: boolean
         * @author Edge
         * @date: 2024/6/22 13:57
         **/

        try
        {
            userFind userFind = new userFind();
            if (userFind.userNameExists(username))
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
