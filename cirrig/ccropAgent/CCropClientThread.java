package CCROP; 
/**
 * Title:        CCropClientThread
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
// Class CCropClientThread extends UFMMTClientThread 

public class CCropClientThread extends UFMMTClientThread { 

    protected String _className = getClass().getName();

    public CCropClientThread( Socket clientSoc, int clientNumber, boolean sim) {
	super(clientSoc, clientNumber, sim);
    }


//----------------------------------------------------------------------------------------
    protected void setupStatusCheck() { }  //override for optional immediate status check.
    protected UFStrings setupImageCheck(String request) { return null; } //override
    protected UFStrings setupExecCheck(String request) { return null; }  //override

//----------------------------------------------------------------------------------------

    // sendZone() used to send Serializable Zone objects 
    public synchronized boolean sendZone(Zone zone) {
      synchronized(_clientSocket) {
System.out.println("SENDING ZONE "+zone.getId());
        try {
            //set socket timeout to finite value to try and detect send error:
            _clientSocket.setSoTimeout(_sendTimeOut);
            ObjectOutputStream outputStream = new ObjectOutputStream(_clientSocket.getOutputStream());
	    outputStream.writeObject(zone);
            //set socket timeout back to infinite so thread just waits for requests:
            _clientSocket.setSoTimeout(0);
            return true;
        }
        catch( IOException iox ) {
            System.out.println( _threadName + ".sendZone> " + iox.toString() );
            return false;
        }
        catch( Exception ex ) {
            System.out.println( _threadName + ".sendZone> " + ex.toString() );
            return false;
        }
      }
    }

} //end of class CCropClientThread
