package org.http;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.Sql.userFind;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            String value = mapper.writeValueAsString(Find.getUserInfo(userName));
            resp.getWriter().println(value);
        }
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
