package org.http;

import org.Sql.delFriend;
import org.Sql.userFind;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;


/**
 * @Auther: Edge
 * @Date: 2024/6/22 13:54
 * @Description: TODO
 * @version: 1.0
 **/


@WebServlet ("/delMyFriend")
public class delMyFriend extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {

        /**
         * @description: doGet方法
         * @param:
         * @param req
         * @param resp
         * @return: void
         * @author Edge
         * @date: 2024/6/22 13:54
         **/

        this.doPost(req , resp);
    }

    // 根据username删除好友
    @Override
    protected void doPost(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {

        /**
         * @description: doPost方法
         * @param:
         * @param req
         * @param resp
         * @return: void
         * @author Edge
         * @date: 2024/6/22 13:54
         **/

        // 获取请求头
        String delFriendUserName = req.getHeader("friendName");
        if (delFriendUserName == null)
        {
            resp.getWriter().println("fail");
            return;
        }
        Cookie[] cookies = req.getCookies();
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if (cookie.getName().equals("username"))
                {
                    String username = cookie.getValue();
                    delFriend del = new delFriend();
                    try
                    {
                        boolean flag = del.delFriendByUsername(username , delFriendUserName);
                        if (flag)
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
