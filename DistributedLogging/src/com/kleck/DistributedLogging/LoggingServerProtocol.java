package com.kleck.DistributedLogging;

import java.io.*;

public class LoggingServerProtocol {
	
	//execute grep command
	public String processInput(String input) {
		String results = "";
		String command = "grep \"E3\" test.txt";
		
		try {
            
		    //run simple command
			System.out.println(command);
            Process p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream())); 
            
            String s = "";
            // read the output from the command
            while ((s = stdInput.readLine()) != null) {
                results = results + s + "\n";
            }
            
            while ((s = stdError.readLine()) != null) {
                results = results + s + "\n";
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
		return results;
	}
	
}
