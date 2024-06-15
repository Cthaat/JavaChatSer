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

@WebServlet("/logoInResp")
public class logoInResp extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {
        this.doPost(req , resp);
    }

    @Override
    protected void doPost(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json");
        String userName = req.getHeader("userName");
        String password = req.getHeader("password");
        userFind Find = new userFind();
        if(Find.userIsExist(userName , password))
        {
            Map<String, Object> userMap = Find.getUserInfo(userName);
            Set<String> keys = userMap.keySet();
            for (String key : keys)
            {
                Cookie cookie = new Cookie(key, userMap.get(key).toString());
                cookie.setMaxAge(60 * 60 * 24 * 365);
                cookie.setPath("/");
                resp.addCookie(cookie);
            }
        }
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
