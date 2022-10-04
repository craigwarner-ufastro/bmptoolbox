package BMPToolbox; 
/**
 * Title:        BmpPlcClientThread
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
// Class BmpPlcClientThread extends UFMMTClientThread 

public class BmpPlcClientThread extends UFMMTClientThread { 

    protected String _className = getClass().getName();

    public BmpPlcClientThread( Socket clientSoc, int clientNumber, boolean sim) {
	super(clientSoc, clientNumber, sim);
    }


//----------------------------------------------------------------------------------------
    protected void setupStatusCheck() { }  //override for optional immediate status check.
    protected UFStrings setupImageCheck(String request) { return null; } //override
    protected UFStrings setupExecCheck(String request) { return null; }  //override

//----------------------------------------------------------------------------------------

} //end of class BmpPlcClientThread
