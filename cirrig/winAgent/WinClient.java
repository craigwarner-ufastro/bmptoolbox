package ufWinAgent;
/**
 * Title:        WinClient
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Base class for java agents to override
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;

import javaUFProtocol.*;
import javaUFLib.*;
import javaMMTLib.*;

//=====================================================================================================

public class WinClient { 

    public static final
        String rcsID = "$Name:  $ $Id: WinClient.java,v 1.6 2011/10/26 17:11:15 warner Exp $";

    protected int _serverPort;
    protected String host = "localhost";
    protected String _hostname, _clientName;
    protected String _mainClass = getClass().getName();
    protected boolean _verbose = false;
    protected boolean _simMode = false;
    protected boolean _shutdown = false;
    protected boolean _isConnected = false;
    protected int _timeout = 30000;
    protected int count = 0;
    protected boolean doStopOnly = false, updateSoftware = false, resetSocket = false; 
    protected boolean restart = false, doListDir = false, getFile = false;
    protected boolean listTasks = false, killTasks = false, resetUsb=false;
    protected int _clientCount = 0;
    protected String argsToPass = "", taskToKill = "";
    protected Socket _clientSocket;
    public static String installDir = UFExecCommand.getEnvVar("BMPINSTALL");
    protected String fileToGet = "";
    protected String _dir = "..";
    protected String jar = "wthjec.jar";

//----------------------------------------------------------------------------------------

    public WinClient( int serverPort, String[] args )
    {
	System.out.println( rcsID );

	_serverPort = serverPort;
	System.out.println(_mainClass + "> server port = " + _serverPort);

	_clientSocket = null;
	String _connTime = ctime();
        options(args);

        try {
          _clientSocket = new Socket(host, _serverPort);
          _isConnected = true;
        } catch (IOException ioe) {
          System.out.println(_mainClass+"> "+ioe.toString());
          _isConnected = false;
          return;
        }
        _hostname = _clientSocket.getLocalAddress().getHostAddress();
	setArgsToPass(args);

	_clientName = host;
	System.out.println(_mainClass + "> time = " + _connTime);
	System.out.println(_mainClass + "> new connection from: " + _clientName);

	if( _simMode )
	    System.out.println(_mainClass + "> running in simulation mode.");
    }

//-----------------------------------------------------------------------------------------------------

    /** Handle command line options at startup */
    protected void options(String[] args) {
      for (int j = 0; j < args.length; j++) {
        if (args[j].toLowerCase().indexOf("-v") != -1) _verbose = true;
        else if (args[j].toLowerCase().indexOf("-sim") != -1) _simMode = true;
        else if (args[j].toLowerCase().indexOf("-timeout") != -1) {
          if (args.length > j+1) try {
            _timeout = Integer.parseInt(args[j+1]);
          } catch (NumberFormatException e) {}
        } else if (args[j].toLowerCase().indexOf("-host") != -1) {
          if (args.length > j+1) host = args[j+1];
        } else if (args[j].toLowerCase().indexOf("-jar") != -1) {
          if (args.length > j+1) jar = args[j+1];
	} else if (args[j].toLowerCase().indexOf("-stop") != -1) {
	  doStopOnly = true;
	} else if (args[j].toLowerCase().indexOf("-update") != -1) {
	  updateSoftware = true;
	} else if (args[j].toLowerCase().indexOf("-dir") != -1) {
	  _dir = args[j+1];
	} else if (args[j].toLowerCase().indexOf("-listdir") != -1) {
	  _dir = args[j+1];
	  doListDir = true;
        } else if (args[j].toLowerCase().indexOf("-get") != -1) {
          fileToGet = args[j+1];
          getFile = true;
	} else if (args[j].toLowerCase().indexOf("-tasklist") != -1) {
	  listTasks = true;
	} else if (args[j].toLowerCase().indexOf("-resetusb") != -1) {
	  resetUsb = true;
	} else if (args[j].toLowerCase().indexOf("-kill") != -1) {
	  taskToKill = args[j+1];
	  killTasks = true;
	}
      } 
    }

    protected void setArgsToPass(String[] args) {
      boolean skipNext = false;
      argsToPass = "-host "+_hostname;
      for (int j = 0; j < args.length; j++) {
	if (args[j].toLowerCase().indexOf("-dir") != -1 || args[j].toLowerCase().indexOf("-update") != -1) {
	  skipNext = true;
	  continue;
	}
	if (skipNext) {
	  skipNext = false;
	  continue;
	}
	argsToPass += " "+args[j];
      }
    }

