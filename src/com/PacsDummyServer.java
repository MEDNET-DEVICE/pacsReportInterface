package com;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class PacsDummyServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(9090);
        System.out.println("PACS Dummy Server Started...");

        Socket socket = serverSocket.accept(); // Accept connection from client
        System.out.println("Client Connected...");

        InputStream is = socket.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        OutputStream os = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(os, true);

        String msg;
        String ack = "";

        // Send 100 messages at 1-minute intervals
        for (int i = 1; i <= 100; i++) {

            String message = "MSH|^~\\&|HIS|HISHL7|Synapse|20230517180000|20230517180000||ORU^R01|DI.23-24-4048|P|2.3|1|||||||" +
                    "PID|1||MRN-190016||43Y/M^GANGASAGAR^J PANDEY||20000514|M||||||||||AdmID||||||||||||" +
                    "PV1|1|OP|||9923:25985|||^Ref Phy|||||||||||9923:25985|50|||||||||||||||||||||||||||||||" +
                    "ORC|NW|MED:87825|MED:87825||||20230517180000||20230517180000|||REFERRING^DR. ^CASUALTY|||||4|||||||" +
                    "OBR|1|MED:87825|MED:87825|44211^CT SCAN OF ABDOMEN AND MESENTRIC ANGIOGRAPHY|ROUTINE||20230517180000||||||||" +
                    "&CT SCAN OF ABDOMEN AND MESENTRIC ANGIOGRAPHY|REFERRING^DR. ^CASUALTY||MEDNET|MR3812|SPSLocation|MEDNET|||CT" +
                    "|||20230517180000||||Reason ForProc|||SchPerPhyName||20230517180000|||||||||" +
                    "OBX||TX|44211^CT SCAN OF ABDOMEN AND MESENTRIC ANGIOGRAPHY||CT SCAN ABDOMEN AND PELVIS (TEST REPORT DO NOT DISPATCH).. .. ..Technique: MDCT imaging was performed using thin contiguous axial scan abdomen and pelvis with oral and IV contrast... ..Findings... Liver appears normal in size and shape. Intrahepatic biliary radicles are normal.. Gall bladder is normal. Spleen is normal. Pancreas appears normal. No evidence of focal lesions or dilatation of pancreatic duct is seen.. Both kidneys are normal. No evidence of calculi or hydronephrosis on either side.. Urinary bladder is normal. No evidence of wall thickening is seen.. Uterus, ovaries and adnex are normal in appearance . No evidence of para-aortic or pelvic lymphadenopathy is seen.. Small and large bowel loops (upto proximal half of transverse colon) are well opacified.Mild prominence of few small bowel calibre ((approximately) 2.5cm)are noted. . No evidence of undue dilatation or wall thickening of large bowel loops is seen.. No evidence of ascites is seen.. A well-defined non-enhancing loculaetd collection with tiny air foci measuring(approximately) 5.7 x 6 x 3.6cm is noted in subcutaneous plane in right infraumbilical region likely post op change. No obvious intra-abdominal communication/ extension is evident.. Ryle's tube, foley's bulb and a rectal catheter is noted in situ.. Bilateral pleural effusion with adjacent consolidation is noted.... ..COMMENTS:... A well-defined loculated subcutaneous collection in right infraumbilical region as described.. Mild prominence of few small bowel calibre noted. Adv- follow up if indicated.... ..(Suggested clinical correlation).. .. .||||||F|||20230517180000||5^Dr. STACY PATEL|\u001c";

//            String message = "Message " + i + " to PACS sender \u001c"; // Message with HL7 end character
            writer.println(message); // Send message to the client
            System.out.println("Sent: " + message);

            Thread.sleep(60000); // Wait for 1 minute before sending the next message
        }

        // Continuously listen for incoming data
        while ((msg = br.readLine()) != null) {
            ack += msg;

            // End of message condition (HL7 end character '\u001c')
            if (ack.contains("\u001c")) {
                System.out.println("Message received from PACS Sender: " + ack);
                ack = ""; // Reset for the next message
            }
        }
    }
}
