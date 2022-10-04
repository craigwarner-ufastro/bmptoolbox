package ufWinAgent;
/**
 * Title:        WinAgent
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

public class WinAgent implements Runnable { 

    public static final
        String rcsID = "$Name:  $ $Id: WinAgent.java,v 1.10 2011/10/27 17:29:49 warner Exp $";

    protected int _serverPort;
    protected String _mainClass = getClass().getName();
    protected boolean _verbose = false;
    protected boolean _simMode = false;
    protected boolean _isExec = false;
    protected boolean _shutdown = false;
    protected boolean _isConnected = false;
    protected int _timeout = 30000;
    protected int count = 0;
    protected String _health = "GOOD";
    protected long _heartbeat = 0;
    protected boolean _hasLock = false;
    protected boolean _execConnected = false;
    protected int _clientCount = 0;

    protected Vector< WinClientThread > _clientThreads = new Vector(5);
    protected ListenThread _cListener = null;
    public static Object mutex = new Object();

//----------------------------------------------------------------------------------------

    public WinAgent( int serverPort, String[] args )
    {
	System.out.println( rcsID );

	_serverPort = serverPort;
	_isExec = false;
	System.out.println(_mainClass + "> server port = " + _serverPort);

	options(args);
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
        } 
      }
    }

//-----------------------------------------------------------------------------------------------------

    protected void _startupListenThread()
    {
	//start a ServerSocket listening thread to accept UFLib-UFProtocol messaging clients.
	// this always must be done after full setup above so that objects exist for client threads.

	System.out.println(_mainClass + "> starting Listening thread for client connections...");
	_cListener = new ListenThread();
	_cListener.start();
    }
//-----------------------------------------------------------------------------------------------------

    /** main loop */
    public void exec() {
      System.out.println(_mainClass + "::exec> starting service");
      /* connect to device and exec agent */
      /* start listen thread for clients */
      _startupListenThread();
      /* create and start main thread */
      Thread t = new Thread(this);
      t.start();
      int oldcount = -1;
      int ntries = 0;
      while (true) {
        if (_shutdown) {
          System.out.println(_mainClass + "::exec> termination signal recv'd, or shutdown command...");
          shutdown();
        }
	/* Check that main thread is alive and restart if its died. */
        if (count > oldcount) {
          ntries = 0;
          oldcount = count;
        } else ntries++;
        if (! t.isAlive()) {
          System.out.println(_mainClass + "::exec> Thread "+t.getId()+" died!");
          t = new Thread(this);
          t.start();
          System.out.println(_mainClass + "::exec> Starting new thread: "+t.getId());
          ntries = 0;
        }
        hibernate();
        if (_verbose && count%20 == 0) System.out.println("Thread "+t.getId()+": "+t.getState());
      }
    }

    /* This method is where commands are processed and sent to the device sockets */
    protected boolean action(WinClientThread ct) {
      /* First increment count */
      synchronized(mutex) { count++; }
      UFStrings req = ct.getRequest();
      UFStrings reply = null;
      int nreq = 0;
      if (req == null) {
	System.err.println(_mainClass+"::action> Received null request!");
	return false;
      }
      String clientName = req.name().substring(0, req.name().indexOf(":"));
      boolean success = false;

      nreq = req.numVals();
      System.out.println(_mainClass+"::action> Received new request.");
      System.out.println("\tClient Thread: "+ct.getThreadName());
      System.out.println("\tRequest Name: "+req.name());
      System.out.println("\tRequest Size: "+nreq);
      for (int j = 0; j < nreq; j++) {
        System.out.println("\tRequest "+(j+1)+": "+req.stringAt(j));
      }

      /* Do work here */
      success = true;
      for (int j = 0; j < nreq; j++) {
        if (getCmdName(req,j).equals("STOP_AGENT")) {
          String name = getCmdParam(req,j);
	  if (name.toLowerCase().indexOf("_local") != -1) name = name.substring(0, name.indexOf("_local")-1);
	  /* First find pid for agent */
	  Vector jpsParams = new Vector();
	  jpsParams.add("jps");
	  jpsParams.add("-lm");
	  System.out.println(_mainClass+"::action> Running jps to find pid of weather gui.");
	  ProcessBuilder pb = new ProcessBuilder(jpsParams);
          Process p = null;
	  boolean doStop = false;
	  String pid = "";
          try {
            pb.redirectErrorStream(true);
            p = pb.start();
            BufferedReader procReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while (line != null) {
              line = procReader.readLine();
	      if (line == null) continue;
	      System.out.println("\t"+line);
	      if (line.indexOf(name) != -1) {
		/* Found it! */
		pid = line.substring(0, line.indexOf(" "));
		doStop = true;
	      }
	    } 
          } catch (Exception ioe) {
            p.destroy();
            System.out.println(_mainClass+"::action> ERROR: "+ioe.toString());
            success = false;
            break;
          }
	  if (!doStop) continue;

          Vector <String> stopParams = new Vector();
          stopParams.add("taskkill");
	  stopParams.add("/IM");
	  stopParams.add(pid);
	  stopParams.add("/T");
	  stopParams.add("/F");
          System.out.println(_mainClass+"::action> Running taskkill to stop weather gui."); 
          pb = new ProcessBuilder(stopParams);
          p = null;
          try {
            pb.redirectErrorStream(true);
            p = pb.start();
            BufferedReader procReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while (line != null) {
              line = procReader.readLine();
              if (line != null) System.out.println("\tstop> "+line);
            }
            p.waitFor();
            if (p.exitValue() == 0) {
              System.out.println(_mainClass+"::action> stop successful.");
            } else {
              System.out.println(_mainClass+"::action> WARNING: stop terminated abnormally with "+p.exitValue());
            }
          } catch (Exception ioe) {
            p.destroy();
            System.out.println(_mainClass+"::action> ERROR: "+ioe.toString());
            success = false;
            break;
          }
        } else if (getCmdName(req,j).equals("START_AGENT")) {
          String param = getCmdParam(req,j);
	  startAgent(ct, param);
        } else if (getCmdName(req,j).equals("DIR")) {
          String param = getCmdParam(req,j);
	  /* param is directory do list */
          System.out.println(_mainClass+"::action> Directory listing for: "+param);
          ct._send(_mainClass+"::action> Directory listing for: "+param);
	  File theDir = new File(param);
	  if (theDir.exists()) {
	    File[] fileList = theDir.listFiles(); 
            for (int i = 0; j < fileList.length; i++) {
	      System.out.println("\t"+fileList[i].toString()+"\t"+fileList[i].length()+"\t"+(new Date(fileList[i].lastModified())).toString());
	      ct._send("\t"+fileList[i].toString()+"\t"+fileList[i].length()+"\t"+(new Date(fileList[i].lastModified())).toString());
	    }
	  } else {
            System.out.println(_mainClass+"::action> Directory does not exist!"); 
            ct._send(_mainClass+"::action> Directory does not exist!"); 
	  }
	  System.out.println(_mainClass+"::action> EOF");
	  ct._send(_mainClass+"::action> EOF");
	  break;
	} else if (getCmdName(req,j).equals("GET_FILE")) {
          String param = getCmdParam(req,j);
	  String outfilename = param.substring(param.lastIndexOf("\\"));
	  /* param is file to get */
          File file = new File(param);
          byte[] byteArray = new byte[(int)file.length()];
          try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            bis.read(byteArray, 0, byteArray.length);
          } catch(IOException e) {
            e.printStackTrace();
	    UFStrings response = new UFStrings("Error", e.toString());
	    ct._send(response);
            return false;
          }
          UFBytes byteStream = new UFBytes(outfilename, byteArray);
          System.out.println(_mainClass + "::exec> sending "+param);
	  UFStrings response = new UFStrings("Sending file", outfilename);
	  ct._send(response);
          if (ct._send(byteStream) <= 0) {
            return false;
          }
	} else if (getCmdName(req,j).equals("LIST_TASKS")) {
          /* First find pid for camiraserver */
          Vector tlParams = new Vector();
          tlParams.add("tasklist");
          System.out.println(_mainClass+"::action> Running tasklist...");
          ct._send(_mainClass+"::action> Running tasklist...");
          ProcessBuilder pb = new ProcessBuilder(tlParams);
          Process p = null;
          boolean doStop = false;
          String pid = "";
          try {
            pb.redirectErrorStream(true);
            p = pb.start();
            BufferedReader procReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while (line != null) {
              line = procReader.readLine();
              if (line == null) continue;
              System.out.println("\t"+line);
              ct._send("\t"+line);
            }
          } catch (Exception ioe) {
            p.destroy();
            System.out.println(_mainClass+"::action> ERROR: "+ioe.toString());
            success = false;
            break;
          }
          System.out.println(_mainClass+"::action> EOF");
          ct._send(_mainClass+"::action> EOF");
          break;
        } else if (getCmdName(req,j).equals("RESET_USB")) {
          System.out.println(_mainClass+"::action> "+ctime()+": Attempting to reset USB!");
          Vector <String> usbParams = new Vector();
          usbParams.add("devcon");
          usbParams.add("remove");
          usbParams.add("@usb\\*");
          System.out.println(_mainClass+"::action> Running devcon to shut down USB");
	  ct._send(_mainClass+"::action> Running devcon to shut down USB");
          ProcessBuilder pb = new ProcessBuilder(usbParams);
          Process p = null;
	  String line;
          try {
            pb.redirectErrorStream(true);
            p = pb.start();
            BufferedReader procReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = "";
            while (line != null) {
              line = procReader.readLine();
	      if (line == null) continue;
              System.out.println("\t"+line);
              ct._send("\t"+line);
            }
            p.waitFor();
            if (p.exitValue() == 0) {
              System.out.println(_mainClass+"::action> shut down USB successfully.");
	      ct._send(_mainClass+"::action> shut down USB successfully.");
            } else {
              System.out.println(_mainClass+"::action> WARNING: USB shutdown terminated abnormally with "+p.exitValue());
            }
          } catch (Exception ioe) {
            p.destroy();
            System.out.println(_mainClass+"::action> ERROR: "+ioe.toString());
	    ct._send(_mainClass+"::action> ERROR: "+ioe.toString());
	    success = false;
            break;
          }
          /* Now try to rescan USB */
          System.out.println(_mainClass+"::action> "+ctime()+": Attempting to rescan USB!");
          usbParams = new Vector();
          usbParams.add("devcon");
          usbParams.add("rescan");
          System.out.println(_mainClass+"::action> Running devcon to rescan USB");
	  ct._send(_mainClass+"::action> Running devcon to rescan USB");
          pb = new ProcessBuilder(usbParams);
          p = null;
          try {
            pb.redirectErrorStream(true);
            p = pb.start();
            BufferedReader procReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = "";
            while (line != null) {
              line = procReader.readLine();
              if (line == null) continue;
              System.out.println("\t"+line);
              ct._send("\t"+line);
            }
            p.waitFor();
            if (p.exitValue() == 0) {
              System.out.println(_mainClass+"::action> rescanned USB successfully.");
	      ct._send(_mainClass+"::action> rescanned USB successfully.");
            } else {
              System.out.println(_mainClass+"::action> WARNING: USB rescan terminated abnormally with "+p.exitValue());
            }
            ct._send(_mainClass+"::action> EOF");
          } catch (Exception ioe) {
            p.destroy();
            System.out.println(_mainClass+"::action> ERROR: "+ioe.toString());
	    ct._send(_mainClass+"::action> ERROR: "+ioe.toString());
	    success = false;
            break;
          }
	} else if (getCmdName(req,j).equals("KILL")) {
          String program = getCmdParam(req,j);

          /* First find pid for program */
          Vector tlParams = new Vector();
          tlParams.add("tasklist");
          System.out.println(_mainClass+"::action> Running tasklist to find pid of "+program);
          ct._send(_mainClass+"::action> Running tasklist to find pid of "+program);
          ProcessBuilder pb = new ProcessBuilder(tlParams);
          Process p = null;
          boolean doStop = false;
          String pid = "";
	  Vector pidList = new Vector();
          try {
            pb.redirectErrorStream(true);
            p = pb.start();
            BufferedReader procReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while (line != null) {
              line = procReader.readLine();
              if (line == null) continue;
              System.out.println("\t"+line);
              ct._send("\t"+line);
              if (line.indexOf(program) != -1) {
                /* Found it! */
                int idx1 = line.indexOf(".exe")+4;
                pid = line.substring(idx1).trim();
                pid = pid.substring(0, pid.indexOf(" "));
		pidList.add(pid);
                doStop = true;
              }
            }
          } catch (Exception ioe) {
            p.destroy();
            System.out.println(_mainClass+"::action> ERROR: "+ioe.toString());
            success = false;
            break;
          }

	  if (!doStop) {
	    System.out.println(_mainClass+"::action> "+program+" not found.");
	    ct._send(_mainClass+"::action> "+program+" not found.");
	  }

          while (doStop && pidList.size() > 0) {
            Vector <String> stopParams = new Vector();
            stopParams.add("taskkill");
            stopParams.add("/IM");
            stopParams.add((String)pidList.remove(0));
            stopParams.add("/T");
            stopParams.add("/F");
            System.out.println(_mainClass+"::action> Running taskkill to stop "+program);
            ct._send(_mainClass+"::action> Running taskkill to stop "+program);
            pb = new ProcessBuilder(stopParams);
            p = null;
            try {
              pb.redirectErrorStream(true);
              p = pb.start();
              BufferedReader procReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
              String line = "";
              while (line != null) {
                line = procReader.readLine();
                if (line != null) System.out.println(_mainClass+"::action> "+line);
              }
              p.waitFor();
              if (p.exitValue() == 0) {
                System.out.println(_mainClass+"::action> "+program+" killed successfully.");
                ct._send(_mainClass+"::action> "+program+" killed successfully.");
              } else {
                System.out.println(_mainClass+"::action> WARNING: "+program+" terminated abnormally with "+p.exitValue());
                ct._send(_mainClass+"::action> WARNING: "+program+" terminated abnormally with "+p.exitValue());
              }
            } catch (Exception ioe) {
              p.destroy();
              System.out.println(_mainClass+"::action> ERROR: "+ioe.toString());
              success = false;
              break;
            }
          }
	  if (doStop) {
            System.out.println(_mainClass+"::action> "+program+" found and killed.");
            ct._send(_mainClass+"::action> "+program+" found and killed.");
	  }
	}
      }
      return true;
    }

