package com.kleck.DistributedLogging;

import java.net.*;
import java.io.*;

public class LoggingServerThread extends Thread {
	//initialize socket and in and out buffers
	private Socket clientSocket = null;
	private LoggingServer ls = null;
	
	public LoggingServerThread (Socket clientSocket, LoggingServer loggingServer) {
		super("Thread For Server Number " + loggingServer.getServerNumber());
		this.ls = loggingServer;
		this.clientSocket = clientSocket;
	}
	
	//1 thread of the server
	//needs to do 3 things
	//1.  accept input from client socket
	//2.  process input using LoggingServerProtocol
	//3.  output results to client socket
	public void run() {
		try {
			PrintWriter serverToClient = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader clientToServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			//get client input and send to LoggingServerProtocol
			String inputFromClient;
			String outputToClient;
		    LoggingServerProtocol lsp = new LoggingServerProtocol();
		    
		    //is this needed?
		    //outputToClient = lsp.processInput(null);
		    //serverToClient.println(outputToClient);
			while ((inputFromClient = clientToServer.readLine()) != null) {
			    System.out.println("server processing "+ inputFromClient);
				//get the output from the LoggingServerProtocol
				if(inputFromClient.equals("END")) {
					clientSocket.close();
					ls.closeServer();
					break;
				}
				outputToClient = lsp.processInput(inputFromClient);
			    serverToClient.println(outputToClient);
			    serverToClient.flush();
			    outputToClient = "";
			}			

		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("herenow");
		}
	    System.out.println("closing client socket on server");
		//close the client socket
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
}
