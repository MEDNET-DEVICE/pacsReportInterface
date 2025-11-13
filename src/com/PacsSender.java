package com;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class PacsSender {
    public static void main(String[] args) throws Exception {
        String host = "192.168.2.81";
        int port = 7070;

        // Establish connection to PACS Server
        Socket socket = new Socket(host, port);
        System.out.println("Connected to PACS Server...");

        OutputStream os = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(os, true);

        // Sending HL7 Data every 1 minute
        while (true) {
            String hl7Message = "MSH|^~\\&|PACS|Lab|MedNet|20240317||ORU^R01|12345|P|2.3\u001c\n";
            writer.println(hl7Message);
            System.out.println("HL7 Data Sent...");

            Thread.sleep(60000); // Send data every 1 minute
        }
    }
}
