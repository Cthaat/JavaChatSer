package org.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.Sql.userFind;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.http.chatSocketSer.userSocketList;


/**
 * @Auther: Edge
 * @Date: 2024/6/22 13:55
 * @Description: TODO
 * @version: 1.0
 **/


@WebServlet ("/logoInResp")
public class logoInResp extends HttpServlet
{
    public static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {

        /**
         * @description: 处理GET请求
         * @param:
         * @param req
         * @param resp
         * @return: void
         * @author Edge
         * @date: 2024/6/22 13:55
         **/

        // 转发给POST方法
        this.doPost(req , resp);
    }

    @Override
    protected void doPost(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {

        /**
         * @description: 处理POST请求
         * @param:
         * @param req
         * @param resp
         * @return: void
         * @author Edge
         * @date: 2024/6/22 13:55
         **/

        // 设置响应头
        resp.setContentType("application/json");
        // 获取请求参数
        String userName = req.getHeader("userName");
        String password = req.getHeader("password");
        // 验证用户信息
        userFind Find = new userFind();
        // 验证成功
        if (Find.userIsExist(userName , password) && userSocketList.get(userName) == null)
        {
            // 设置cookie
            Map<String, Object> userMap = Find.getUserInfo(userName);
            Set<String> keys = userMap.keySet();
            // 遍历map，设置cookie
            for (String key : keys)
            {
                Cookie cookie = new Cookie(key , userMap.get(key).toString());
                cookie.setMaxAge(60 * 60 * 24 * 365);
                cookie.setPath("/");
                resp.addCookie(cookie);
            }
            // 响应成功
            resp.setStatus(HttpServletResponse.SC_OK);
            new Thread(new chatSocketSer()).start();
        }
    }
}
