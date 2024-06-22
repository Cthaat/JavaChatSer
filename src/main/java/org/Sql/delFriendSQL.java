package org.Sql;

import java.sql.SQLException;


/**
 * @Auther: Edge
 * @Date: 2024/6/22 13:58
 * @Description: TODO
 * @version: 1.0
 **/


public interface delFriendSQL
{

    /**
     * @param null
     * @description: a
     * @param:
     * @return:
     * @author Edge
     * @date: 2024/6/22 13:58
     **/

    public boolean delFriendByUsername(String username , String friendname) throws SQLException;
}
