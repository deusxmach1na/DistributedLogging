package com.kleck.DistributedLogging;

import java.net.*;
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
			//get client input and send to LoggingServerProtocol
		    LoggingServerProtocol lsp = new LoggingServerProtocol();
			String input = (String) clientToServer.readObject();
			System.out.println("server processing "+ input);
			String output = "";
			
			//get output 
			//could change this conditionally on input
			if(input.equals("generateRandomTestLogs")) {
				output = "Generated these files:\n" + lsp.generateRandomTestLogs(this.serverNumber);
			}
			else {
				output = lsp.executeGrep(input);
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
	
	
}
