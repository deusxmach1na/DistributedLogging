package com.kleck.DistributedLogging;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;


public class LoggingClient {
    public static void main(String[] args) throws IOException {
        /* 
        if (args.length != 2) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }
 		*/
        //String hostName = "localhost";
        //int portNumber = 6665;
        ArrayList<LoggingClientThread> lct = new ArrayList<LoggingClientThread>();
        
        //read hostname file
        Properties props = loadParams();
        String[] hosts = props.getProperty("servers").split(";");
        String command = "grep \"E3\" test.txt";
        
        //get BufferedReader to read input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        while((command = br.readLine()) != "exit") {
	        if(command.trim() != "") {
	        	//start 1 thread per host
		        for(int i=0;i<hosts.length;i++) {
		        	String[] host = hosts[i].split(",");
		        	if(host[0]!="" && host[1]!="") {
		        		System.out.println("Spawning Host " + host[0] + " And Port " + host[1]);
		                lct.add(new LoggingClientThread(host[0], Integer.parseInt(host[1]), command));
		                lct.get(i).start();	
		                System.out.println("Started Thread " + i);
		        	}
		        }
	        }
        
	        //wait for all threads to return
	        //does this need to adjust in case of server failure?
	        for(int i=0;i<lct.size();i++){
	        	try {
	        		System.out.println("joining threads");
					lct.get(i).join();
					//System.out.println("joining thread " + i);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
	        
        }    
        System.out.println("fin");
        

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
	    	System.out.println("Did not find hostname file.");
	    	e.printStackTrace();
	    }
	    
	    return props;
	}

}
