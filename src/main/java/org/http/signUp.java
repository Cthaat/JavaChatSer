package org.http;

import org.Sql.addNewUser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description: 创建新用户
 * @ClassName: signUp
 * @Author: Edge
 * @Date: 2024/6/25 21:04
 * @Version: 1.0
 */

@WebServlet ("/signUp")
public class signUp extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {

        /**
         * @description: 重定向到登录页面
         * @param:
         * @param req
         * @param resp
         * @return: void
         * @author Edge
         * @date: 2024/6/25 21:13
         **/

        this.doPost(req , resp);
    }

    @Override
    protected void doPost(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {

        /**
         * @description: 创建新用户
         * @param:
         * @param req
         * @param resp
         * @return: void
         * @author Edge
         * @date: 2024/6/25 21:13
         **/

        req.setCharacterEncoding("UTF-8");
        String username = req.getHeader("username");
        String password = req.getHeader("password");
        resp.setContentType("application/json");
        addNewUser adder = new addNewUser();
        boolean result = adder.addNewUser(username , username , password);
        if (result)
        {
            resp.getWriter().println("success");
        }
    }
}
