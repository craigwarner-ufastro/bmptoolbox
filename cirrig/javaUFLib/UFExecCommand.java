package javaUFLib;

/**
 * Title:        UFExecCommand
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2005
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  JPanel executing and showing results of a shell command
 */

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class UFExecCommand extends javax.swing.JPanel implements Runnable {

    String command, params, defaultPath, rshHost;
    JLabel label;
    JTextArea messages,errorMessages;
    JScrollPane scrollPane,errorScrollPane;
    JButton dismiss;
    ProcessBuilder pb;
    Map m;
    Process p;
    BufferedReader reader,errorReader;
    boolean isRunning, isValid, selected, isUnique, doRsh;
    Thread thread;
    String os = "", path = "";

    public static String getEnvVar(String envVar)  {
	try {
	    Properties env = new Properties();
	    env.load(Runtime.getRuntime().exec("env").getInputStream());
	    return (String)env.getProperty(envVar);
	} catch (Exception e){
	    Vector v = new Vector();
            v.add("env");
            ProcessBuilder pb = new ProcessBuilder(v);
	    try {
	      Map m = pb.environment();
	      String value = (String)m.get(envVar);
	      return value;
	    } catch(Exception ex) {
	      System.err.println("ExecCommand error > unable to get environment");
	   }
	    System.out.println("UFExecCommand.getEnvVar> "+e.toString());
	    return null;
	}
    }

    public UFExecCommand(String command, String params) {
	this(command, params, "",false,"");
    }

    public UFExecCommand(String command, String params, String defaultPath) {
	this(command, params, defaultPath,false,"");
    }

    public UFExecCommand(String command, String params, String defaultPath, boolean doRSH, String rshHost) {
	doRsh = doRSH;
	this.rshHost = rshHost;
	command = command.trim();
        params = params.trim();
        int pos = command.indexOf(" ");
	if (pos != -1) command = command.substring(0,pos);
	if (command.endsWith("&")) command = command.substring(0, command.length()-1);
	if (params.endsWith("&")) params = params.substring(0, params.length()-1);
	this.command = command;
	this.params = params;
	this.defaultPath = defaultPath.trim();
	Vector v = new Vector();
	if (doRsh) { v.add("rsh"); v.add(rshHost); v.add("source ~/.login; source ~/.cshrc; "+command); }
	else v.add(command);
	String[] tempArgs = params.split(" ");
	for (int j = 0; j < tempArgs.length; j++) {
	   v.add(tempArgs[j]);
	}
	pb = new ProcessBuilder(v);
	pb.redirectErrorStream(true);
	try {
	   m = pb.environment();
	} catch(Exception e) {
	   System.err.println("ExecCommand error > unable to get environment");
	}
        label = new JLabel("About to run "+command+" with options "+params);
        messages = new JTextArea(10,40);
        scrollPane = new JScrollPane(messages);
        errorMessages = new JTextArea(10,40);
	errorScrollPane = new JScrollPane(errorMessages);
	JTabbedPane messageTabs = new JTabbedPane();
	messageTabs.add("Output",scrollPane);
	messageTabs.add("Errors",errorScrollPane);
	dismiss = new JButton("End Process");
        SpringLayout layout = new SpringLayout();
        setLayout(layout);
        add(label);
        layout.putConstraint(SpringLayout.WEST, label, 10, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, label, 10, SpringLayout.NORTH,this);
        //add(scrollPane);
        add(messageTabs);
	layout.putConstraint(SpringLayout.WEST, messageTabs, 10, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, messageTabs, 15, SpringLayout.SOUTH, label);
        add(dismiss);
        dismiss.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                isRunning = false;
                if (p != null) p.destroy();
            }
        });
        layout.putConstraint(SpringLayout.WEST, dismiss, 10, SpringLayout.WEST,this);
        layout.putConstraint(SpringLayout.NORTH, dismiss, 15, SpringLayout.SOUTH, messageTabs);
        layout.putConstraint(SpringLayout.EAST, this, 30, SpringLayout.EAST, messageTabs);
        layout.putConstraint(SpringLayout.SOUTH, this, 10, SpringLayout.SOUTH, dismiss);
    }

    public String getEnv(String key) {
	if (m == null) return null;
	String value = (String)m.get(key);
	return value;
    }

    public void setEnv(String key, String value) {
	if (m == null) {
	    System.err.println("UFExecCommand error > environment not defined.");
	    return;
	}
	m.put(key, value);
    }

    public void start() {
        isValid = testCommand();
        if (isValid) {
	    boolean isUnique = checkProcesses();
	    if (isUnique) {
		label.setText("Running "+command+" with options "+params);
                isRunning = true;
                thread = new Thread(this);
                thread.start();
	    }
	    else label.setText("Failed to run "+command+" with options "+params);
        } else {
	   label.setText("Failed to run "+command+" with options "+params);
           if (! isValid) System.err.println("ExecCommand error > command "+command+" not found.");
        }
    }

    public boolean testCommand() {
	try {
	    String s = "";
	    Process unamep;
	    if (doRsh)
		unamep = new ProcessBuilder("rsh",rshHost,"/bin/uname").start();
	    else
		unamep = new ProcessBuilder("/bin/uname").start();
	    BufferedReader unamebr = new BufferedReader(new InputStreamReader(unamep.getInputStream()));
	    s = unamebr.readLine();
	    unamebr.close();
	    if (s.toLowerCase().indexOf("linux") != -1) {
		os = "linux";
		path = "/usr/bin/";
	    } else if (s.toLowerCase().indexOf("sun") != -1) {
		os = "sun";
		path = "/usr/ucb/";
	    } else path = "";
	    Process testp;
	    if (defaultPath.equals("")) {
		if (doRsh)
		    testp = new ProcessBuilder("rsh",rshHost,path+"whereis",command).start();
		else
		    testp = new ProcessBuilder(path+"whereis",command).start();
	    } else {
		if (doRsh)
		    testp = new ProcessBuilder("rsh",rshHost,path+"whereis","-B",defaultPath,"-f",command).start();
		else
		    testp = new ProcessBuilder(path+"whereis","-B",defaultPath,"-f",command).start();
	    }
	    BufferedReader testbr = new BufferedReader(new InputStreamReader(testp.getInputStream()));
            s = testbr.readLine(); 
	    unamebr.close();
            if (s.endsWith(":")) {
	      File f = new File(defaultPath+command);
	      if (f.isFile()) return true; else return false;
	    } else return true; 
	} catch (Exception e) {
	    return false;
	}
    }

    public static boolean isRunning(String cmdToCheck) {
	return isRunning(cmdToCheck,null);
    }

    public static boolean isRunning(String cmdToCheck, String rshHost) {
	boolean currentlyRunning = false;
	try {
	    Process checkps;
	    if (rshHost == null || rshHost.trim().equals(""))
		checkps = new ProcessBuilder("/bin/ps","-A").start();
	    else
		checkps = new ProcessBuilder("rsh",rshHost+" /bin/ps -A").start();
	    BufferedReader brps = new BufferedReader(new InputStreamReader(checkps.getInputStream()));
	    String s = "";
	    while (s != null) {
		s = brps.readLine();
		if (s == null) break;
		if (s.endsWith(" "+cmdToCheck)) currentlyRunning = true;
	    }
	}  catch (Exception e) {
	    System.err.println("UFExecCommand.isRunning> "+e.toString());
	}
	return currentlyRunning;
    }

    public static String getProcessOutput(String cmdToRun) {
	return getProcessOutput(cmdToRun,null,null);
    }

    public static String getProcessOutput(String cmdToRun, String options) {
	return getProcessOutput(cmdToRun,options,null);
    }

    public static String getProcessOutput(String cmdToRun, String options, String rshHost) {
	try {
	    Process p;
	    if (rshHost == null || rshHost.trim().equals("")){
		p = new ProcessBuilder(cmdToRun,options).start();
	    }else{
		p = new ProcessBuilder("rsh",rshHost,"source ~/.login; source ~/.cshrc; "+cmdToRun+" "+options).start();
	    }
	    //sleep for a little while to allow the process to generate output
	    Thread.sleep(500);
	    BufferedInputStream bis = new BufferedInputStream(p.getInputStream());
	    BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream())); 
	    String totalS = "";
	    byte [] charBuff;
	    while (true) {
		if (br.ready()) {
		    System.err.println("UFExecCommand.getProcessOutput> "+br.readLine());
		}
		charBuff = new byte[bis.available()];
		int red = bis.read(charBuff,0,charBuff.length);
		if (red <= 0) break;
		totalS += new String(charBuff);
	    }	
	    bis.close();
	    p.destroy();
	    return totalS;
	} catch (Exception e) {
	    System.err.println("UFExecCommand.getProcessOutput> "+e.toString());
	}
	return null;
    }

    public boolean checkProcesses() {
	isUnique = true;
	selected = true;
	try {
	    Process checkps;
	    if (doRsh) 
		checkps = new ProcessBuilder("rsh",rshHost,"/bin/ps","-eo","pid,user,comm").start();
	    else
		checkps = new ProcessBuilder("/bin/ps","-eo","pid,user,comm").start();
	    BufferedReader brps = new BufferedReader(new InputStreamReader(checkps.getInputStream()));
	    String s = " ";
	    while (s != null) {
		s = brps.readLine();
		if (s == null) break;
		if (s.endsWith(" "+command)) isUnique = false; 
	    }
	    brps.close();
	} catch (Exception e) {
	    return false;
	}
	if (!isUnique) {
	    selected = false;
            final JDialog pFrame = new JDialog((Frame)null,"Process running...");
	    pFrame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
                    selected = true;
		    pFrame.dispose();
		}
	    });
            Container content = pFrame.getContentPane();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            JLabel pLabel = new JLabel("The process "+command+" is already running.  Do you want to:");
            JButton abortButton = new JButton("Abort new process");
		abortButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ev) {
			selected = true;
                        pFrame.dispose();
		    }
		});
            JButton ignoreButton = new JButton("Ignore and run anyway");
		ignoreButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
			isUnique = true;
                        selected = true;
                        pFrame.dispose();
                    }
                });
            JButton killButton = new JButton("Kill old process and run");
		killButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
			try {
			    Process checkps;
			    if (doRsh)
				checkps = new ProcessBuilder("rsh",rshHost,"/usr/bin/pkill",command).start();
			    else
				checkps = new ProcessBuilder("/usr/bin/pkill",command).start();
			    isUnique = true;
			    int x = checkps.waitFor();
			} catch (Exception e) {
			    System.err.println("ExecCommand error > Unable to kill "+command);
			}
			selected = true;
                        pFrame.dispose();
                    }
                });
            JPanel labelPanel = new JPanel();
            labelPanel.add(pLabel);
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(abortButton);
            buttonPanel.add(ignoreButton);
            buttonPanel.add(killButton);
            content.add(labelPanel);
            content.add(buttonPanel);
            pFrame.pack();
	    pFrame.setModal(true);
	    pFrame.setAlwaysOnTop(true);
            pFrame.setVisible(true);
	}
	//while (! selected) {
	//}
	return isUnique;
    }

    public void run() {
	try {
	    //m.put("LD_LIBRARY_PATH","/share/local/ufflam/lib:/share/local/ufflam/extern/lib:/opt/local/lib:/opt/EDTpdv:/gemini/dhs/dhs/lib/solaris-mt:/gemini/dhs/dhs/external/lib/solaris-mt:/gemini/epics/base/lib/solaris-sparc-gnu:/gemini/epics/extensions/lib/solaris-sparc-gnu:/share/local/lib:/usr/local/lib:/usr/local/ssl/lib:/lib:/usr/lib:/usr/ucblib:/usr/X11R6/lib:/usr/openwin/lib:/usr/dt/lib" );
	    p = pb.start();
	    //int x = p.waitFor();
	    //System.out.println("X: "+x);
	    //reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	    reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	    String s = " ";
	    while (isRunning) {
		s = reader.readLine();
		if (s != null) messages.append(s+"\n");
		s = errorReader.readLine();
		if (s != null) errorMessages.append(s+"\n");
	    }
	} catch(Exception e) {
	    isRunning = false;
	    System.err.println("UFExecCommand.run> "+e.toString());
	}
	if (p != null) p.destroy();
	label.setText("Done!");
    }

    public static void main(String[] args) {
        System.out.println(getEnvVar("UFINSTALL"));
 	UFExecCommand ufec = new UFExecCommand("ls","-l");
 	JFrame frame = new JFrame("UFExecCommand");
 	Container content = frame.getContentPane();
	content.add(ufec);
 	frame.pack();
	frame.setDefaultCloseOperation(3);
 	frame.setVisible(true);
 	ufec.start();
    }
}
