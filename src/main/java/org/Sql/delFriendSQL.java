package org.Sql;

import java.sql.SQLException;

public interface delFriendSQL
{
    public boolean delFriendByUsername(String username , String friendname) throws SQLException;
}
