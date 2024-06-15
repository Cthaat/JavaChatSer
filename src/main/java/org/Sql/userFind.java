package org.Sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class userFind implements userFindSQL
{
    // 通过账户密码判断用户是否存在
    @Override
    public boolean userIsExist(String username , String password)
    {
        // 是否存在的标志
        boolean userExists = false;
        try
        {
            // 获取连接
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            // SQL语句
            String sql = "select 1 from users where username = ? and password = ?";
            // 传入参数
            userExists = Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql , Boolean.class , username , password));
            // 返回结果
            return userExists;
        }
        catch (DataAccessException e)
        {
            // 报错
            return userExists;
        }
    }

    // 判断用户名是否存在
    @Override
    public boolean userNameExists(String username)
    {
        // 定义一个布尔值变量，默认为false
        boolean usernameExists = false;
        try
        {
            // 创建JdbcTemplate对象，用于操作数据库
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            // 定义sql语句，查询用户名是否存在
            String sql = "select 1 from users where username = ?";
            // 执行查询，将查询结果转换为Boolean类型
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
            //创建JdbcTemplate对象，用于操作数据库
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            //定义sql语句，根据用户名查询用户信息
            String sql = "select * from users where username = ?";
            //执行查询，返回查询结果
            return jdbcTemplate.queryForMap(sql , username);
        }
        catch (Exception e)
        {
            //捕获异常，返回null
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getFriendList(String username)
    {
        try
        {
            // 创建一个JdbcTemplate对象
            JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
            // 定义SQL语句
            String sql = "select friend_name from p2p_relationship where user_name = ?";
            // 执行SQL语句，获取好友列表
            List<Map<String, Object>> friendList = jdbcTemplate.queryForList(sql , username);
            return friendList;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