//-----------------------------------------------------------------------------------------------------

    /** main loop */
    public void exec() {
      System.out.println(_mainClass + "::exec> starting service");
      UFStrings reply = null;

      if (doListDir) {
	reply = new UFStrings(_mainClass+": actionRequest", "DIR::"+_dir);
        System.out.println(_mainClass+"::exec> sending DIR::"+_dir);
        if (_send(reply) <= 0) {
          _terminate();
          return;
        }
        try {
          BufferedReader devIn = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream()));
          _clientSocket.setSoTimeout(0);
          String line = "";
          boolean keepGoing = true;
          while (line != null && keepGoing) {
            line = devIn.readLine();
            if (line == null) keepGoing = false;
            System.out.println(line);
            if (line.indexOf("ERROR") != -1 || line.indexOf("EOF") != -1) keepGoing = false;
          }
	  System.out.println("Done.");
        } catch (IOException e) {
          _terminate();
          return;
        }
        return;
      }

      if (getFile) {
        reply = new UFStrings(_mainClass+": actionRequest", "GET_FILE::"+fileToGet);
        System.out.println(_mainClass+"::exec> sending GET_FILE::"+fileToGet);
        if (_send(reply) <= 0) {
          _terminate();
          return;
        }
        UFProtocol ufpr = UFProtocol.createFrom( _clientSocket );
        String request = ufpr.name();
	if (request.indexOf("\\") == 0) request = request.substring(1);
	if (ufpr instanceof UFStrings) {
	  System.out.println(ufpr.toString());
	  if (request.equals("Error")) return;
	} else {
	  System.out.println("Received unknown object.");
	}
        ufpr = UFProtocol.createFrom( _clientSocket );
        request = ufpr.name();
        if (request.indexOf("\\") == 0) request = request.substring(1);
        if (ufpr instanceof UFBytes) {
          try {
            System.out.println(_mainClass+"::exec> Receiving file "+request);
            UFBytes byteStream = (UFBytes)ufpr;
            byte[] byteArray = byteStream.values();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(request));
            bos.write(byteArray, 0, byteArray.length);
            bos.flush();
            bos.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
	return;
      }

      if (listTasks) {
        reply = new UFStrings(_mainClass+": actionRequest", "LIST_TASKS::LIST_TASKS");
        System.out.println(_mainClass+"::exec> sending LIST_TASKS::LIST_TASKS");
        if (_send(reply) <= 0) {
          _terminate();
          return;
        }
        try {
          BufferedReader devIn = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream()));
          _clientSocket.setSoTimeout(0);
          String line = "";
          boolean keepGoing = true;
          while (line != null && keepGoing) {
            line = devIn.readLine();
            if (line == null) keepGoing = false;
            System.out.println(line);
            if (line.indexOf("ERROR") != -1 || line.indexOf("EOF") != -1) keepGoing = false;
          }
          System.out.println("Done.");
        } catch (IOException e) {
          _terminate();
          return;
        }
        return;
      }

      if (resetUsb) {
        reply = new UFStrings(_mainClass+": actionRequest", "RESET_USB::RESET_USB");
        System.out.println(_mainClass+"::exec> sending RESET_USB::RESET_USB");
        if (_send(reply) <= 0) {
          _terminate();
          return;
        }
        try {
          BufferedReader devIn = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream()));
          _clientSocket.setSoTimeout(0);
          String line = "";
          boolean keepGoing = true;
          while (line != null && keepGoing) {
            line = devIn.readLine();
            if (line == null) keepGoing = false;
            System.out.println(line);
            if (line.indexOf("ERROR") != -1 || line.indexOf("EOF") != -1) keepGoing = false;
          }
          System.out.println("Done.");
        } catch (IOException e) {
          _terminate();
          return;
        }
        return;
      }

      if (killTasks) {
        reply = new UFStrings(_mainClass+": actionRequest", "KILL::"+taskToKill);
        System.out.println(_mainClass+"::exec> sending KILL::"+taskToKill); 
        if (_send(reply) <= 0) {
          _terminate();
          return;
        }
        try {
          BufferedReader devIn = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream()));
          _clientSocket.setSoTimeout(0);
          String line = "";
          boolean keepGoing = true;
          while (line != null && keepGoing) {
            line = devIn.readLine();
            if (line == null) keepGoing = false;
            System.out.println(line);
            if (line.indexOf("ERROR") != -1 || line.indexOf("found") != -1) keepGoing = false;
          }
        } catch (IOException e) {
          _terminate();
          return;
        }
        return;
      }
      
      if (updateSoftware) {
	String[] infilenames = {"lib/javaUFProtocol.jar", "lib/javaUFLib.jar", "lib/javaMMTLib.jar", "bin/ufWinAgent.jar", "wthjec/wthjec.jar", "bmpjec/bmpjec.jar", "bin/BMPToolbox.jar"};
        String[] outfilenames = {"lib\\javaUFProtocol.jar", "lib\\javaUFLib.jar", "lib\\javaMMTLib.jar", "bin\\ufWinAgent.jar", "wthjec\\wthjec.jar", "bmpjec\\bmpjec.jar", "bin\\BMPToolbox.jar"};
	reply = new UFStrings(_mainClass+":update software", "update software");
	if (_send(reply) <= 0) {
	  _terminate();
	  return;
	}	
	for (int i = 0; i < infilenames.length; i++) {
	  File file = new File(installDir+"/"+infilenames[i]);
	  byte[] byteArray = new byte[(int)file.length()];
	  try {
	    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
	    bis.read(byteArray, 0, byteArray.length);
	  } catch(IOException e) {
	    e.printStackTrace();
	    _terminate();
	    return;
	  }
	  UFBytes byteStream = new UFBytes(_dir+"\\"+outfilenames[i], byteArray);
	  System.out.println(_mainClass + "::exec> sending "+infilenames[i]);
	  if (_send(byteStream) <= 0) {
	    _terminate();
	    return;
	  }
	}
        reply = new UFStrings("EOF", "EOF");
        if (_send(reply) <= 0) {
          _terminate();
          return;
        }
      }

      reply = new UFStrings(_mainClass+": actionRequest", "STOP_AGENT::"+jar);
      System.out.println(_mainClass+"::exec> sending STOP_AGENT::"+jar);
      if (_send(reply) <= 0) {
        _terminate();
        return;
      }

      if (!doStopOnly) {
        //wait before sending start to give stop time to complete
        hibernate(10000);
	reply = new UFStrings(_mainClass+": actionRequest", "START_AGENT::"+jar+" "+argsToPass);
	System.out.println(_mainClass+"::exec> sending START_AGENT::"+jar+" "+argsToPass);
        if (_send(reply) <= 0) {
          _terminate();
          return;
        }
        try {
	  BufferedReader devIn = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream()));
	  _clientSocket.setSoTimeout(0);
	  String line = "";
	  boolean keepGoing = true;
	  while (line != null && keepGoing) {
	    line = devIn.readLine();
            if (line == null) keepGoing = false; 
	    System.out.println(line);
          }
	} catch (IOException e) {
	  _terminate();
	  return;
	}
      }
    }

