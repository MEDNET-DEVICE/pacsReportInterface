package com.reader;

import com.connector.LabConnectorService;
import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class PacsServerReading {

    private Socket socket;

    public PacsServerReading(Socket socket) {
        this.socket = socket;
    }

    public static void log(String message) {
        LabConnectUtil.log(message);
    }

    private static String webServiceCall(String data, String webServiceUrl) throws Exception {
        log("Ready to call the web service...");

        Map<String, Object> headerParamMap = new HashMap<>();
        headerParamMap.put("mednetOAuthApiToken", "607e2bb49932eedc15e24b0694dd7556732c909d");
        headerParamMap.put("loggedInUserID", "-1");
        headerParamMap.put("Content-Type", "text/plain");

        WebClient resultClient = WebClient.create(webServiceUrl);
        WebClient.getConfig(resultClient).getHttpConduit().getClient().setReceiveTimeout(15000);

        resultClient.type(MediaType.TEXT_PLAIN);
        resultClient.accept(MediaType.TEXT_PLAIN);

        for (Map.Entry<String, Object> entry : headerParamMap.entrySet()) {
            resultClient.header(entry.getKey(), entry.getValue());
        }

        try {
            //            String hl7Message = "MSH|^~\\&|HIS|HISHL7|Synapse|20230517180000|20230517180000||ORU^R01|DI.23-24-4048|P|2.3|1|||||||" +
            //                    "PID|1||MRN-190016||43Y/M^GANGASAGAR^J PANDEY||20000514|M||||||||||AdmID||||||||||||" +
            //                    "PV1|1|OP|||9923:25985|||^Ref Phy|||||||||||9923:25985|50|||||||||||||||||||||||||||||||" +
            //                    "ORC|NW|MED:87825|MED:87825||||20230517180000||20230517180000|||REFERRING^DR. ^CASUALTY|||||4|||||||" +
            //                    "OBR|1|MED:87825|MED:87825|44211^CT SCAN OF ABDOMEN AND MESENTRIC ANGIOGRAPHY|ROUTINE||20230517180000||||||||" +
            //                    "&CT SCAN OF ABDOMEN AND MESENTRIC ANGIOGRAPHY|REFERRING^DR. ^CASUALTY||MEDNET|MR3812|SPSLocation|MEDNET|||CT" +
            //                    "|||20230517180000||||Reason ForProc|||SchPerPhyName||20230517180000|||||||||" +
            //                    "OBX||TX|44211^CT SCAN OF ABDOMEN AND MESENTRIC ANGIOGRAPHY||CT SCAN ABDOMEN AND PELVIS (TEST REPORT DO NOT DISPATCH).. .. ..Technique: MDCT imaging was performed using thin contiguous axial scan abdomen and pelvis with oral and IV contrast... ..Findings... Liver appears normal in size and shape. Intrahepatic biliary radicles are normal.. Gall bladder is normal. Spleen is normal. Pancreas appears normal. No evidence of focal lesions or dilatation of pancreatic duct is seen.. Both kidneys are normal. No evidence of calculi or hydronephrosis on either side.. Urinary bladder is normal. No evidence of wall thickening is seen.. Uterus, ovaries and adnex are normal in appearance . No evidence of para-aortic or pelvic lymphadenopathy is seen.. Small and large bowel loops (upto proximal half of transverse colon) are well opacified.Mild prominence of few small bowel calibre ((approximately) 2.5cm)are noted. . No evidence of undue dilatation or wall thickening of large bowel loops is seen.. No evidence of ascites is seen.. A well-defined non-enhancing loculaetd collection with tiny air foci measuring(approximately) 5.7 x 6 x 3.6cm is noted in subcutaneous plane in right infraumbilical region likely post op change. No obvious intra-abdominal communication/ extension is evident.. Ryle's tube, foley's bulb and a rectal catheter is noted in situ.. Bilateral pleural effusion with adjacent consolidation is noted.... ..COMMENTS:... A well-defined loculated subcutaneous collection in right infraumbilical region as described.. Mild prominence of few small bowel calibre noted. Adv- follow up if indicated.... ..(Suggested clinical correlation).. .. .||||||F|||20230517180000||5^Dr. STACY PATEL|";
            //
            //            String msg = "TVNIfF5+XCZ8SElTfEhJU0hMN3xTeW5hcHNlfDIwMjMwNTE3MTgwMDAwfDIwMjMwNTE3MTgwMDAwfHxPUlVeUjAxfERJLjIzLTI0LTQwNDh8UHwyLjN8MXx8fHx8fHwNClBJRHwxfHxNUk4tMTkwMDE2fHw0M1kvTV5HQU5HQVNBR0FSXkogUEFOREVZfHwyMDAwMDUxNHxNfHx8fHx8fHx8fEFkbUlEfHx8fHx8fHx8fHx8DQpQVjF8MXxPUHx8fDk5MjM6MjU5ODV8fHxeUmVmIFBoeXx8fHx8fHx8fHx8OTkyMzoyNTk4NXw1MHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8DQpPUkN8Tld8TUVEOjg3ODI1fE1FRDo4NzgyNXx8fHwyMDIzMDUxNzE4MDAwMHx8MjAyMzA1MTcxODAwMDB8fHxSRUZFUlJJTkdeRFIuIF5DQVNVQUxUWXx8fHx8NHx8fHx8fHwNCk9CUnwxfE1FRDo4NzgyNXxNRUQ6ODc4MjV8NDQyMTFeQ1QgU0NBTiBPRiBBQkRPTUVOIEFORCBNRVNFTlRSSUMgQU5HSU9HUkFQSFl8Uk9VVElORXx8MjAyMzA1MTcxODAwMDB8fHx8fHx8fCZDVCBTQ0FOIE9GIEFCRE9NRU4gQU5EIE1FU0VOVFJJQyBBTkdJT0dSQVBIWXxSRUZFUlJJTkdeRFIuIF5DQVNVQUxUWXx8TUVETkVUfE1SMzgxMnxTUFNMb2NhdGlvbnxNRURORVR8fHxDVHx8fDIwMjMwNTE3MTgwMDAwfHx8fFJlYXNvbiBGb3JQcm9jfHx8U2NoUGVyUGh5TmFtZXx8MjAyMzA1MTcxODAwMDB8fHx8fHx8fHwNCk9CWHx8VFh8NDQyMTFeQ1QgU0NBTiBPRiBBQkRPTUVOIEFORCBNRVNFTlRSSUMgQU5HSU9HUkFQSFl8fENUIFNDQU4gQUJET01FTiBBTkQgUEVMVklTIChURVNUIFJFUE9SVCBETyBOT1QgRElTUEFUQ0gpLi4gLi4gLi5UZWNobmlxdWU6IE1EQ1QgaW1hZ2luZyB3YXMgcGVyZm9ybWVkIHVzaW5nIHRoaW4gY29udGlndW91cyBheGlhbCBzY2FuIGFiZG9tZW4gYW5kIHBlbHZpcyB3aXRoIG9yYWwgYW5kIElWIGNvbnRyYXN0Li4uIC4uRmluZGluZ3MuLi4gTGl2ZXIgYXBwZWFycyBub3JtYWwgaW4gc2l6ZSBhbmQgc2hhcGUuIEludHJhaGVwYXRpYyBiaWxpYXJ5IHJhZGljbGVzIGFyZSBub3JtYWwuLiBHYWxsIGJsYWRkZXIgaXMgbm9ybWFsLiBTcGxlZW4gaXMgbm9ybWFsLiBQYW5jcmVhcyBhcHBlYXJzIG5vcm1hbC4gTm8gZXZpZGVuY2Ugb2YgZm9jYWwgbGVzaW9ucyBvciBkaWxhdGF0aW9uIG9mIHBhbmNyZWF0aWMgZHVjdCBpcyBzZWVuLi4gQm90aCBraWRuZXlzIGFyZSBub3JtYWwuIE5vIGV2aWRlbmNlIG9mIGNhbGN1bGkgb3IgaHlkcm9uZXBocm9zaXMgb24gZWl0aGVyIHNpZGUuLiBVcmluYXJ5IGJsYWRkZXIgaXMgbm9ybWFsLiBObyBldmlkZW5jZSBvZiB3YWxsIHRoaWNrZW5pbmcgaXMgc2Vlbi4uIFV0ZXJ1cywgb3ZhcmllcyBhbmQgYWRuZXggYXJlIG5vcm1hbCBpbiBhcHBlYXJhbmNlIC4gTm8gZXZpZGVuY2Ugb2YgcGFyYS1hb3J0aWMgb3IgcGVsdmljIGx5bXBoYWRlbm9wYXRoeSBpcyBzZWVuLi4gU21hbGwgYW5kIGxhcmdlIGJvd2VsIGxvb3BzICh1cHRvIHByb3hpbWFsIGhhbGYgb2YgdHJhbnN2ZXJzZSBjb2xvbikgYXJlIHdlbGwgb3BhY2lmaWVkLk1pbGQgcHJvbWluZW5jZSBvZiBmZXcgc21hbGwgYm93ZWwgY2FsaWJyZSAoKGFwcHJveGltYXRlbHkpIDIuNWNtKWFyZSBub3RlZC4gLiBObyBldmlkZW5jZSBvZiB1bmR1ZSBkaWxhdGF0aW9uIG9yIHdhbGwgdGhpY2tlbmluZyBvZiBsYXJnZSBib3dlbCBsb29wcyBpcyBzZWVuLi4gTm8gZXZpZGVuY2Ugb2YgYXNjaXRlcyBpcyBzZWVuLi4gQSB3ZWxsLWRlZmluZWQgbm9uLWVuaGFuY2luZyBsb2N1bGFldGQgY29sbGVjdGlvbiB3aXRoIHRpbnkgYWlyIGZvY2kgbWVhc3VyaW5nKGFwcHJveGltYXRlbHkpIDUuNyB4IDYgeCAzLjZjbSBpcyBub3RlZCBpbiBzdWJjdXRhbmVvdXMgcGxhbmUgaW4gcmlnaHQgaW5mcmF1bWJpbGljYWwgcmVnaW9uIGxpa2VseSBwb3N0IG9wIGNoYW5nZS4gTm8gb2J2aW91cyBpbnRyYS1hYmRvbWluYWwgY29tbXVuaWNhdGlvbi8gZXh0ZW5zaW9uIGlzIGV2aWRlbnQuLiBSeWxlJ3MgdHViZSwgZm9sZXkncyBidWxiIGFuZCBhIHJlY3RhbCBjYXRoZXRlciBpcyBub3RlZCBpbiBzaXR1Li4gQmlsYXRlcmFsIHBsZXVyYWwgZWZmdXNpb24gd2l0aCBhZGphY2VudCBjb25zb2xpZGF0aW9uIGlzIG5vdGVkLi4uLiAuLkNPTU1FTlRTOi4uLiBBIHdlbGwtZGVmaW5lZCBsb2N1bGF0ZWQgc3ViY3V0YW5lb3VzIGNvbGxlY3Rpb24gaW4gcmlnaHQgaW5mcmF1bWJpbGljYWwgcmVnaW9uIGFzIGRlc2NyaWJlZC4uIE1pbGQgcHJvbWluZW5jZSBvZiBmZXcgc21hbGwgYm93ZWwgY2FsaWJyZSBub3RlZC4gQWR2LSBmb2xsb3cgdXAgaWYgaW5kaWNhdGVkLi4uLiAuLihTdWdnZXN0ZWQgY2xpbmljYWwgY29ycmVsYXRpb24pLi4gLi4gLnx8fHx8fEZ8fHwyMDIzMDUxNzE4MDAwMHx8NV5Eci4gU1RBQ1kgUEFURUx8";

            byte[] msgBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] encodedMsgBytes = Base64.getEncoder().encode(msgBytes);
            String encodedMsg = new String(encodedMsgBytes, StandardCharsets.UTF_8);

            String response = resultClient.post(encodedMsg, String.class);
            log("Received response from web service: " + response);
            return response;
        } catch (ClientErrorException e) {
            log("Client Error occurred: HTTP Status Code: " + e.getResponse().getStatus());
            log("Error Response Body: " + e.getResponse().readEntity(String.class));
            throw new Exception("Error during web service call", e);
        }
    }

