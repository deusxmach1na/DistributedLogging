package com.kleck.DistributedLogging;

import java.net.*;
import java.io.*;

public class LoggingServer {
	//this code runs on each server or port
	//a server needs to be able to do 2 things
	//1.  listen for requests
	//2.  spin up new server thread
	private int portNumber;
	private int serverNumber;
	private ServerSocket serverSocket;
	
	//main method
	//get args and spin up the Logging Server
	public static void main (String args[]) {
		int port = 6665;
		int server = 1;
		
		//change port and server if args are passed
		if(args.length == 2) {
			port = Integer.parseInt(args[0]);
			server = Integer.parseInt(args[1]);
		}
		if(args.length < 2) {
			System.out.println("Using default port " + port + ", and default server Number " + server +".");
		}
		else {
			System.out.println("Using custom port " + port + ", and server Number " + server + ".");
		}
		new LoggingServer(port, server);
	}
	
	//LoggingServer constructor
	//needs to take a portNumber and the server id
	public LoggingServer(int portNumber, int serverNumber) {
		this.portNumber = portNumber;
		this.serverNumber = serverNumber;
		
		try {
			//initialize server socket
			this.serverSocket = new ServerSocket(this.portNumber);
		
			//create a new thread when client connects
			while(true) {
				LoggingServerThread lst = new LoggingServerThread(serverSocket.accept(), this.serverNumber);
				lst.start();
			}
		}	
		catch (BindException be) {
			System.out.println("Logging Server has already been started on port number " + portNumber + ".");
		}
		catch (IOException e) {
			System.out.println("I/O Error listening on port number " + portNumber + ".");
		}	 
		
	}

	//what server is this
	public int getServerNumber() {
		return this.serverNumber;
	}

}
