package org.http;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet ("/loadAllUsers")
public class loadAllMessage extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {
        this.doPost(req , resp);
    }

    @Override
    protected void doPost(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {
        // 获取请求中的Cookie列表
        Cookie[] cookies = req.getCookies();
        if (cookies!= null)
        {
            for (Cookie cookie : cookies)
            {
                if (cookie.getName().equals("username"))
                {

                }
            }
        }
    }
}
