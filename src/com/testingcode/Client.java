package com.testingcode;

import java.io.*;
import java.net.Socket;

public class Client {
    private static final String SERVER_HOST = "localhost";  // ✅ Your server's local IP
    private static final int SERVER_PORT = 7878;  // ✅ Same port as the server

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) { // ✅ Connect to server
            System.out.println("Connected to server: " + SERVER_HOST + ":" + SERVER_PORT);

            // Send a message to the server
            OutputStream os = socket.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

            String message = "Hello Server\u001c\n";  // Add EOM character
            bw.write(message);
            bw.flush(); // Ensure message is sent immediately
            System.out.println("Message sent to server: " + message);

            // Receive acknowledgment from server
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
                if (line.indexOf('\u001c') != -1) { // End of message
                    break;
                }
            }

            System.out.println("ACK received from server: " + response.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
