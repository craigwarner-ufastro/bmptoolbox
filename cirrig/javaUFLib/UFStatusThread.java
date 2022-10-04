package javaUFLib;

/**
 * Title:        UFStatusThread.java
 * Version:      (see rcsID)
 * Authors:      Frank Varosi and Shaun McDowell
 * Company:      University of Florida
 * Description:  Thread for updating client threads.
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.Time;

import javaUFProtocol.*;
import javaUFLib.*;

//=====================================================================================================
// Class StatusThread creates thread to send Status messages to status clients:
//=====================================================================================================
public class UFStatusThread extends Thread {
    protected String _className = getClass().getName();
    protected boolean _keepRunning = true;
    protected UFClientThread _currClient = null;
    protected int _LoopRepeatPeriod;
    
    protected Vector< UFClientThread > _clientThreads;
    protected List _syncList;
    protected LinkedList< UFProtocol > _statusMsgs;
    
    protected static int _nStatThreadCnt = 0;
    
    public UFStatusThread(Vector< UFClientThread > clientThreads, List syncList, LinkedList< UFProtocol > statusMsgs) {
	this(clientThreads, syncList, statusMsgs, 1000);
    }
    
    public UFStatusThread(Vector< UFClientThread > clientThreads, List syncList, 
		LinkedList< UFProtocol > statusMsgs, int LoopRepeatPeriod) {
	_clientThreads = clientThreads;
	_syncList = syncList;
	_statusMsgs = statusMsgs;
	_LoopRepeatPeriod = LoopRepeatPeriod;
    }

    public void setUpdatePeriod(int update) { _LoopRepeatPeriod = update; }
    
    public void run() {
	try {
	    System.out.println(_className + ".run> Thread # " + (++_nStatThreadCnt)
		    + " sending to " + _clientThreads.size() + " clients.");
	    UFProtocol ufpStatusMsg = null;

	    while( _keepRunning ) {	  

		synchronized( _syncList ) {
		    if( _statusMsgs.size() > 0 ) ufpStatusMsg = _statusMsgs.removeFirst();
		}

		if( ufpStatusMsg == null ) sleep( _LoopRepeatPeriod );
		else {

		    for( int i = 0; i < _clientThreads.size(); i++ )
		    {
			_currClient = _clientThreads.elementAt(i);
			if( _currClient.statusClient() ) {
			    if( _currClient._send( ufpStatusMsg ) <= 0 ) _currClient._terminate();
			}
		    }

		    ufpStatusMsg = null;
		    _currClient = null;
		    yield();
		}
	    }
	} catch (Exception ex) {
	    System.err.println(_className + ".run> "+ex.toString());
	    return;
	}
    }

    public void stopRunning() {
	_keepRunning = false;
	if( _currClient != null ) _currClient._terminate();
    }
}
