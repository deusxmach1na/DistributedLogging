package com.kleck.DistributedLogging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

public class LoggingClientThread extends Thread{
	//class to spawn new threads to connect to each server and collect logs
    //initialize communication with the server
    //needs to do the following
    //1. connect to hostname, and portNumber
    //2. take input from console/user
    //3. display output from server
	private String hostName;
	private int portNumber;
	
	public LoggingClientThread(String hostName, int portNumber) {
		this.hostName = hostName;
		this.portNumber = portNumber;
	}
    
	public void run() {
		try {
			
			//initialized socket and variables
            Socket dlSocket = new Socket(this.hostName, this.portNumber);
        	PrintWriter out = new PrintWriter(dlSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(dlSocket.getInputStream()));
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
      
        	
        	String fromServer = null;
        	String fromUser = console.readLine();
    		if (fromUser != null) {
    			System.out.println("Client: " + fromUser);
    			out.println(fromUser);
    		}
        	//server output and user input
        	while ((fromServer = in.readLine()) != null) {    		

        		if (fromUser == "END") {
        			console.close();
        			dlSocket.close();
        		}
        		//fromUser = stdIn.readLine();
        		System.out.println("Server: " + fromServer);
        	}
        } catch (UnknownHostException e) {
            System.out.println("Don't know about host " + hostName);
        } catch (IOException e) {
            System.out.println("Couldn't get I/O for the connection to " + hostName);
        }
	}
	
	//open property file to get the hostName and portNumber
	public void loadParams() {
	    Properties props = new Properties();
	    InputStream is = null;
	 
	    //load file
	    try {
	        File f = new File("server.prop");
	        is = new FileInputStream( f );
	    }
	    catch (Exception e) { 
	    	is = null; 
	    }
	    
	    try {
	        if ( is == null ) {
	            // Try loading from classpath
	            is = getClass().getResourceAsStream("server.prop");
	        }
	 
	        // Try loading properties from the file (if found)
	        props.load( is );
	    }
	    catch ( Exception e ) { }
	 
	    this.hostName = props.getProperty("HostName", "localhost");
	    this.portNumber = new Integer(props.getProperty("PortNumber", "8080"));
	}
		
}
