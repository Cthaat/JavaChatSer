package org.http;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import static org.http.chatSocketSer.userSocketList;

public class chatSocketThread extends Thread
{
    private Socket socket;
    private String userName;

    public chatSocketThread(Socket socket)
    {
        this.socket = socket;
    }

    public void run()
    {
        try (InputStream inputStream = socket.getInputStream() ;
             DataInputStream dataInputStream = new DataInputStream(inputStream) ;
             OutputStream outputStream = socket.getOutputStream() ;
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream))
        {
            userName = dataInputStream.readLine();
            System.out.println("User " + userName + " connected");
            synchronized (userName)
            {
                userSocketList.put(userName, socket);
            }
            while (true)
            {
                String jsonMessage = dataInputStream.readLine();
                ObjectMapper mapper = new ObjectMapper();
                Map<String , String> messageMap = mapper.readValue(jsonMessage, Map.class);
                String message = messageMap.get("message");
                String receiver = messageMap.get("receiver");
                System.out.println("User " + userName + " sent message " + message + " to " + receiver);
                synchronized (receiver)
                {
                    Socket receiverSocket = userSocketList.get(receiver);
                    if (receiverSocket!= null)
                    {
                        dataOutputStream.writeUTF(jsonMessage);
                        dataOutputStream.flush();
                    }
                }
            }
        }
        catch (Exception e)
        {
            userSocketList.remove(userName);
        }
    }
}
