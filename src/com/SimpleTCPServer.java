package com;


import java.io.*;
import java.net.*;

public class SimpleTCPServer {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 7878;

        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(host, port));

        System.out.println("Server is running on " + host + ":" + port);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client connected: " + socket.getInetAddress());

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);
            }
        }
    }
}