//-----------------------------------------------------------------------------------------------------


    protected void hibernate() {
      //1000 millisec
      hibernate(1000);
    }

    protected void hibernate(int tout) {
      try {
        Thread.sleep(tout);
      } catch (InterruptedException e) {
        System.out.println(_mainClass + "::hibernate> error: "+e.toString());
      }
    }

    protected void shutdown() {
      System.out.println(_mainClass + "::shutdown> shutting down...");
      System.exit(0);
    }
//-----------------------------------------------------------------------------------------------------

    public String ctime() {
        String date = new Date( System.currentTimeMillis() ).toString();
        return( date.substring(4,19) + " LT");
    }

    public String getDateString() {
      /* Return String of format 2010Jan01 */
      String[] date = new Date(System.currentTimeMillis()-43200).toString().split(" ");
      return date[5]+date[1]+date[2];
    }

//----------------------------------------------------------------------------------------
	public void _terminate() {
          System.out.println(_mainClass + ".terminate> kill socket to ["+_clientName+"]");
	  try {
	    _clientSocket.close();
	  } catch(IOException e) { }
	  shutdown();
	}

        public String hostname() { return _hostname; }
	public void verbose( boolean talk ) { _verbose = talk; }
	public Socket _socket() { return _clientSocket; }



//----------------------------------------------------------------------------------------
	public synchronized int _send( UFProtocol ufpr ) {
	  synchronized(_clientSocket) {
	    try {
              //set socket timeout to finite value to try and detect send error:
              _clientSocket.setSoTimeout(_timeout);
              int nbytes = ufpr.sendTo( _clientSocket );
              //set socket timeout back to infinite so thread just waits for requests:
              _clientSocket.setSoTimeout(0);
              if( nbytes <= 0 ) System.out.println( _mainClass + ".send> zero bytes sent.");
              return nbytes;
            }
            catch( IOException iox ) {
              System.out.println( _mainClass + ".send> " + iox.toString() );
              return(-1);
            }
            catch( Exception ex ) {
              System.out.println( _mainClass + ".send> " + ex.toString() );
              return(-1);
            }
	  }
	}


} //end of class WinClient
