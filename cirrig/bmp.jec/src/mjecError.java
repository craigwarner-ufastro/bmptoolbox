package bmpjec;

/**
 * Title:        mjecError.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Mjec Error frame
 */


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

//===============================================================================
/**
 * A class to report and log errors
 */
public class mjecError {
  public static final String rcsID = "$Name:  $ $Id: mjecError.java,v 1.1 2010/06/25 18:39:41 warner Exp $";

  static JTextArea jTextArea = new JTextArea();
  static JPanel buttonPanel = new JPanel();
  static JButton dismissButton = new JButton("Dismiss");
  static JButton clearButton = new JButton("Clear");
  static JScrollPane scrollPane;
  static JFrame errorMsgFrame = new JFrame();
  static private boolean firstTime = true;
  

//-------------------------------------------------------------------------------
  /**
   * Error message frame initialization
   */
  private static void errorMsgFrameInit() {
    try {
      errorMsgFrame = new JFrame("mjecError Window");
      errorMsgFrame.getContentPane().setLayout(new BorderLayout());
      buttonPanel.setLayout(new FlowLayout());
      buttonPanel.add(dismissButton);
      dismissButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dismissButton_actionPerformed();
        }
      });
      buttonPanel.add(clearButton);
      clearButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent ae) {
		  clearButton_actionPerformed();
	      }
	  });
      scrollPane = new JScrollPane(jTextArea);
      errorMsgFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
      errorMsgFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
      errorMsgFrame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          dismissButton_actionPerformed();
        }
      });
      firstTime = false;
      errorMsgFrame.getContentPane().validate();
      errorMsgFrame.setSize(new Dimension(400,600));
      jTextArea.setEditable(false);
    }
    catch (Exception e) {
      System.out.println("mjecError.errorMsgFrameInit> " + e.toString());
    }
  } // end of errorMsgFrameInit

//------------------------------------------------------------------------------
  /**
   * Destroys the window when the Dismiss button is pressed.
   * or the when window is closed
   */
  public static void dismissButton_actionPerformed() {
    errorMsgFrame.dispose();
	//firstTime = true;
  } // end of dismissButton_actionPerformed

//------------------------------------------------------------------------------
  /**
   * Clear the text area of previous messages
   */
  public static void clearButton_actionPerformed() {
    
      jTextArea.setText("");
  } // end of dismissButton_actionPerformed

//------------------------------------------------------------------------------
  /**
   * Show a new error to the log window and write it to log file
   *@param msg String: message to be displayed in the error frame
   */
  public static void show(String msg) {
    String className = "";
    if (firstTime) errorMsgFrameInit();
    if (!msg.toLowerCase().startsWith("error")) msg = "Error: " + msg;
    try {
	    className = getContext(3).toString();
    }
    catch (Exception e) {}
    jTextArea.append(msg + "\n");
    jTextArea.append("\t" + className + "\n");
    jTextArea.append("\t" + new Date().toString()+"\n");
    errorMsgFrame.setVisible(true);
    errorMsgFrame.repaint();
    System.out.println(msg + " " + className);
    try {
      StringTokenizer st = new StringTokenizer(new Date().toString());
      st.nextToken();
      String filename = "bmpjec_errors_";
      filename += st.nextToken();
      st.nextToken(); st.nextToken(); st.nextToken();
      filename += "-" + st.nextToken();
      filename += ".log";
      BufferedWriter bw = new BufferedWriter(new FileWriter(filename,/*append = */true));
      String s = new Date().toString() + " " + msg + " " + className + "\n";
      bw.write(s);
      bw.newLine();
      bw.close();
    }
    catch (Exception e) {
      System.out.println(e.toString());
    }
  } // end of show(java.lang.String)


//------------------------------------------------------------------------------
  /**
   * Create/Unhide the error window
   */
   public static void show() {
     if (firstTime) errorMsgFrameInit();
     errorMsgFrame.setVisible(true);
     errorMsgFrame.repaint();
   } // end of show(void)


//-------------------------------------------------------------------------------
  /**
   * Determine where the error occured. Level will normally be 3.
   * This is a bit of a kluge. Do yo know a better way?
   *@param level level of the error
   */
  static Context getContext (int level) {
    // printStackTrace()
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream pout = new PrintStream(out);
    new Exception().printStackTrace(pout);

    BufferedReader din
        = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
    String targetLine = "  at CLASS.METHOD(FILE.java:0000)"; // ???
    try {
      for (int i=0; i<level; i++) {
        targetLine = din.readLine();
      }
      targetLine = din.readLine();
    }
    catch (IOException e) {
      System.out.println("Error reading printStackTrace output in mjecError");
    }
    StringTokenizer tk = new StringTokenizer(targetLine, " \n\t.():");
    String[] tokens = new String[8];
    int i;
    for (i=0; i < 6; i++) tokens[i] = "?";
    tokens[6] = "0";
    for (i=0; tk.hasMoreTokens() && i < 8; i++) {
      tokens[i] = tk.nextToken();
    }

    // setup context
    Context context = new Context();
    context.className  = tokens[2];
    context.methodName = tokens[3];
    String file = tokens[4];
    String ext  = tokens[5];
    context.sourceFile = file + "." + ext;
    context.lineNumber = Integer.parseInt(tokens[6]);

    return context;
  } // end of getContext

} // end of class mjecError


//==============================================================================
/**
 * A class to identify the location of an error
 */
class Context {
  public String className;
  public String methodName;
  public String sourceFile;
  public int lineNumber;


//------------------------------------------------------------------------------
  /**
   * returns className + ", " + methodName + ", " + sourceFile + ", " + lineNumber
   */
  public String toString () {
    return className + ", " + methodName + ", " + sourceFile + ", " + lineNumber;
  } // end of toString

} // end of Context




