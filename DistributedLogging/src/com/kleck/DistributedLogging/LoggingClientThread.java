package com.kleck.DistributedLogging;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/* class to spawn new threads to connect to each server and issue user command
 * initialize communication with the server
 * needs to do the following
 * 1. connect to hostname, and portNumber
 * 2. take input from console/user
 * 3. return output from server
 */


public class LoggingClientThread extends Thread{
	private String hostName;
	private int portNumber;
	private String command;
	private String saveToFile;
	private boolean isLogTest;
	
	public LoggingClientThread(String hostName, int portNumber, String command, boolean isLogTest) {
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.command = command;
		this.isLogTest = isLogTest;
	}
    
	//called by LoggingClient when a user enters a command
	public void run() {
		try {
            Socket socket = new Socket(this.hostName, this.portNumber);
        	ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	String fromServer = "";
        	String fromUser = this.command;
        	
        	//send command to the server
    		if (fromUser != null) {
    			//System.out.println("Client Request: " + fromUser);
    			out.writeObject(fromUser);
    		}
    		
    		//prepare to print results to a file
    		this.saveToFile = "serverResponse_" + hostName + "_" + portNumber + ".out";
    		PrintWriter toFile = new PrintWriter(saveToFile, "UTF-8");
    		//get results from server and print to file for unit test
    		//print to console if it is not a Unit Test
    		while((fromServer = in.readLine()) != null) {
    			if(!this.isLogTest)
    				System.out.println(fromServer);
    			if(this.isLogTest)
    				toFile.println(fromServer);
    		}
    		
    		//tell server to close connection and clean up
    		out.writeObject(fromServer);
    		toFile.close();
        	socket.close();
        	out.close();
        	in.close();
        	
        	
        } catch (UnknownHostException e) {
            System.out.println("Host IP could not be determined.  Host Name: " + hostName);
        } catch (IOException e) {
            System.out.println("I/O Connection to " + hostName + " on port " + portNumber + " has failed.\n"
            		+ "Ensure a server is running on each port listed in servers.prop");
        } catch (Exception e) {
            System.out.println("General exception connecting to " + hostName + " port " + portNumber);
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
