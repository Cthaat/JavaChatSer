package org.http;

import org.Sql.p2pRoomChat;
import org.Sql.userFind;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.http.logoInResp.MAPPER;

@WebServlet ("/getp2pMessages")
public class getp2pMessages extends HttpServlet
{

    @Override
    protected void doGet(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {
        this.doPost(req , resp);
    }

    @Override
    protected void doPost(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {
        String friendName = req.getHeader("friendName");
        if (friendName != null)
        // 获取请求中的Cookie列表
        {
            p2pRoomChat p2p = new p2pRoomChat();
            Cookie[] cookies = req.getCookies();
            if (cookies != null)
            {
                for (Cookie cookie : cookies)
                {
                    if (cookie.getName().equals("username"))
                    {
                        String userName = cookie.getValue();
                        List<Map<String, Object>> messages = p2p.getMessages(userName, friendName);
                        String json = MAPPER.writeValueAsString(messages);
                        resp.getWriter().write(json);
                    }
                }
            }
        }
    }
}
