package javaUFLib;

//Title:        UFAgentsControl
//Version:      (see rcsID)
//Author:       Frank Varosi
//Copyright:    Copyright (c) 2003-7
//Company:      University of Florida, Dept. of Astronomy.
//Description:  Start/stop/restart CanariCam agents.

import java.io.*;

//=======================================================================================
/**
 * Used by CanariCam Interface Server (CIS) (see CancamIfcServer.java).
 * @author Frank Varosi
 */

public class UFAgentsControl {

    public static final
	String rcsID = "$Name:  $ $Id: UFAgentsControl.java,v 1.9 2010/02/08 00:24:25 varosi Exp $";

    private String _className = getClass().getName();
    private String _prefix = "uf";
    private ProcessBuilder _pbStartAgents = new ProcessBuilder(_prefix+"start","-all");
    private ProcessBuilder _pbStopAgents = new ProcessBuilder(_prefix+"stop","-all");
    private ProcessBuilder _pbSimAgents = new ProcessBuilder(_prefix+"sim","-all");

    public UFAgentsControl() {
	_pbStartAgents.redirectErrorStream(true);
	_pbStopAgents.redirectErrorStream(true);
	_pbSimAgents.redirectErrorStream(true);
    }

    public UFAgentsControl(String prefix) {
	_prefix = prefix;
	_pbStartAgents = new ProcessBuilder(_prefix+"start","-all");
	_pbStopAgents = new ProcessBuilder(_prefix+"stop","-all");
	_pbSimAgents = new ProcessBuilder(_prefix+"sim","-all");
	_pbStartAgents.redirectErrorStream(true);
	_pbStopAgents.redirectErrorStream(true);
	_pbSimAgents.redirectErrorStream(true);
    }

    public boolean restartAgent( String devAgent ) { return restartAgent( devAgent, false ); }

    public boolean restartAgent( String devAgent, boolean simMode )
    {
	if( simMode ) {
	    ProcessBuilder pbRestartAgent = new ProcessBuilder(_prefix+"restart", devAgent,"-sim");
	    return execProcess( pbRestartAgent );
	}
	else {
	    ProcessBuilder pbRestartAgent = new ProcessBuilder(_prefix+"restart", devAgent);
	    return execProcess( pbRestartAgent );
	}
    }

    public boolean startAgent( String devAgent )
    {
	ProcessBuilder pbRestartAgent = new ProcessBuilder(_prefix+"restart", devAgent);
	return execProcess( pbRestartAgent );
    }

    public boolean stopAgent( String devAgent )
    {
	ProcessBuilder pbStopAgent = new ProcessBuilder(_prefix+"stop", devAgent);
	return execProcess( pbStopAgent );
    }

    public boolean simAgent( String devAgent )
    {
	ProcessBuilder pbRestartAgent = new ProcessBuilder(_prefix+"restart", devAgent,"-sim");
	return execProcess( pbRestartAgent );
    }

    public boolean startAll()
    {
	return execProcess( _pbStartAgents );
    }

    public boolean stopAll()
    {
	return execProcess( _pbStopAgents );
    }

    public boolean simAll()
    {
	return execProcess( _pbSimAgents );
    }
//----------------------------------------------------------------------------------------------------

    public boolean execProcess( ProcessBuilder prcbld )
    {
	try {
	    prcbld.redirectErrorStream(true);
	    Process prc = prcbld.start();
	    readProcess( prc );
	    return true;
	}
	catch( IOException iox ) {
	    System.err.println(iox.toString());
	    return false;
	}
	catch( Exception ex ) {
	    System.err.println(ex.toString());
	    return false;
	}
    }
//----------------------------------------------------------------------------------------------------

    public void readProcess( Process prc ) throws Exception
    {
	BufferedReader prcReader = new BufferedReader(new InputStreamReader(prc.getInputStream()));
	prc.waitFor();
	String reply = prcReader.readLine();
	while( reply != null ) {
	    System.out.println(_className + "> " + reply);
	    reply = prcReader.readLine();
	}
	prcReader.close();
    }
}
