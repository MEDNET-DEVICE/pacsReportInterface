package com.nairobi;

public class Service {

    public void start(String[] args) {
        // Initialization code: start threads, open connections, etc.
        System.out.println("Service Started");

        try {
            Server server = new Server();
            server.startServer();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(String[] args) {
        // Clean-up code: close connections, stop threads, etc.
        System.out.println("Service Stopped");
    }
}
