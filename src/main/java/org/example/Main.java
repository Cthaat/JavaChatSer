package org.example;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main
{
    public static void main(String[] args)
    {
        try (ServerSocket serverSocket = new ServerSocket(8888) ;
             Socket socket = serverSocket.accept() ;
             InputStream is = socket.getInputStream();
             DataInputStream dis = new DataInputStream(is))
        {
            while (true)
            {
                String message = dis.readUTF();
                System.out.println("Received message: " + message);
                if (message.equals("exit"))
                {
                    break;
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("Quit: " + e.getMessage());
        }
    }
}