//    public void startReading(String webServiceUrl) throws Exception {
//        log("Ready to start reading the data ............");
//
//        try {
//            InputStream is = socket.getInputStream();
//            BufferedReader br = new BufferedReader(new InputStreamReader(is));
//
//            String ack = "";
//            String msg;
//
//            // Continuously listen for data
//            while ((msg = br.readLine()) != null) {
//                ack += msg;
//                if (ack.contains("\u001c")) {
//                    log("Message received from PACS: " + ack);
//                    webServiceCall(ack, webServiceUrl);
//                    ack = "";
//                }
//            }
//        } catch (IOException e) {
//            log("Connection lost. Restarting service");
//            LabConnectorService.startService();
//        }
//    }

//    public void startReading(String webServiceUrl) throws Exception {
//        log("Ready to start reading the data ............");
//
//        String host = "172.16.1.28";
//        int port = 7878;
//
//        try (Socket socket = new Socket(host, port);
//             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
//
//            log("Connected to server: " + host + ":" + port);
//
//            StringBuilder ack = new StringBuilder();
//            String msg;
//
//            while ((msg = br.readLine()) != null) {
//                ack.append(msg);
//                if (ack.toString().contains("\u001c")) {
//                    log("Message received from PACS: " + ack);
//                    webServiceCall(ack.toString(), webServiceUrl);
//                    ack.setLength(0);  // Clear the buffer
//                }
//            }
//        } catch (IOException e) {
//            log("Connection lost. Restarting service");
//            LabConnectorService.startService();
//        }
//    }

    public void startReading(String webServiceUrl) {
        log("Ready to start reading the data ............");

        String host = "172.16.1.28";
        int port = 7878;

        while (true) {
            try (Socket socket = new Socket(host, port)) {
                log("Connected to server: " + host + ":" + port);

                InputStream is = socket.getInputStream();
                StringBuilder ack = new StringBuilder();
                int character;

                while ((character = is.read()) != -1) {  // Read byte-by-byte
                    ack.append((char) character);
                    if (character == '\u001c') {  // If end character is found
                        log("Message received from PACS: " + ack.toString().trim());
//                        webServiceCall(ack.toString(), webServiceUrl);
                        ack.setLength(0); // Reset buffer for next message
                    }
                }
            } catch (IOException e) {
                log("Connection lost. Retrying...");
                try {
                    Thread.sleep(5000); // Wait before retrying
                } catch (InterruptedException ignored) {}
            }
        }
    }




}