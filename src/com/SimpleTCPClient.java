package com;

import java.io.*;
import java.net.*;

public class SimpleTCPClient {
    public static void main(String[] args) {
        String host = "172.16.1.28";  // Server IP
        int port = 7878;  // Server Port

        try (Socket socket = new Socket(host, port);
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true);
             InputStream input = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

            for (int i = 1; i <= 100; i++) {
                String message = "Message " + i + "\u001c";  // Append delimiter
                writer.println(message);  // Send message with newline
                System.out.println("Sent: " + message);
                Thread.sleep(1000);  // Small delay to prevent overload
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
