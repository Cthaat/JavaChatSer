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
        System.out.println("New chat receive socket thread started");
        String userName = "";
        try (InputStream inputStream = socket.getInputStream() ;
             DataInputStream dataInputStream = new DataInputStream(inputStream) ;)
        {
            System.out.println("Waiting for user name");
            userName = dataInputStream.readUTF();
            synchronized (userName)
            {
                System.out.println("User " + userName + " connected");
                userSocketList.put(userName , socket);
            }
            while (true)
            {
                String jsonMessage = dataInputStream.readUTF();
                System.out.println("Received message " + jsonMessage);
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> messageMap = mapper.readValue(jsonMessage , Map.class);
                String message = messageMap.get("message");
                String receiver = messageMap.get("username");
                System.out.println("User " + userName + " sent message " + message + " to " + receiver);
                Socket receiverSocket = userSocketList.get(receiver);
                if (receiverSocket != null)
                {
                    OutputStream senderOutputStream = receiverSocket.getOutputStream();
                    DataOutputStream senderDataOutputStream = new DataOutputStream(senderOutputStream);
                    Map<String, String> responseMap = new HashMap<>();
                    responseMap.put("username" , userName);
                    responseMap.put("message" , message);
                    String responseJson = mapper.writeValueAsString(responseMap);
                    senderDataOutputStream.writeUTF(responseJson);
                    senderDataOutputStream.flush();
                }
            }
        }
        catch (Exception e)
        {
            synchronized (userName)
            {
                userSocketList.remove(userName);
                System.out.println("User " + userName + " disconnected \n \n");
            }
        }
    }
}
