package com.kleck.DistributedLogging;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

public class LoggingUnitTests {
	public static void main(String[] args) {
		//get hosts from file using LoggingClient loadParams()
		//read from host file for easier testing :)
		Properties props = LoggingClient.loadParams();
		String[] hosts = props.getProperty("servers").split(";");
		ArrayList<Integer> hostPorts = new ArrayList<Integer>();
		for(int i=0;i<hosts.length;i++) {
			hostPorts.add(new Integer(hosts[i].split(",")[1]));
		}
		
		//have the user either generateLogs
		if(args[0].equals("generateLogs")) {
			startTestServers(hostPorts);
			//builds and starts a clientThread and issues command to generateLogs
			LoggingClient.startClientThreads(hosts, "generateLogs", false);
			System.out.println("Logs have been generated on the servers.\n  "
					+ "Please move these to the client folder to run the unit test.");
		}
		//or run a grep test
		else if(args[0].equals("runUnitTest")){
			//start some servers
			startTestServers(hostPorts);
			
			//build grep command
			ArrayList<String> knownKeys = new ArrayList<String>();
			knownKeys.add("_ULTR_");
			knownKeys.add("_RARE_");
			knownKeys.add("_SOME_");
			knownKeys.add("_FREQ_");
			String command = "";
			
			//lets user select which test to do
			try {
				command = "grep \"" + knownKeys.get(Integer.parseInt(args[1])) + "\" ";
			}
			catch(NumberFormatException nfe) {
				command = "grep \"" + knownKeys.get(0) + "\" ";
			}
			System.out.println(command);
			LoggingClient.startClientThreads(hosts, command, true);		
			
			
			//get filenames for server logs
			//these files need to be moved to the client side
			//after being generated on the server side
			ArrayList<String> logFilenames = new ArrayList<String>();
			for(int i=0;i<hostPorts.size();i++) {
				logFilenames.add("server_" + i + ".log");
			}
			
			//get filenames for server output to compare
			//split hosts[i]
			ArrayList<String> greppedFilenames = new ArrayList<String>();
			for(int i=0;i<hosts.length;i++) {
				greppedFilenames.add("serverResponse_" + hosts[i].split(",")[0] + "_" + hosts[i].split(",")[1] + ".out");
			}
			
			//print the results
			System.out.println(parseFilesAndCompare(logFilenames, greppedFilenames, command, hosts));	
		}
	}
	
	//start some servers from hostfile
	private static void startTestServers(ArrayList<Integer> hostPorts) {
		//start a server in a new thread for each hostPortNumber
		for(int i=0;i<hostPorts.size();i++) {
			TestingThread tt = new TestingThread(hostPorts.get(i), i);
			tt.start();
		}	
	}
	
	//do a local grep
	private static String parseFilesAndCompare(ArrayList<String> logFilenames, ArrayList<String> greppedFilenames, String command, String[] hosts) {
		boolean isMatch = true;
		String result = "";
		
        for(int i=0;i<hosts.length;i++) {
            ArrayList<String> localGrepResults = new ArrayList<String>();
            ArrayList<String> remoteGrepResults = new ArrayList<String>();
			//do a local grep on logFilenames and compare lineNumbers
        	command = command + logFilenames.get(i);
        	//System.out.println(command);
			try {
				//do local grep
				Process p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
		        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream())); 
		        String s = "";
		        // read the output from the command
		        while ((s = stdInput.readLine()) != null) {
		        	//get the line number, guaranteed there are no other #LINE_NUMBER#'s
		        	localGrepResults.add(s.split("#LINE_NUMBER#")[1]);
		        }
		        stdInput.close();
		        
		        //get all the line numbers for the same server (the remote page)
		        BufferedReader br = new BufferedReader(new FileReader(greppedFilenames.get(i)));
		        String line;
		        System.out.println(greppedFilenames.get(i));
		        while ((line = br.readLine()) != null) {
		        	remoteGrepResults.add(line.split("#LINE_NUMBER#")[1]);
		        }
		        br.close();
		        
		        //sort results
		        Collections.sort(remoteGrepResults);
		        Collections.sort(localGrepResults);
		        
		        //compare each element
		        for(int j=0;j<Math.min(remoteGrepResults.size(), localGrepResults.size());j++) {
		        	if(!remoteGrepResults.get(j).equals(localGrepResults.get(j))) {
		        		isMatch = false;		
		        		result = "FAIL**Failed Matching Elements**FAIL";
		        	}
		        }
		        
		        /*
		        //fail if they are not the same size
		        if(remoteGrepResults.size()!=localGrepResults.size()) {
		        	isMatch = false;		
        			result = "FAIL**Failed On Size of Arrays**FAIL" + remoteGrepResults.size() + "\n" + localGrepResults.size();
		        }
		          */
		        if(!isMatch)
		        	break;
		        
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
        
        }
		if(isMatch)
			result = "SUCCESS**Local and Remote Greps Match!**SUCCESS";
        return result;

		
	}
}

//inner class
//called to start a new Logging server
class TestingThread extends Thread {
	int port;
	int serverNumber;
	
	public TestingThread (int port, int serverNumber) {
		this.port = port;
		this.serverNumber = serverNumber;
	}
	
	public void run() {
		new LoggingServer(this.port, this.serverNumber);
	}
}
