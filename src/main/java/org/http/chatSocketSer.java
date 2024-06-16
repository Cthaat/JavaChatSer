package org.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class chatSocketSer implements Runnable
{
    public Socket userSocket = null;
    public static Map<String, Socket> userSocketList = new HashMap<>();

    public void getUserSocketConnections()
    {
        try (ServerSocket serverSocket = new ServerSocket(10086) ; )
        {
            System.out.println("Server started on port 10086");
            while (true)
            {
                userSocket = serverSocket.accept();
                System.out.println("New connection established with " + userSocket.getRemoteSocketAddress());
                new Thread(new chatSocketThread(userSocket)).start();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run()
    {
        this.getUserSocketConnections();
    }
}
