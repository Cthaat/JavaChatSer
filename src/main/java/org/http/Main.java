package org.http;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet ("/hello")
public class Main extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {
        this.doPost(req , resp);
    }

    @Override
    protected void doPost(HttpServletRequest req , HttpServletResponse resp) throws ServletException, IOException
    {
        String jsonString = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        System.out.println(jsonString);
        BufferedReader br = req.getReader();
        String line = null;
        while ((line = br.readLine())!= null) {
            System.out.println(line);
        }
        System.out.println(req.getHeader("userName"));
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().println("{\"message\":\"Hello World\"}");
    }
}