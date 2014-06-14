package com.kleck.DistributedLogging;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class LoggingServerProtocol {
	
	//execute grep command
	public String executeGrep(String input, int serverNumber) {
		String results = "";
		String command = input;
		
		try {
            

            Process p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream())); 
            
            String s = "";
            // read the output from the command
            while ((s = stdInput.readLine()) != null) {
                results = results + "#_SERVERNUMBER_#" + serverNumber + s + "\n";
            }
            while ((s = stdError.readLine()) != null) {
                results = results + "#_SERVERNUMBER_#" + serverNumber + s + "\n";
            }
			
        }
        catch (IOException e) {
            e.printStackTrace();
        }
		return results;
	}
	
	
	//generate random logs for unit testing
	//TESTING
	public String generateLogs(int serverNumber) {
		//need to generate 1 log per machine
		//with the following
		//1. rare keys
		//2. somewhat frequent keys
		//3. frequent keys
		//4. bonus ultra rare
		//rest random
		ArrayList<String> knownKeys = new ArrayList<String>();
		knownKeys.add("_ULTR_");
		knownKeys.add("_RARE_");
		knownKeys.add("_SOME_");
		knownKeys.add("_FREQ_");
		String filename;
		
		filename = "server_" + serverNumber + ".log";
		Random random = new Random();
		int linesToGenerate = 1000000;
		int j = 0;			
		
		//open file writer and generate random lines
		PrintWriter toFile;
		try {
			toFile = new PrintWriter(filename, "UTF-8");
			toFile.println("#LOG_FILE_FOR_SERVER_NUMBER_" + serverNumber + "#");
			
			//generate random lines
			while(j < linesToGenerate) {
				int rand = random.nextInt(10000); //0-9999
				String keyValuePair = "";
				
				//ULTRA RARE
				if(rand == 0) 
					keyValuePair = "#_SERVERNUMBER_#" + serverNumber + "{" + knownKeys.get(0) + ":" + knownKeys.get(0) + "}";
				//RARE
				else if(rand < 30) 
					keyValuePair = "#_SERVERNUMBER_#" + serverNumber + "{" + knownKeys.get(1) + ":" + knownKeys.get(1) + "}";
				//SOME
				else if(rand < 300)
					keyValuePair = "#_SERVERNUMBER_#" + serverNumber + "{" + knownKeys.get(2) + ":" + knownKeys.get(2) + "}";
				//FREQ
				else if(rand < 3000)
					keyValuePair = "#_SERVERNUMBER_#" + serverNumber + "{" + knownKeys.get(3) + ":" + knownKeys.get(3) + "}";
				//RAND
				else 
					keyValuePair = "#_SERVERNUMBER_#" + serverNumber + generateRandomLine();
				toFile.println(keyValuePair + "#LINE_NUMBER#" + j);
				j++;
			}
			toFile.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return filename;
	}

	
	//used to generate a random line in the logs
	//TESTING
	private String generateRandomLine() {
		String alpha = "       0123456789AAAABBCCCDDDEEEEEEEFFFGGGHHHIJKLLLMMMNNNOOOOPPQRRRRRSSSSSTTTTTUUUVWXYZ";
		Random random = new Random();
		String value = "{" + String.valueOf(System.nanoTime()) + ":";
		
		//make the value 25 characters long
		for(int i=0;i<50;i++) {
			value += alpha.charAt(random.nextInt(alpha.length()));
		}
		return value + "}";
	}
	
	
}
