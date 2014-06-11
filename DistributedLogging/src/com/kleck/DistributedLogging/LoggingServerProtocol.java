package com.kleck.DistributedLogging;

import java.io.*;
import java.util.Random;

public class LoggingServerProtocol {
	
	//execute grep command
	public String executeGrep(String input) {
		String results = "";
		String command = input;
		
		try {
            

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
	
	
	//execute grep command
	public String generateRandomTestLogs(int serverNumber) {
		//need to generate 2 logs per machine
		//1 with the following
		//1. rare keys
		//2. somewhat frequent keys
		//3. frequent keys
		//1 that is completely random
		String[] knownKeys = new String[4];
		knownKeys[0] = "RARE";
		knownKeys[1] = "SOME";
		knownKeys[2] = "FREQ";
		knownKeys[3] = "RAND";
		String filenames[] = new String[2];
		
		for(int i=0;i<2;i++) {
			//filename will be something like
			//server_1_KNOWN.log or server_2_RANDOM.log
			
			if(i==0)
				filenames[i] = "server_" + serverNumber + "_KNOWN.log";
			if(i==1)
				filenames[i] = "server_" + serverNumber + "_RANDOM.log";
			Random random = new Random();
			int linesToGenerate = 100000;
			int j = 0;			
			
			//open file writer and generate random lines
			PrintWriter toFile;
			try {
				toFile = new PrintWriter(filenames[i], "UTF-8");
				//generate random lines
				while(j < linesToGenerate) {
					int rand = random.nextInt(10000); //0-9999
					String keyValuePair = "";
					
					//RARE
					if(rand < 10) 
						keyValuePair = "{" + knownKeys[0] + ":" + knownKeys[0] + "}";
					//SOME
					else if(rand < 100)
						keyValuePair = "{" + knownKeys[1] + ":" + knownKeys[1] + "}";
					//FREQ
					else if(rand < 1000)
						keyValuePair = "{" + knownKeys[2] + ":" + knownKeys[2] + "}";
					//RAND
					else 
						keyValuePair = generateRandomLine();
					toFile.println(keyValuePair);
					j++;
				}
				toFile.close();			
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
					
		}
		return filenames[0] + "\n" + filenames[1];
	}

	
	//used to generate a random line in the logs
	private String generateRandomLine() {
		String alpha = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random random = new Random();
		String time = String.valueOf(System.nanoTime());
		String value = "";
		String results = "";
		int rand;
		
		//make the value 25 characters long
		for(int i=0;i<25;i++) {
			rand = random.nextInt(alpha.length());
			value += alpha.charAt(rand);
		}
		
		results = "{" + time + ":" + value + "}";
		
		return results;
	}
	
	
}
