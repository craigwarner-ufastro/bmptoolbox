package cjec;

/**
 * Title:        MMTPol Java Engineering Console (MJEC)
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Status Thread for MJEC
 */

import java.util.*;
import java.io.*;
import java.net.*;
import javaUFProtocol.*;
import javaMMTLib.*;

public class StatusThread implements Runnable {
  public static final
        String rcsID = "$Name:  $ $Id: StatusThread.java,v 1.4 2011/02/07 17:38:58 warner Exp $";

  protected CjecFrame cjec;
  protected boolean _isRunning = true;
  protected AgentConfig config;
  protected String name;
  protected Socket statusSocket;
  protected boolean _shutdown = false, _verbose = false, _isConnecting = false, _isConnected = false;
  protected int _sleepPeriod = 5000, _timeout = 10000;
  private String _className = getClass().getName();
  private String _threadName;

  public StatusThread(CjecFrame cjec, AgentConfig config) {
    this.cjec = cjec;
    this.config = config;
    name = config.getName();
    _threadName = _className+"("+name+")";
    _isRunning = true;
    _shutdown = false;
  }

  public void shutdown() {                                                                           
    _shutdown = true;                                                                                
    _isConnected = false;                                                                            
    if (config.isConnected()) config.disconnect();                                                   
  }             

  public void verbose(boolean verbose) { _verbose = verbose; }

  public void pause() {
    _isConnected = false;
    try {
      Thread.sleep(_sleepPeriod);
    } catch(InterruptedException e) {}
  }

  public void restart() {
    _isConnecting = true;
    _isRunning = true;
  }

  public boolean isConnected() { return _isConnected; }
  public boolean isConnecting() { return _isConnecting; }

  public void run() {
    UFProtocol ufpr;
    long nreq = 0;
    int nulls = 0;
    int zeros = 0;
    while (!_shutdown) {
      if (!config.doConnect() || !_isRunning) {
        pause();
        continue;
      }
      _isConnecting = true;
      _isConnected = false;
      System.out.println(_threadName+"::run> connecting to "+name+" as status client "+ctime());
      try {
	statusSocket = config.connect();
	if (statusSocket == null) {	
          System.out.println(_threadName+"::run> Status socket is NULL "+ctime());
          config.disconnect();
	  pause();
	  continue;
	}
	UFTimeStamp greet = new UFTimeStamp(_threadName+": statusclient");
        greet.sendTo(statusSocket);
        statusSocket.setSoTimeout(_timeout); //10 sec timeout
        ufpr = UFProtocol.createFrom( statusSocket );
	if (ufpr == null) {
	  System.out.println(_threadName+"::run> received null object!  Closing socket!");
	  config.disconnect();
	  pause();
	  continue; 
	} else {
          String request = ufpr.name().toLowerCase();
          if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
            System.out.println(_threadName+"::run> connection established: "+request);
            _isRunning = true;
            _isConnecting = false;
            _isConnected = true;
          } else {
            System.out.println(_threadName+"::run> received "+request+".  Closing socket!");
            config.disconnect();
	    pause();
	    continue;
          }
        }
      } catch(Exception e) {
        System.out.println(_threadName+"::connectToServers> "+e.toString());
        config.disconnect();
	pause();
        continue;
      }
      /* Connection established here now enter isRunning loop */
      nreq = 0;
      nulls = 0;
      zeros = 0;
      while (_isRunning) {
	ufpr = UFProtocol.createFrom( statusSocket );
	++nreq;
	if( ufpr == null ) {
          System.out.println( _threadName +"> Recvd null object. "+ctime());
          if( ++nulls > 1) {
System.out.println(_threadName +"> DISCONNECTING at "+ctime());
            _isRunning = false;
            _isConnected = false;
            if (config.isConnected()) config.disconnect();
            break;
          }
        } else if( _handleRequest( ufpr ) <= 0 ) {
          System.out.println( _threadName +"> Recvd zero object. "+ctime());
          if (++zeros > 1) {
System.out.println(_threadName +"> Z DISCONNECTING at "+ctime());
            _isRunning = false;
            _isConnected = false;
            if (config.isConnected()) config.disconnect();
            break;
          }
        } else{
	  nulls = 0;
	  zeros = 0;
	}
      } /* End isRunning loop */
      cjecError.show("Error: Disconnected from "+name);
      pause();
    } /* End !shutdown loop */
  } /* end run */


  protected int _handleRequest(UFProtocol ufpr) {
    try {
      if (UFRecord.isUFRecord(ufpr)) {
        /* This is a UFRecord object */
        return handleRecord(new UFRecord(ufpr));
      } else {
        System.out.println(_threadName+"::_handleRequest> Received unknown status: "+ufpr.name());
      }
    } catch(Exception ex) {
      System.out.println(_threadName+"::_handleRequest> ERROR: "+ex.toString());
    }
    return 1;
  }

  protected int handleRecord(UFRecord rec) {
    if (_verbose) System.out.println(_threadName+"::handleRecord> Received record: "+rec.name()+" = "+rec.getValue());
    cjec.updateDatabase(rec);
    return 1;
  }

  public String ctime() {
      String date = new Date( System.currentTimeMillis() ).toString();
      return( date.substring(4,19) + " LT");
  }

}
