package org.http;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static org.http.chatSocketSer.userSocketList;

public class chatReceiveSocketThread extends Thread
{
    private final Socket socket;

    public chatReceiveSocketThread(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        // 打印日志
        System.out.println("New chat receive socket thread started");
        // 声明一个用户名变量
        String userName = "";
        // 使用try-with-resources语句，自动关闭资源
        try (InputStream inputStream = socket.getInputStream() ;
             DataInputStream dataInputStream = new DataInputStream(inputStream) ;)
        {
        // 等待用户名
            System.out.println("Waiting for user name");
            userName = dataInputStream.readUTF();
        // 使用synchronized关键字，保证用户名的线程安全
            synchronized (userName)
            {
                // 打印用户连接日志
                System.out.println("User " + userName + " connected");
                // 将用户名和socket放入map中
                userSocketList.put(userName , socket);
            }
            // 使用while循环，持续接收消息
            while (true)
            {
                // 读取消息
                String jsonMessage = dataInputStream.readUTF();
                System.out.println("Received message " + jsonMessage);
                // 将消息转换为Map
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> messageMap = mapper.readValue(jsonMessage , Map.class);
                // 获取消息内容和接收者用户名
                String message = messageMap.get("message");
                String receiver = messageMap.get("username");
                System.out.println("User " + userName + " sent message " + message + " to " + receiver);
                // // 根据接收者用户名，获取接收者socket
                Socket receiverSocket = userSocketList.get(receiver);
                // 如果接收者socket不为空，则发送消息
                if (receiverSocket != null)
                {
                    // // 获取接收者socket的输出流
                    OutputStream senderOutputStream = receiverSocket.getOutputStream();
                    DataOutputStream senderDataOutputStream = new DataOutputStream(senderOutputStream);
                    // // 创建响应Map
                    Map<String, String> responseMap = new HashMap<>();
                    // 将用户名和消息放入响应Map中
                    responseMap.put("username" , userName);
                    responseMap.put("message" , message);
                    // 将响应Map转换为json字符串
                    String responseJson = mapper.writeValueAsString(responseMap);
                    // 发送响应
                    senderDataOutputStream.writeUTF(responseJson);
                    senderDataOutputStream.flush();
                }
            }
        }
        // 捕获异常
        catch (Exception e)
        {
            // 使用synchronized关键字，保证用户名的线程安全
            synchronized (userName)
            {
                // 将用户名从map中移除
                userSocketList.remove(userName);
                // 打印用户断开连接日志
                System.out.println("User " + userName + " disconnected \n \n");
            }
        }
    }
}
