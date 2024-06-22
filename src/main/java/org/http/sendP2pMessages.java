package org.http;

import org.Sql.p2pRoomChat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * @Auther: Edge
 * @Date: 2024/6/22 13:55
 * @Description: TODO
 * @version: 1.0
 **/


@WebServlet ("/sendP2pMessages")
public class sendP2pMessages extends HttpServlet
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
         * @date: 2024/6/22 13:55
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
         * @date: 2024/6/22 13:55
         **/

        // 获取请求头
        String friendUserName = req.getHeader("friendName");
        String message = req.getHeader("message");
        Cookie[] cookies = req.getCookies();
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if (cookie.getName().equals("username"))
                {
                    String username = cookie.getValue();
                    p2pRoomChat p2p = new p2pRoomChat();
                    // 发送消息
                    if (p2p.getSendMessage(username , friendUserName , message))
                    {
                        resp.getWriter().println("success");
                    }
                    else
                    {
                        resp.getWriter().println("fail");
                    }
                }
            }
        }
    }
}
