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
		ArrayList<String> passedArgs = new ArrayList<String>();
		
		for(int i=0;i<args.length;i++) {
			passedArgs.add(args[i]);
		}
		
		//add blank arguments in case the user didn't send enough args
		for(int i=0;i<3;i++) {
			if(passedArgs.size() <= i) {
				passedArgs.add("");
			}
		}
		
		//get host ports from the host filename
		for(int i=0;i<hosts.length;i++) {
			hostPorts.add(new Integer(hosts[i].split(",")[1]));
		}
		
		//have the user either generateLogs
		if(passedArgs.get(0).equals("generateLogs")) {
			//are the test servers all localhost?
			if(passedArgs.get(2).equals("true"))
				startTestServers(hostPorts);
			System.out.println("Please wait. Generating Log Files...");
			//builds and starts a clientThread and issues command to generateLogs
			LoggingClient.startClientThreads(hosts, "generateLogs " + passedArgs.get(1), false);
			System.out.println("Logs have been generated on the servers.\n  "
					+ "Please move these to the client folder to run the unit test.\n");
		}
		//or run a grep test
		else if(passedArgs.get(0).equals("runUnitTest")) {
			//start some servers if they are all localhost
			if(passedArgs.get(2).equals("true"))
				startTestServers(hostPorts);
			
			//build grep command
			ArrayList<String> knownKeys = new ArrayList<String>();
			knownKeys.add("_ULTR_");
			knownKeys.add("_RARE_");
			knownKeys.add("_SOME_");
			knownKeys.add("_FREQ_");
			String command = "";
			int testNum = 0;
			
			//lets user select which test to do
			//as long as it's between 0 and 3
			try {
				testNum = Integer.parseInt(passedArgs.get(1));
				if(testNum >= knownKeys.size())
					testNum = 0;
				command = "grep \"" + knownKeys.get(testNum) + "\" ";
				System.out.println("Searching for pattern: " + knownKeys.get(testNum));
			}
			catch(NumberFormatException nfe) {
				command = "grep \"" + knownKeys.get(0) + "\" ";
				System.out.println("Searching for pattern: " + knownKeys.get(0));
			}
			//System.out.println(command);
			System.out.println("Please wait for the grep to complete...");
			long startTime = System.currentTimeMillis();
			LoggingClient.startClientThreads(hosts, command, true);	
			long stopTime = System.currentTimeMillis();
			System.out.println("TIME TO GREP: = " + (stopTime - startTime) + " milliseconds.");
			
			
			//get filenames for server logs
			//these files need to be moved to the client side
			//after being generated on the server side
			ArrayList<String> logFilenames = new ArrayList<String>();
			for(int i=0;i<hostPorts.size();i++) {
				logFilenames.add("machine." + i + ".log");
			}
			
			//get filenames for server output to compare
			//split hosts[i]
			ArrayList<String> greppedFilenames = new ArrayList<String>();
			for(int i=0;i<hosts.length;i++) {
				greppedFilenames.add("serverResponse_" + hosts[i].split(",")[0] + "_" + hosts[i].split(",")[1] + ".out");
			}
			
			//print the final results!!!!
			System.out.println(parseFilesAndCompare(logFilenames, greppedFilenames, command, hosts));	
		}
		else {
			System.out.println("Example use:\n"
					+ "java -cp DistributedLogging.jar com.kleck.DistributedLogging.LoggingUnitTests generateLogs X\n"
			        + "X is an optional integer between 0 and 5\n"
					+ "0 ~1 MB log file\n"
					+ "1 ~10 MB log file\n"
					+ "2 ~50 MB log file\n"
					+ "3 ~100 MB log file DEFAULT\n"
					+ "4 ~500 MB log file\n"
					+ "5 ~1000 MB log file\n"
					+ "java -cp DistributedLogging.jar com.kleck.DistributedLogging.LoggingUnitTests runUnitTest X\n"
			        + "X is an optional integer between 0 and 3\n"
					+ "0 is an ultra rare pattern        (p=.0001) DEFAULT\n"
					+ "1 is a rare pattern  			 (p=.003)\n"
					+ "2 is a somewhat frequent pattern  (p=.03)\n"
					+ "3 is a frequent pattern           (p=.3)");
		}
	}
	
	//start some servers from hostfile
	private static ArrayList<LoggingServer> startTestServers(ArrayList<Integer> hostPorts) {
		//start a server in a new thread for each hostPortNumber
		ArrayList<LoggingServer> servers = new ArrayList<LoggingServer>();
		for(int i=0;i<hostPorts.size();i++) {
			TestingThread tt = new TestingThread(hostPorts.get(i), i);
			tt.start();
			servers.add(tt.getLoggingServer());
		}
		return servers;	
	}
	
	//do a local grep
	private static String parseFilesAndCompare(ArrayList<String> logFilenames, ArrayList<String> greppedFilenames, String command, String[] hosts) {
		boolean isMatch = true;
		String result = "";
		String tempCommand = command;
		
        for(int i=0;i<hosts.length;i++) {
            ArrayList<String> localGrepResults = new ArrayList<String>();
            ArrayList<String> remoteGrepResults = new ArrayList<String>();
            
			//do a local grep on logFilenames and compare lineNumbers
        	command = tempCommand + logFilenames.get(i);
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
		        //System.out.println(greppedFilenames.get(i));
		        while ((line = br.readLine()) != null) {
		        	remoteGrepResults.add(line.split("#LINE_NUMBER#")[1]);
		        }
		        br.close();
		        //System.out.println("grep size" + localGrepResults.size());
		        //sort results
		        Collections.sort(remoteGrepResults);
		        Collections.sort(localGrepResults);
		        
		        //compare each element
		        for(int j=0;j<Math.min(remoteGrepResults.size(), localGrepResults.size());j++) {
		        	if(!remoteGrepResults.get(j).equals(localGrepResults.get(j))) {
		        		isMatch = false;		
		        		result = "FAIL**Test Failed Matching Elements From The Remote grep And Local grep**FAIL";
		        	}
		        }
		        
		        //fail if they are not the same size
		        if(remoteGrepResults.size()!=localGrepResults.size()) {
		        	isMatch = false;		
        			result = "FAIL**Results inconclusive.  Number of Results Do Not Match**FAIL";
		        }
		          
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
	LoggingServer loggingServer;
	
	public TestingThread (int port, int serverNumber) {
		this.port = port;
		this.serverNumber = serverNumber;
	}
	
	public void run() {
		this.loggingServer = new LoggingServer(this.port, this.serverNumber);
	}
	
	public LoggingServer getLoggingServer() {
		return this.loggingServer;
	}
	
}
