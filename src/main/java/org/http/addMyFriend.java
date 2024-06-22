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


/**
 * @Auther: Edge
 * @Date: 2024/6/22 13:53
 * @Description: TODO
 * @version: 1.0
 **/


@WebServlet ("/addMyFriend")
public class addMyFriend extends HttpServlet
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
         * @date: 2024/6/22 13:53
         **/

        this.doPost(req , resp);
    }


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
         * @date: 2024/6/22 13:53
         **/

        // 获取请求头
        String addFriendUserName = req.getHeader("friendName");
        // 如果没有获取到请求头，则返回fail
        if (addFriendUserName == null)
        {
            resp.getWriter().println("fail");
            return;
        }
        // 获取cookies
        Cookie[] cookies = req.getCookies();
        // 如果cookies不为空，则遍历cookies
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                // 如果cookie的name为username，则获取username的值
                if (cookie.getName().equals("username"))
                {
                    String username = cookie.getValue();
                    // 实例化addFriend
                    addFriend add = new addFriend();
                    try
                    {
                        // 如果添加好友成功，则返回success
                        if (add.addFriendByUsername(username , addFriendUserName))
                        {
                            resp.getWriter().println("success");
                        }
                        // 如果添加好友失败，则返回fail
                        else
                        {
                            resp.getWriter().println("fail");
                        }
                    }
                    // 如果发生SQLException，则返回fail
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
