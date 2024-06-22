package org.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


/**
 * @Auther: Edge
 * @Date: 2024/6/22 13:53
 * @Description: TODO
 * @version: 1.0
 **/


public class chatSocketSer implements Runnable
{
    public Socket userSocket = null;
    public static Map<String, Socket> userSocketList = new HashMap<>();

    public void getUserSocketConnections()
    {
        
        /**
         * @description: 获取用户连接的Socket连接
         * @param: 
         * @return: void
         * @author Edge
         * @date: 2024/6/22 13:54
         **/

        try (ServerSocket serverSocket = new ServerSocket(10086) ; )
        {
            // 创建一个ServerSocket，监听端口10086
            System.out.println("Server started on port 10086");
            while (true)
            {
                // 等待客户端连接
                userSocket = serverSocket.accept();
                System.out.println("New connection established with " + userSocket.getRemoteSocketAddress());
                // 创建一个新的线程，用于处理客户端的连接
                new Thread(new chatReceiveSocketThread(userSocket)).start();
            }
        }
        catch (IOException e)
        {
            System.out.println("Server socket exceptio");
        }
    }

    @Override
    public void run()
    {
        
        /**
         * @description: 启动Socket服务
         * @param: 
         * @return: void
         * @author Edge
         * @date: 2024/6/22 13:58
         **/

        this.getUserSocketConnections();
    }
}