//-----------------------------------------------------------------------------------------------------

    public void run() {
      while (true) {
	if (_shutdown) {
          System.out.println(_mainClass + "::run> termination signal recv'd, or shutdown command...");
          shutdown();
        }
	for (int j = 0; j < _clientThreads.size(); j++) {
	  final WinClientThread ct = _clientThreads.elementAt(j);
	  if (ct.hasRequest()) {
	    Thread actionThread = new Thread() {
	      public void run() {
		System.out.println(_mainClass+"::run "+ctime()+"> Received requests from "+ct.getThreadName());
		boolean _success = action(ct);
		if (_success) {
		  System.out.println(_mainClass + "::run> Successfully executed requests from "+ct.getThreadName());
		} else {
		  System.out.println(_mainClass + "::run> FAILED to execute requests from "+ct.getThreadName());
		}
	      }
	    };
	    actionThread.start();
	    hibernate(50);
	  }
	}
        synchronized(mutex) {
          count++;
          hibernate(200);
        }
      }
    }

//-----------------------------------------------------------------------------------------------------
    protected void startAgent(final WinClientThread ct, final String param) {
      Thread t = new Thread() {
	public void run() {
          String[] args = param.split(" ");
          boolean doStart = true;
          Vector <String> startParams = new Vector();
	  File cwd = new File ("..");
	  String path = "";
	  try {
	    path = cwd.getCanonicalPath();
	  } catch (IOException ioe) {
	    System.out.println(_mainClass+"::startAgent> Failed to get current path!");
	  }
	  if (args[0].toLowerCase().indexOf("bmptoolbox") != -1) {
	    startParams.add(path+"\\bin\\ufbmpAgent.bat");
	  } else if (args[0].toLowerCase().indexOf("bmpjec") != -1) { 
	    if (args[0].toLowerCase().indexOf("local") != -1) {
              startParams.add(path+"\\bmpjec\\bmpjec_local.bat");
	    } else {
	      startParams.add(path+"\\bmpjec\\bmpjec.bat");
	    }
	  } else {
            startParams.add(path+"\\wthjec\\wthjec.bat");
	  }
          System.out.println(_mainClass+"::action> Running "+param);
          for (int i = 1; i < args.length; i++) {
	    if (args[i].equals("-exechost")) {
	      startParams.add(args[i]);
	      startParams.add(args[i+1]);
	      System.out.println("\t"+args[i]);
              System.out.println("\t"+args[i+1]);
	    } else if (args[i].equals("-log")) {
              startParams.add(args[i]);
              startParams.add(args[i+1]);
              System.out.println("\t"+args[i]);
              System.out.println("\t"+args[i+1]);
	    }
	  }
          ProcessBuilder pb = new ProcessBuilder(startParams);
          Process p = null;
	  boolean keepGoing = true;
          try {
            pb.redirectErrorStream(true);
            p = pb.start();
            BufferedReader procReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while (line != null && keepGoing) {
              line = procReader.readLine();
	      if (line != null) keepGoing = ct._send(line);
            }
            p.waitFor();
            if (p.exitValue() == 0) {
              System.out.println(_mainClass+"::action> start successful.");
            } else {
              System.out.println(_mainClass+"::action> WARNING: start terminated abnormally with "+p.exitValue());
            }
          } catch (Exception ioe) {
	    if (p != null) p.destroy();
	    ioe.printStackTrace();
            System.out.println(_mainClass+"::action> ERROR: "+ioe.toString());
            return;
          }
	}
      };
      t.start();
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

    /* Helper methods to parse command name and params */

    public String getCmdName(UFStrings cmd, int idx) {
      String name = cmd.stringAt(idx);
      name = name.substring(0, name.indexOf("::"));
      return name;
    }

    public String getCmdName(UFStrings cmd) {
      return getCmdName(cmd, 0);
    }

    public String getCmdParam(UFStrings cmd, int idx) {
      String param = cmd.stringAt(idx);
      param = param.substring(param.indexOf("::")+2);
      return param;
    }

    public String getCmdParam(UFStrings cmd) {
      return getCmdParam(cmd, 0);
    }

    public String getReplyToken(String reply, int token) {
      return getReplyToken(reply, token, " ");
    }

    public String getReplyToken(String reply, int token, String delim) {
      String[] vals = reply.split(delim);
      if (vals.length > token) return vals[token];
      System.out.println(_mainClass+"::getReplyToken> Error: '"+reply+"' contains less than "+token+" tokens.");
      return "null";
    }

    public String roundVal(float val, int dec) {
      String outVal = ""+val;
      int n = outVal.indexOf(".");
      if (n != -1 && n+3 < outVal.length()) {
	outVal = outVal.substring(0, n+3);
      }
      return outVal;
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

//=====================================================================================================

    // Class ListenThread creates thread to Listen for socket connections from clients:

    protected class ListenThread extends Thread {

	private String _className = getClass().getName();

	public ListenThread() {}

	public void run() {
	    try {
		System.out.println(_className + ".run> server port = "+_serverPort);
		ServerSocket ss = new ServerSocket(_serverPort);
		while (true) {	  
		    Socket clsoc = ss.accept();
		    System.out.println(_className + ".run> accepting new connection...");
		    WinClientThread ct = new WinClientThread(clsoc, ++_clientCount, _simMode);
		    _clientThreads.add( ct );
		    System.out.println(_className + ".run> connection accepted.");
		    ct.verbose(_verbose);
		    new Thread(ct).start();
		    yield();
		}
	    } catch (Exception ex) {
		System.err.println(_className + ".run> "+ex.toString());
		return;
	    }
	}
    }


//===========================================================================================
    // Class UFMMTClientThread creates a thread for each client, for handling requests from client.

    protected class WinClientThread implements Runnable {
   
	protected Thread _thread;
	protected Socket _clientSocket;
	protected int _clientNumber = 0;
	protected int _sendTimeOut = 13000;
	protected String _hostname;
	protected String _agentName = "";
	protected String _clientName;
	protected String _threadName;
	protected boolean _fullClient = false, _keepRunning = true, _closeSocket=false;
	protected String _connTime;
	protected String _className = getClass().getName();
	protected boolean _verbose = false;
	protected ArrayDeque <UFStrings> pendingReqs;
	protected PrintWriter devOut = null;

	public WinClientThread( Socket clientSoc, int clientNumber, boolean sim) {
	  _clientNumber = clientNumber;
          _clientSocket = clientSoc;
          _hostname = clientSoc.getInetAddress().toString(); //don't use getHostName() anymore due to slowness;
          _connTime = ctime();
          _clientName = _hostname + ":" + _clientNumber;
          System.out.println(_className + "> time = " + _connTime);
          System.out.println(_className + "> new connection from: " + _clientName);
          pendingReqs = new ArrayDeque(20);
	  try {
	    devOut = new PrintWriter(_clientSocket.getOutputStream(), true);
	  } catch(IOException e) { }
	}

//----------------------------------------------------------------------------------------
	public String getThreadName() {
          return _threadName;
	}
//----------------------------------------------------------------------------------------

	public boolean hasRequest() {
	  if (pendingReqs.isEmpty()) return false;
          return true;
	}

	public UFStrings getRequest() {
	  if (pendingReqs.isEmpty()) return null;
	  return (UFStrings)pendingReqs.remove();
	}

//----------------------------------------------------------------------------------------

	public void run() {

          System.out.println(_className + ".run> waiting for requests from: "+_clientName);
          _threadName = _className+"("+_clientName+")";
          //set socket timeout to infinite so thread just waits for requests:
          InputStreamReader devIn = null;
          try {
            devIn = new InputStreamReader(_clientSocket.getInputStream());
            _clientSocket.setSoTimeout(0);
          }
          catch( IOException ioe ) { System.out.println( ioe.toString() ); }
          int nreq = 0;
          int nulls = 0;
          int zeros = 0;

          while( _keepRunning ) {

            //recv and process requests from client:
            try {
	      Thread.sleep(50);
	      if (!devIn.ready()) continue;
            }
            catch (InterruptedException e) {}
            catch (IOException e) {
              System.out.println(e.toString());
            }

            UFProtocol ufpr = UFProtocol.createFrom( _clientSocket );
            ++nreq;

            if( ufpr == null ) {
                System.out.println( _threadName + ctime() + "> Recvd null object.");
                if( ++nulls > 1 ) _keepRunning = false;
            }
            else if( _handleRequest( ufpr ) <= 0 ) {
                if( ++zeros > 1 )
                    _keepRunning = false;
            }
          }

          _fullClient = false;
          _closeSocket = true;
          System.out.println(_threadName + ".run> dropped socket to ["+_clientName+"]");
	}
//----------------------------------------------------------------------------------------
	public void _terminate() {
	  _keepRunning = false;
          _fullClient = false;
          _closeSocket = true;
          System.out.println(_threadName + ".terminate> kill socket to ["+_clientName+"]");
	}

        public String hostname() { return _hostname; }
	public void verbose( boolean talk ) { _verbose = talk; }
	public Socket _socket() { return _clientSocket; }

	public String ctime() {
	  String date = new Date( System.currentTimeMillis() ).toString();
	  return( date.substring(4,19) + " LT");
	}

//----------------------------------------------------------------------------------------
	protected int _handleRequest( UFProtocol ufpr ) {
          String request = ufpr.name().toLowerCase();
          UFStrings reply = null;

          System.out.println(_threadName+"::_handleRequest>"+ctime());
          System.out.println(_threadName+"::_handleRequest> request: "+ufpr.name());

	  if (request.indexOf("fullclient") > 0) {
	    _fullClient = true;
            reply = new UFStrings("OK:accepted","Connected to full client.  Ready to receive commands.");
	  } else if (ufpr instanceof UFStrings && request.indexOf("ok:accepted") >= 0) {
	    System.out.println(_threadName+"::_handleRequest> connection established.");
	  } else if (ufpr instanceof UFStrings && request.indexOf("actionrequest") > 0) {
	    return handleAction((UFStrings)ufpr);
          } else return handleRequest(ufpr);

	  if (reply == null) return 1;
	  String response = reply.valData(0);
	  int nchar = Math.min( 60, response.length() );
	  System.out.println(_threadName + reply.timeStamp().substring(0,19)+ " : " + reply.name()+" : "+ response.substring(0,nchar));
	  return _send( reply );
	}

    protected int handleAction(UFStrings req) {
      int nreq = req.numVals();
      System.out.println(_threadName+"::handleAction> Received action request bundle: "+req.name());
      System.out.println(_threadName+"::handleAction> contains "+nreq+" requests.");
      pendingReqs.add(req);
      return nreq;
    }

    protected int handleRequest(UFProtocol req) {
      String request = req.name().toLowerCase();
      if (req instanceof UFStrings && request.indexOf("update software") != -1) {
	/* Request to update software */
	int nupdated = updateSoftware();
        System.out.println(_threadName+"::handleRequest> updated "+nupdated+" files!"); 
	return nupdated;
      } else if (req instanceof UFStrings && request.indexOf("invalid request") > 0) {
        /* Response that this client sent an invalid request */
        String val = ((UFStrings)req).stringAt(0);
        System.out.println(_threadName+"::handleRequest> previous request invalid: "+val);
        return val.length();
      } else if (req instanceof UFStrings && request.endsWith("error")) {
        /* Received error response */
        String val = ((UFStrings)req).stringAt(0);
        System.out.println(_threadName+"::handleRequest> received ERROR message: "+val);
        return val.length();
      } else if (req instanceof UFStrings && request.endsWith("success")) {
        /* Received success response */
        String val = ((UFStrings)req).stringAt(0);
        System.out.println(_threadName+"::handleRequest> received SUCCESS message: "+val);
        return val.length();
      }
      /* Otherwise send invalid request and echo back request name */
      UFStrings reply = new UFStrings(_threadName+": invalid request", request);
      System.out.println(_threadName+"::handleRequest> received invalid request: "+request);
      return _send(reply);
    }


    protected int updateSoftware() {
      int nupdated = 0;
      boolean keepGoing = true;
      while (keepGoing) {
	UFProtocol ufpr = UFProtocol.createFrom( _clientSocket );
	String request = ufpr.name();
	if (ufpr instanceof UFStrings && request.indexOf("EOF") != -1) {
	  keepGoing = false;
	  continue;
	} else if (ufpr instanceof UFBytes && (request.indexOf("C:") == 0 || request.indexOf("..") == 0)) {
	  try {
	    System.out.println(_threadName+"::updateSoftware> Receiving file "+request);
	    UFBytes byteStream = (UFBytes)ufpr;
	    byte[] byteArray = byteStream.values(); 
	    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(request));
	    bos.write(byteArray, 0, byteArray.length);
	    bos.flush();
	    bos.close();
	    nupdated++;
	  } catch (IOException e) {
	    keepGoing = false;
	    e.printStackTrace();
	  }
	} 
      }
      return nupdated;
    }


//----------------------------------------------------------------------------------------
	public synchronized int _send( UFProtocol ufpr ) {
	  synchronized(_clientSocket) {
	    try {
              //set socket timeout to finite value to try and detect send error:
              _clientSocket.setSoTimeout(_sendTimeOut);
              int nbytes = ufpr.sendTo( _clientSocket );
              //set socket timeout back to infinite so thread just waits for requests:
              _clientSocket.setSoTimeout(0);
              if( nbytes <= 0 ) System.out.println( _threadName + ".send> zero bytes sent.");
              return nbytes;
            }
            catch( IOException iox ) {
              System.out.println( _threadName + ".send> " + iox.toString() );
              return(-1);
            }
            catch( Exception ex ) {
              System.out.println( _threadName + ".send> " + ex.toString() );
              return(-1);
            }
	  }
	}

	public synchronized boolean _send(String line) {
	  synchronized(_clientSocket) {
	    try {
              //set socket timeout to finite value to try and detect send error:
              _clientSocket.setSoTimeout(_sendTimeOut);
	      devOut.println(line);
	      devOut.flush();
	      _clientSocket.setSoTimeout(0);
            } catch(IOException ioe) {
              System.err.println(_mainClass+"::_send> "+ioe.toString());
              try { _clientSocket.setSoTimeout(0); } catch(Exception e) {}
              return false;
            }
	  }
	  return true;
	}
    }

} //end of class WinAgent
