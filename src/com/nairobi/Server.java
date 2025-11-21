package com.nairobi;

import com.reader.LabConnectUtil;
import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.startServer();
    }

    public void startServer() throws Exception {

        String rootDrive = "/D:";
        String folderSuffix = "PACS_CONNECTOR";
        String propertyFileSuffix = "pacs-connector";
        // Reading properties from the pacs-connector.properties file
        ResourceBundle bundle = new PropertyResourceBundle(
                new FileInputStream(rootDrive + File.separator + folderSuffix + File.separator + propertyFileSuffix + ".properties")
        );

        String host = bundle.getString("pacs.host");
        int port = Integer.parseInt(bundle.getString("pacs.port"));
        String webServiceUrl = bundle.getString("pacs.updateReport.url");
        String sendingApp = bundle.getString("pacs.sendingApp");
        String sendingFac = bundle.getString("pacs.sendingFac");
        String recApp = bundle.getString("pacs.recApp");
        String recFac = bundle.getString("pacs.recFac");
        String controlID = bundle.getString("pacs.controlID");



        Socket socket=null;
        BufferedReader br=null;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log("Server started on port " + port);

            while (true) {
                // Accept client connection
                socket = serverSocket.accept();
                log("Client connected: " + socket.getInetAddress());

                // Read data from client
                InputStream is = socket.getInputStream();
                br= new BufferedReader(new InputStreamReader(is));

                StringBuilder receivedMessage = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    receivedMessage.append(line);
                    if (line.indexOf('\u001c') != -1) { // End of message
                        break;
                    }
                }

                log("Message received: " + receivedMessage.toString());

                String resp="Error in HL7 message format";

                try{

                    String hl7Message = addSegmentSeparators(receivedMessage.toString());

                    resp = webServiceCall(hl7Message,webServiceUrl);

                }catch (Exception e){

                    String timestamp=new SimpleDateFormat("yyyMMddHHmmss").format(new Date());

                    StringBuilder ackMsg =  new StringBuilder();
                    ackMsg.append("MSH|^~\\&|")
                            .append(recApp).append("|").append(recFac).append("|")
                            .append(sendingApp).append("|").append(sendingFac).append("|")
                            .append(timestamp).append("||ACK|")
                            .append(controlID).append("|P|2.3\r");

                    ackMsg.append("MSA|")
                            .append("AE")
                            .append("|")
                            .append(controlID)
                            .append("|")
                            .append(resp)
                            .append("|")
                            .append("|")
                            .append("|")
                            .append("\r");



                    resp=ackMsg.toString();


                }

                char END_OF_BLOCK = '\u001c';
                char START_OF_BLOCK = '\u000b';
                char CARRIAGE_RETURN = 13;

                String ackMessage = START_OF_BLOCK + resp + END_OF_BLOCK + CARRIAGE_RETURN;

                // Send acknowledgment
                OutputStream os = socket.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

                bw.write(ackMessage);
                bw.flush(); // Ensure data is sent immediately

                log("ACK sent to client and message is "+ackMessage);

                // Close resources
                br.close();
                bw.close();
//                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String webServiceCall(String data, String webServiceUrl) throws Exception {
        log("Ready to call the web service...");

        String decodedACKMessage = "";

        Map<String, Object> headerParamMap = new HashMap<>();
        headerParamMap.put("mednetOAuthApiToken", "607e2bb49932eedc15e24b0694dd7556732c909d");
        headerParamMap.put("loggedInUserID", "-1");
        headerParamMap.put("Content-Type", "text/plain");

        WebClient resultClient = WebClient.create(webServiceUrl);
        WebClient.getConfig(resultClient).getHttpConduit().getClient().setReceiveTimeout(60000);
        WebClient.getConfig(resultClient).getHttpConduit().getClient().setConnectionTimeout(30000);

        resultClient.type(MediaType.TEXT_PLAIN);
        resultClient.accept(MediaType.TEXT_PLAIN);

        for (Map.Entry<String, Object> entry : headerParamMap.entrySet()) {
            resultClient.header(entry.getKey(), entry.getValue());
        }

        try {

            data = data.replace("\u000B", "");
            data = data.replaceAll("\r", "\r\n");

            byte[] respArr = (data).getBytes(StandardCharsets.UTF_8);
            byte[] encodedRespArr = Base64.getEncoder().encode(respArr);
            String encodedString = new String(encodedRespArr, StandardCharsets.UTF_8);

            String encodedResponse = resultClient.post(encodedString, String.class);
            log("Received response from web service: "+encodedResponse);


            // Decode the Base64 string
            byte[] decodedBytes = Base64.getDecoder().decode(encodedResponse.getBytes(StandardCharsets.UTF_8));
            // Convert the decoded bytes into a string
            decodedACKMessage = new String(decodedBytes, StandardCharsets.UTF_8);
            // Print the decoded message
            //  String decodedACKMessage = encodedResponse;
            log(decodedACKMessage);

        } catch (ClientErrorException e) {
            log("Client Error occurred: HTTP Status Code: " + e.getResponse().getStatus());
            log("Error Response Body: " + e.getResponse().readEntity(String.class));
        }
        return decodedACKMessage;
    }

    public static void log(String message) {
        LabConnectUtil.log(message);
    }



    private static final String[] HL7_SEGMENTS = {
            "MSH", "PID", "PV1", "PV2", "ORC", "OBR", "OBX", "SOBX", "MSA", "NTE", "EVN", "IN1", "DG1", "TXA"
    };


    private static String addSegmentSeparators(String hl7Message) {
        if (hl7Message == null || hl7Message.isEmpty()) {
            return "";
        }

        String normalized = hl7Message.replace("\n", "").replace("\r", "");

        for (String segment : HL7_SEGMENTS) {
            if(!segment.equals("MSH")) {
                String target = segment + "|";
                String replacement = "\r" + segment + "|";
                normalized = normalized.replace(target, replacement);
            }
        }

        // Clean up any accidental double carriage returns introduced during replacement
        normalized = normalized.replaceAll("\r{2,}", "\r");

        return normalized;
    }
}
