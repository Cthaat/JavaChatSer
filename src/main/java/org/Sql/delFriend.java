package org.Sql;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class delFriend implements delFriendSQL
{
    @Override
    public boolean delFriendByUsername(String username , String friendname) throws SQLException
    {
        TransactionSynchronizationManager.initSynchronization();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(SQLUtils.getDataSource());
        DataSource dataSource = jdbcTemplate.getDataSource();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try
        {
            connection.setAutoCommit(false);
            String sql = "delete from p2p_relationship where user_name = ? and friend_name = ?";
            int result1 = jdbcTemplate.update(sql , username , friendname);
            int result2 = jdbcTemplate.update(sql , friendname , username);
            if (result1 == 1 && result2 == 1)
            {
                connection.commit();
                return true;
            }
            connection.rollback();
            return false;
        }
        catch (Exception e)
        {
            connection.rollback();
            return false;
        }
        finally
        {
            connection.setAutoCommit(true);
            connection.close();
        }
    }
}
