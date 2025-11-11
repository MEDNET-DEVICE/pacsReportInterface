package com.connector;

import com.reader.LabConnectUtil;
import com.reader.PacsServerReading;

import java.io.File;
import java.io.FileInputStream;
import java.net.*;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class LabConnectorService {

    private static Socket socket;
    private String host;
    private int port;

    public LabConnectorService(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void log(String message) {
        LabConnectUtil.log(message);
    }

    // This Method is responsible to establish connection
    public Socket buildConnection() throws Exception {
        InetAddress address = InetAddress.getByName(host);
        socket = new Socket(address, port);

        if (!socket.isConnected()) {
            log("Server is Not Connected shutting down the application ");
            throw new Exception("Server is Not Connected");
        }
        log("Connected to server: " + socket.getInetAddress());

        return socket;
    }

    public static void startService() throws Exception {
        log("Ready to start the service");

        String rootDrive = "/D:";
        String folderSuffix = "PACS_CONNECTOR";
        String propertyFileSuffix = "pacs-connector";

        int restartAttempts = 0; // Counter for restart attempts
        int maxAttempts = 3; // Maximum restart attempts

        // Reading properties from the pacs-connector.properties file
        ResourceBundle bundle = new PropertyResourceBundle(
                new FileInputStream(rootDrive + File.separator + folderSuffix + File.separator + propertyFileSuffix + ".properties")
        );

//        String host = "localhost";
//        int port = 9090;
         String host = bundle.getString("pacs.host");
         int port = Integer.parseInt(bundle.getString("pacs.port"));
        String webServiceUrl = bundle.getString("pacs.updateReport.url");

        log("Host: " + host + ", Port: " + port + ", webServiceUrl : " + webServiceUrl);

        while (restartAttempts < maxAttempts) {
            Socket socket = null;
            try {
                LabConnectorService connection = new LabConnectorService(host, port);
                socket = connection.buildConnection();  // Establishing socket connection

                PacsServerReading reader = new PacsServerReading(socket);

                reader.startReading(webServiceUrl); // Started reading data

                return;

            } catch (Exception e) {
                restartAttempts++;
                log("Error: " + e.getMessage());
                log("Attempt " + restartAttempts + " of " + maxAttempts + " - Restarting service...");

                if (restartAttempts >= maxAttempts) {
                    log("Max restart attempts reached. Service will not restart further.");
                    break;
                }

                try {
                    Thread.sleep(60000);  // retry after 1 minutes
                } catch (InterruptedException ie) {
                    log("Error during wait: " + ie.getMessage());
                }
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (Exception e) {
                    log("Error during socket closing: " + e.getMessage());
                }
            }
        }
    }

    // Main entry point to start the service
    public static void main(String[] args) {
        try {
            startService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}