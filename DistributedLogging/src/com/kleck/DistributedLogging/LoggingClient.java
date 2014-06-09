package com.kleck.DistributedLogging;

import java.io.*;


public class LoggingClient {
    public static void main(String[] args) throws IOException {
        /* 
        if (args.length != 2) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }
 		*/
        String hostName = "localhost";
        int portNumber = 6665;
        
        //read 
        LoggingClientThread lct = new LoggingClientThread(hostName, portNumber);
        lct.start();
    }

}
