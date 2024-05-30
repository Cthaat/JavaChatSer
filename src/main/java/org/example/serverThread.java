package org.example;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class serverThread extends Thread
{
    private final Socket tcpServerSocket;

    public serverThread(Socket socket)
    {
        this.tcpServerSocket = socket;
    }

    @Override
    public void run()
    {
        try (InputStream is = tcpServerSocket.getInputStream() ;
             DataInputStream dis = new DataInputStream(is) ;)
        {
            System.out.println("Accepted connection from " + tcpServerSocket.getRemoteSocketAddress());
            while (true)
            {
                String message = dis.readUTF();
                System.out.println("Received message: " + message + "   " + "from client: " + tcpServerSocket.getRemoteSocketAddress());
                if (message.equals("exit"))
                {
                    break;
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("Quit: " + e.getMessage() + " " + e.getClass().getName());
        }
    }
}
