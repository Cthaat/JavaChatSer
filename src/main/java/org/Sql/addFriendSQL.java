package org.Sql;

import java.sql.SQLException;


/**
 * @Auther: Edge
 * @Date: 2024/6/22 13:55
 * @Description: TODO
 * @version: 1.0
 **/


public interface addFriendSQL
{

    /**
     * @param null
     * @description: a
     * @param:
     * @return:
     * @author Edge
     * @date: 2024/6/22 13:55
     **/

    public boolean addFriendByUsername(String username , String friendName) throws SQLException;
}
