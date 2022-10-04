package cjec;

/**
 * Title:        Cirrig Java Engineering Console (CJEC)
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Config for an agent connection 
 */

import java.util.*;
import java.io.*;
import java.net.*;

public class AgentConfig {

    public static final
        String rcsID = "$Name:  $ $Id: AgentConfig.java,v 1.2 2010/06/25 18:39:41 warner Exp $";

    protected String name;
    protected String hostname = "localhost";
    protected int port;
    protected boolean doConnect = false;
    protected boolean isConnected = false;
    String _mainClass = getClass().getName();
    Vector <Socket> sockets;
    protected boolean _verbose = false;

    public AgentConfig(String name, String hostname, int port, boolean doConnect) {
      this.name = name;
      this.hostname = hostname;
      this.port = port;
      this.doConnect = doConnect;
      sockets = new Vector(2);
    }

    public AgentConfig(String name, int port, boolean doConnect) {
      this(name, "localhost", port, doConnect);
    }

    public AgentConfig(String name, String hostname, int port) {
      this(name, hostname, port, true);
    }

    public AgentConfig(String name, int port) {
      this(name, "localhost", port, true);
    }

    public void setVerbosity(boolean v) {
      _verbose = v;
    }

    public String getName() { return name; }

    public boolean isConnected() { return isConnected; }

    public void setConnected(boolean connected) {
      isConnected = connected;
    }

    public boolean doConnect() { return doConnect; }

    public synchronized Socket connect() {
      Socket socket = null;
      try {
	socket = new Socket(hostname, port);
	isConnected = true;
        sockets.add(socket);
        System.out.println(_mainClass+"::connect> "+socket.toString());
      } catch (IOException ioe) {
        System.out.println(_mainClass+"::connect> "+ioe.toString());
	isConnected = false;
	return null;
      }
      return socket;
    }

    public synchronized void disconnect() {
      isConnected = false;
      System.out.println(_mainClass+"::disconnect> disconnecting sockets..."); 
      for (int j = 0; j < sockets.size(); j++) {
	try {
	  Socket soc = (Socket)sockets.get(j);
	  synchronized(soc) {
	    System.out.println(_mainClass+"::disconnect> "+soc.toString());
	    soc.close();
            System.out.println(_mainClass+"::disconnect> "+soc.isClosed()+" "+soc.isConnected());
	  }
	} catch (Exception ioe) {
          System.out.println(_mainClass+"::disconnect> "+ioe.toString());
	}
      }
      System.out.println(_mainClass+"::disconnect> Disconnected");
      sockets.clear();
    }
}
