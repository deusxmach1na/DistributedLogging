package com.kleck.DistributedLogging;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;


public class LoggingClient {
    public static void main(String[] args) throws IOException {
        
        //read hostname file
        Properties props = loadParams();
        String[] hosts = props.getProperty("servers").split(";");
        String command = "";
        
        //get BufferedReader to read input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        while((command = br.readLine()) != null) {
        	//break out of here if the user types exit
        	if(command.trim().equals("exit"))	
        		break;
        	
        	//otherwise start client threads
        	//and pass the command
        	startClientThreads(hosts, command, false);  
        }    
        //System.out.println("fin");    
    }

    //starts 1 client thread per host 
    //passes the command to each client thread
    //this method used in Testing
	public static void startClientThreads(String[] hosts, String command, boolean isLogTest) {
		//put ClientThreads in an arrayList for easier management
		ArrayList<LoggingClientThread> lct = new ArrayList<LoggingClientThread>();
		String tempCommand = command;
		//start each client thread and wait for
		//them to finish processing the command
		if(!command.trim().equals("")) {	
			//start 1 thread per host
		    for(int i=0;i<hosts.length;i++) {
		    	String[] host = hosts[i].split(",");
		    	if(!host[0].equals("") && !host[1].equals("")) {
		    		//add the server log file name if it is the unit test
		    		if(isLogTest) 
		    			command = tempCommand + "server_" + i + ".log";
		    		//System.out.println("Spawning Host " + host[0] + " And Port " + host[1]);
		            lct.add(new LoggingClientThread(host[0], Integer.parseInt(host[1]), command, isLogTest));
		            lct.get(i).start();	
		            //System.out.println("Started Thread " + i);
		    	}
		    }	        
		    
		    //wait for all threads to return
		    //does this need to handle server failure?
		    //TESTED...better method?
		    for(int i=0;i<lct.size();i++){
		    	try {
					lct.get(i).join();
					//System.out.println("joining thread " + i);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    }
		}

	
	}
    
	//open property file to get the hostName and portNumber
	public static Properties loadParams() {
	    Properties props = new Properties();
	    InputStream is = null;
	    
	    //load file
	    try {
	        File f = new File("servers.prop");
	        is = new FileInputStream(f);
	 
	        // Try loading properties from the file (if found)
	        props.load(is);
	        is.close();
	    }
	    catch (Exception e) { 
	    	System.out.println("Did not find hostname file. Ensure it is in the client folder.");
	    	e.printStackTrace();
	    }
	    
	    return props;
	}

}
