package org.http;

import org.Sql.addFriend;
import org.Sql.delFriend;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet ("/addMyFriend")
public class addMyFriend extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {
        this.doPost(req , resp);
    }

    @Override
    protected void doPost(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {
        // 获取请求头
        String addFriendUserName = req.getHeader("friendName");
        if (addFriendUserName == null)
        {
            resp.getWriter().println("fail");
            return;
        }
        Cookie[] cookies = req.getCookies();
        if (cookies!= null)
        {
            for (Cookie cookie : cookies)
            {
                if (cookie.getName().equals("username"))
                {
                    String username = cookie.getValue();
                    addFriend add = new addFriend();
                    try
                    {
                        if (add.addFriendByUsername(username, addFriendUserName))
                        {
                            resp.getWriter().println("success");
                        }
                        else
                        {
                            resp.getWriter().println("fail");
                        }
                    }
                    catch (SQLException e)
                    {
                        e.printStackTrace();
                        resp.getWriter().println("fail");
                    }
                }
            }
        }
    }
}
