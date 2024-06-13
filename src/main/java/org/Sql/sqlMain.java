package org.Sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class sqlMain
{
    public static void main(String[] args)
    {
        try
        {
            Connection conn = SQLUtils.getConnection();
            PreparedStatement ps = null;
            String sql = "insert into  users (id , name , username , password) values (? , ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1 , "10000001");
            ps.setString(2 , "Admin");
            ps.setString(3 , "admin");
            ps.setString(4 , "123456..a");
            ps.executeUpdate();
            SQLUtils.closeConnection(ps , conn);
        }
        catch (SQLException e)
        {
            System.out.println("SQL Exception: " + e.getMessage());
        }
    }
}
