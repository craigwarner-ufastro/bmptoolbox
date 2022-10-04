package ufWinAgent;
/**
 * Title:        ufWinServer.java
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Main program to start MMT Image Server Agent
 */

import java.util.*;

public class ufWinServer {

    public static final String rcsID = "$Name:  $ $Id: ufWinServer.java,v 1.3 2011/05/02 21:16:26 warner Exp $";

    public ufWinServer(String[] args)
    {
	boolean _winServer = false, _winClient = false;
	int serverPort = 56007;
	for (int j = 0; j < args.length; j++) {
	  if (args[j].equals("-host")) {
	    _winClient = true;
	  }
	  if (args[j].equals("-winserver")) {
	    _winServer = true;
	  }
	  if (args[j].equals("-port")) {
	    if (args.length > j+1) {
	      try {
	        serverPort = Integer.parseInt(args[j+1]);
	      } catch(NumberFormatException nfe) { }
	    }
	  }
	}

	if (_winServer) {
	  WinAgent winAgent = new WinAgent(serverPort, args);
	  winAgent.exec(); 
	} else if (_winClient) {
	  WinClient winClient = new WinClient(serverPort, args);
	  winClient.exec();
	}
    }

    public static void main(String[] args) {
	System.out.println("Creating new Win Server agent...");
	new ufWinServer(args);
    }
}
