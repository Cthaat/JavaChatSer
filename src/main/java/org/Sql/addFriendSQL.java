package org.Sql;

import java.sql.SQLException;

public interface addFriendSQL
{
    public boolean addFriendByUsername(String username , String friendName) throws SQLException;
}
