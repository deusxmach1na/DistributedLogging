package com.kleck.DistributedLogging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class LoggingUnitTests {
	public static void main(String[] args) {
		
		//generate Random Test Logs
		if(args[0].equals("generateRandomTestLogs")) {
			try {
				String[] a = new String[2];
				a[0] = "6665";
				a[1] = "1";
				LoggingServer.main(a);	
				Thread.sleep(100);
				LoggingClient.main(args);
				Thread.sleep(100);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
				bw.write(args[0]);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//run grep test
		else if(args[0].equals("runUnitTest")){
			//
		}
		System.out.println("Testing Complete.");
	}
}
