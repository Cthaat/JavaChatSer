package org.http;


import org.Sql.userFind;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.http.logoInResp.MAPPER;


/**
 * @Auther: Edge
 * @Date: 2024/6/22 13:55
 * @Description: TODO
 * @version: 1.0
 **/


@WebServlet ("/loadAllFriendMessage")
public class loadAllFriendMessage extends HttpServlet
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

        // 获取请求中的Cookie列表
        Cookie[] cookies = req.getCookies();
        if (cookies != null)
        {
            userFind userFind = new userFind();
            for (Cookie cookie : cookies)
            {
                if (cookie.getName().equals("username"))
                {
                    String friendList = MAPPER.writeValueAsString(userFind.getFriendList(cookie.getValue()));
                    resp.getWriter().write(friendList);
                }
            }
        }
    }
}
