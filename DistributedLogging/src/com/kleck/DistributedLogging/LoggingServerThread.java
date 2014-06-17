package com.kleck.DistributedLogging;

import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.io.*;

public class LoggingServerThread extends Thread {
	//initialize socket and in and out buffers
	private Socket clientSocket = null;
	private int serverNumber;
	
	public LoggingServerThread (Socket clientSocket, int serverNumber) {
		//System.out.println("Server Thread Accepted Connection");
		this.clientSocket = clientSocket;
		this.serverNumber = serverNumber;
	}
	
	//1 thread of the server
	//needs to do 3 things
	//1.  accept input from client socket
	//2.  process input using LoggingServerProtocol
	//3.  output results to client socket
	public void run() {
		try {
			DataOutputStream serverToClient = new DataOutputStream(clientSocket.getOutputStream());
			ObjectInputStream clientToServer = new ObjectInputStream(clientSocket.getInputStream());
			//get client input and do something (either execute grep or generateLogs for unit test)
			String input = (String) clientToServer.readObject();
			String output = "";
			//could change this conditionally on input
			if(input.contains("generateLogs")) {
				output = "Generated these files:\n" + generateLogs(input);
			}
			else {
				output = executeGrep(serverToClient, input);
			}	
			
			//send output to client
			serverToClient.writeBytes(output);
			serverToClient.flush();

		    //System.out.println("closing client socket on server");
			//close the client socket
			this.clientSocket.close();

		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("IOException");
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("ClassNotFoundException");
		} 
	}
	
	
	//execute grep command
	public String executeGrep(DataOutputStream serverToClient, String input) {
		String results = "";
		String command = input;
		
		try {
            

            Process p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream())); 
            
            String s = "";
            // read the output from the command
            while ((s = stdInput.readLine()) != null) {
                serverToClient.writeBytes("Server #" + this.serverNumber + ":" + s + "\n");
            }
            while ((s = stdError.readLine()) != null) {
                serverToClient.writeBytes("Server #" + this.serverNumber + ":" + s + "\n");
            }
            System.out.println("Server executed command " + command);
			
        }
        catch (IOException e) {
            e.printStackTrace();
        }
		return results;
	}
	
	
	//generate random logs for unit testing
	//TESTING
	public String generateLogs(String input) {
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
		
		filename = "machine." + this.serverNumber + ".log";
		Random random = new Random();
		int linesToGenerate = 1100000;  //default to ~100 MB
		if(input.contains("0")) {
			linesToGenerate = 11000;  //~1 MB
		}
		else if(input.contains("1")) {
			linesToGenerate = 110000;  //~10 MB
		} 
		else if(input.contains("2")) {
			linesToGenerate = 550000;  //~50 MB
		} 
		else if(input.contains("3")) {
			linesToGenerate = 1100000;  //~100 MB
		} 
		else if(input.contains("4")) {
			linesToGenerate = 5500000;  //~500 MB
		} 
		else if(input.contains("5")) {
			linesToGenerate = 11000000;  //~1000 MB
		} 
		
		
		int j = 0;			
		
		//open file writer and generate random lines
		PrintWriter toFile;
		try {
			toFile = new PrintWriter(filename, "UTF-8");
			toFile.println("#LOG_FILE_FOR_SERVER_NUMBER_" + this.serverNumber + "#");
			
			//generate random lines
			while(j < linesToGenerate) {
				int rand = random.nextInt(10000); //0-9999
				String keyValuePair = "";
				
				//ULTRA RARE
				if(rand == 0) 
					keyValuePair = "#_SERVERNUMBER_#" + this.serverNumber + "{" + knownKeys.get(0) + ":" + knownKeys.get(0) + "}";
				//RARE
				else if(rand < 30) 
					keyValuePair = "#_SERVERNUMBER_#" + this.serverNumber + "{" + knownKeys.get(1) + ":" + knownKeys.get(1) + "}";
				//SOME
				else if(rand < 300)
					keyValuePair = "#_SERVERNUMBER_#" + this.serverNumber + "{" + knownKeys.get(2) + ":" + knownKeys.get(2) + "}";
				//FREQ
				else if(rand < 3000)
					keyValuePair = "#_SERVERNUMBER_#" + this.serverNumber + "{" + knownKeys.get(3) + ":" + knownKeys.get(3) + "}";
				//RAND
				else 
					keyValuePair = "#_SERVERNUMBER_#" + this.serverNumber + generateRandomLine();
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

	public int getServerNumber() {
		return this.getServerNumber();
	}
	
	
	
}
