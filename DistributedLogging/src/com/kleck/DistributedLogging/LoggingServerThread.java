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
			DataOutputStream serverToClient = new DataOutputStream(clientSocket.getOutputStream());
			ObjectInputStream clientToServer = new ObjectInputStream(clientSocket.getInputStream());
			//get client input and send to LoggingServerProtocol
			String inputFromClient;
			String outputToClient;
		    LoggingServerProtocol lsp = new LoggingServerProtocol();
		    
		    //loop and get input from client
			while (true) {
				inputFromClient = (String)clientToServer.readObject();
			    System.out.println("server processing "+ inputFromClient);
				outputToClient = lsp.processInput(inputFromClient);	
				
				//get the output from the LoggingServerProtocol
				if(outputToClient.equals("END")) {
					clientSocket.close();
					ls.closeServer();
					break;
				}
			    serverToClient.writeBytes(outputToClient);
			    serverToClient.flush();
			    serverToClient.writeBytes("EOF");
			    serverToClient.flush();
			}	
		    System.out.println("closing client socket on server");
			//close the client socket
			this.clientSocket.close();

		}
		catch (IOException e) {
			//e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {
			//e.printStackTrace();
		}
		finally {
			try {
				System.out.println("closing client socket on server from finally");
				this.clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}
	
	
	
}
