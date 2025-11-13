package com.test;

//import com.reader.LabConnectUtil;
import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Server {
    public static void main(String[] args) {
        int port = 7070;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                // Accept client connection
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress());

                // Read data from client
                InputStream is = socket.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                StringBuilder receivedMessage = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    receivedMessage.append(line);
                    if (line.indexOf('\u001c') != -1) { // End of message
                        break;
                    }
                }

                String ack = "";
                while (true) {
                    String msg = br.readLine();
                    ack = ack + msg;
                    if(msg.indexOf('\u001c') != -1){
                        break;
                    }
                }




                System.out.println("Message received: " + receivedMessage.toString());


                String resp = webServiceCall(receivedMessage.toString(),"http://192.168.2.57:9999/mednetLab/ws/modalityBrokerWS/updatePatientReport");

                char END_OF_BLOCK = '\u001c';
                char START_OF_BLOCK = '\u000b';
                char CARRIAGE_RETURN = 13;

                String ackMessage = START_OF_BLOCK + resp + END_OF_BLOCK + CARRIAGE_RETURN;

                // Send acknowledgment
                OutputStream os = socket.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

                bw.write(ackMessage);
                bw.flush(); // Ensure data is sent immediately

                System.out.println("ACK sent to client and message is "+ackMessage);

                // Close resources
                br.close();
                bw.close();
//                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String webServiceCall(String data, String webServiceUrl) throws Exception {
        log("Ready to call the web service...");

        Map<String, Object> headerParamMap = new HashMap<>();
        headerParamMap.put("mednetOAuthApiToken", "607e2bb49932eedc15e24b0694dd7556732c909d");
        headerParamMap.put("loggedInUserID", "-1");
        headerParamMap.put("Content-Type", "text/plain");

        WebClient resultClient = WebClient.create(webServiceUrl);
        //WebClient.getConfig(resultClient).getHttpConduit().getClient().setReceiveTimeout(15000);

        resultClient.type(MediaType.TEXT_PLAIN);
        resultClient.accept(MediaType.TEXT_PLAIN);

        for (Map.Entry<String, Object> entry : headerParamMap.entrySet()) {
            resultClient.header(entry.getKey(), entry.getValue());
        }

        try {

            data = data.replace("\u000B", "");

            byte[] encodedBytes = data.getBytes(StandardCharsets.UTF_8);
            String encodedString = Base64.getEncoder().encodeToString(encodedBytes);

            log("data is "+data);
            System.out.println("data is  "+data);
            System.out.println("encoded message is "+encodedString);
            String encodedResponse = resultClient.post(encodedString, String.class);
            log("Received response from web service: " + encodedResponse);



            // Decode the Base64 string
            byte[] decodedBytes = Base64.getDecoder().decode(encodedResponse);
            // Convert the decoded bytes into a string
            String decodedACKMessage = new String(decodedBytes, StandardCharsets.UTF_8);
            // Print the decoded message
            System.out.println(decodedACKMessage);
            log(decodedACKMessage);






            return decodedACKMessage;
        } catch (ClientErrorException e) {
            log("Client Error occurred: HTTP Status Code: " + e.getResponse().getStatus());
            log("Error Response Body: " + e.getResponse().readEntity(String.class));
            throw new Exception("Error during web service call", e);
        }
    }

    public static void log(String message) {
        //LabConnectUtil.log(message);
    }
}
