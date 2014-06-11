package com.kleck.DistributedLogging;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class LoggingClientThread extends Thread{
	//class to spawn new threads to connect to each server and collect logs
    //initialize communication with the server
    //needs to do the following
    //1. connect to hostname, and portNumber
    //2. take input from console/user
    //3. display output from server
	private String hostName;
	private int portNumber;
	private String command;
	private String saveToFile;
	
	public LoggingClientThread(String hostName, int portNumber, String command) {
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.command = command;
	}
    
	public void run() {
		try {
			
			//initialized socket and variables
            Socket socket = new Socket(this.hostName, this.portNumber);
        	ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	String fromServer = "";
        	String fromUser = this.command;
        	
        	//sent command to the server
    		if (fromUser != null) {
    			System.out.println("Client Request: " + fromUser);
    			out.writeObject(fromUser);
    		}
    		
    		//prepare to print results to a file
    		this.saveToFile = "output_" + hostName + "_" + portNumber + ".out";
    		PrintWriter toFile = new PrintWriter(saveToFile, "UTF-8");
    		//get results from server
    		while((fromServer = in.readLine()) != null) {
    			System.out.println("Server Response: " + fromServer);
    			toFile.println(fromServer);
    		}
    		
    		//tell server to close connection and clean up
    		out.writeObject(fromServer);
    		toFile.close();
        	//System.out.println("closing socket on client side");
        	socket.close();
        	
        	
        } catch (UnknownHostException e) {
            System.out.println("Don't know about host " + hostName);
        } catch (IOException e) {
            System.out.println("Couldn't get I/O for the connection to " + hostName + " port " + portNumber);
        }
	}
	
	//get the file that was created
	public String getSaveToFile() {
		return saveToFile;
	}

	//set where to save the file
	public void setSaveToFile(String saveToFile) {
		this.saveToFile = saveToFile;
	}
	
	

		
}
