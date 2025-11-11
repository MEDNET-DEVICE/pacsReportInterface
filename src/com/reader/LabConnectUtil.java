package com.reader;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class LabConnectUtil {

    private static final String LOG_ROOT_DRIVE = "/D:";
    private static final String LOG_DIRECTORY = LOG_ROOT_DRIVE + File.separator + "PACS_CONNECTOR" + File.separator + "logs";


    // Logging the messages to a file
	public static void log(String message) {
		try {
			File logDirectory = new File(LOG_DIRECTORY);
			if(!logDirectory.exists()){
				logDirectory.mkdirs();
			}
  
			// Get the current timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            // Format the log entry with the timestamp
            String logMessageWithTime = "[" + timestamp + "] " + message;

			PrintWriter out = new PrintWriter(new FileWriter(LOG_ROOT_DRIVE + File.separator+"PACS_CONNECTOR"+File.separator+"log"+File.separator+new SimpleDateFormat("dd-MM-yyyy").format(new Date())+"_pacsLog.txt", true), true);
			out.write("\r\n" +logMessageWithTime);
			out.close();

			System.out.println(logMessageWithTime);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
}
