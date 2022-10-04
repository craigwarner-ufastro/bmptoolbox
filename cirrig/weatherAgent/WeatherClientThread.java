package Weather; 
/**
 * Title:        WeatherClientThread
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Base class for threads handling client requests 
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.Time;

import javaUFProtocol.*;
import javaUFLib.*;
import javaMMTLib.*;

//===========================================================================================
// Class WeatherClientThread extends UFMMTClientThread 

public class WeatherClientThread extends UFMMTClientThread { 

    protected String _className = getClass().getName();
    protected long _lastPing = -1;

    public WeatherClientThread( Socket clientSoc, int clientNumber, boolean sim) {
	super(clientSoc, clientNumber, sim);
    }


//----------------------------------------------------------------------------------------
    protected void setupStatusCheck() { }  //override for optional immediate status check.
    protected UFStrings setupImageCheck(String request) { return null; } //override
    protected UFStrings setupExecCheck(String request) { return null; }  //override


//----------------------------------------------------------------------------------------
    /* UFMMTClientThread method _handleRequest parses the request and either
     * performs actions necessary or calls helper methods which will be
     * overridden by subclasses.  Sends reply back to client if necessary.
     */
    protected int _handleRequest( UFProtocol ufpr )
    {
        String request = ufpr.name().toLowerCase();
	if (ufpr instanceof UFStrings && request.endsWith("ping")) {
          /* Received ping request */
	  String val = ((UFStrings)ufpr).stringAt(0);
          if (_verbose) System.out.println(_threadName+"::_handleRequest> received ping: "+val);
	  _lastPing = System.currentTimeMillis();
          UFStrings reply = new UFStrings(_className+": ping", _threadName+": ping");
          return _send(reply);
        }
	return super._handleRequest(ufpr);
    }

    public boolean getPingStatus() {
      /* Return false if its been 30 seconds since last ping received */
      if (_lastPing == -1) return true;
      long interval = (System.currentTimeMillis()-_lastPing)/1000;
      if (interval > 30) return false;
      return true;
    }

} //end of class WeatherClientThread
