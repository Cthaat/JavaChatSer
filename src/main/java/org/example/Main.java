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
        try (ServerSocket serverSocket = new ServerSocket(8888);)
        {
            while (true)
            {
                Socket socket = serverSocket.accept();
                new serverThread(socket).start();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}