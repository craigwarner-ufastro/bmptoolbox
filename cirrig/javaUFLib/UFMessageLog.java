package javaUFLib;

/**
 * Title:        UFMessageLog.java: extends UFTextArea (which extends JTextArea)
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2005
 * Authors:      Craig Warner and Frank Varosi
 * Company:      University of Florida
 * Description:  Holds a log of past messages in Vector and displays in text area.
 */

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Time;

public class UFMessageLog extends UFTextArea {
    public static final
	String rcsID = "$Name:  $ $Id: UFMessageLog.java,v 1.27 2014/03/17 23:23:20 varosi Exp $";

    protected String Name;
    private String _LastMessage = "";
    private String _prevMessage = "";
    private boolean _showTimes = true, _showDups = false, _addDups = false, _newDups = true;

    private Vector _VLog;
    private int _maxAllowSize = 20000; //max allowed size of Vector containing Log of UFMessage objects.
    private int _maxLogSize = 100;     //current max Log size.
    private int _NmsgRecvd = 0;
    private JFrame _LogFrame;
    private int _Hsize, _Vsize;
//--------------------------------------------------------------------------------------------------
    /**
     * Constructors
     *@param  nMegaBytes  int: max number of Mega-Bytes allowed for document length.
     *@param  maxLogSize  int: max number of UFMessage objects that will be saved in Vector.
     */
    public UFMessageLog(String name, int nMegaBytes, int maxLogSize, int rows, int cols)
    {
	super( nMegaBytes, rows, cols );
	this.Name = name;
	this.setEditable(false);
	if( maxLogSize > _maxAllowSize ) maxLogSize = _maxAllowSize;
	this._maxLogSize = maxLogSize;
	this._VLog = new Vector(maxLogSize,10); //use non-zero capacity increment in case more is needed.
    }

    public UFMessageLog(String name, int maxLogSize, int rows, int cols) {
	this(name, 1, maxLogSize, rows, cols);
    }
    public UFMessageLog(int maxLogSize, int rows, int cols) {
	this("?", 1, maxLogSize, rows, cols);
    }

    public UFMessageLog(String name, int nMegaBytes, int maxLogSize)
    {
	super( nMegaBytes );
	this.Name = name;
	this.setEditable(false);
	if( maxLogSize > _maxAllowSize ) maxLogSize = _maxAllowSize;
	this._maxLogSize = maxLogSize;
	this._VLog = new Vector(maxLogSize,10); //use non-zero capacity increment in case more is needed.
    }

    public UFMessageLog(String name, int maxLogSize) { this(name, 1, maxLogSize); }

    public UFMessageLog(int maxLogSize) { this("?", 1, maxLogSize); }

    public UFMessageLog(String name) { this(name, 1, 100); }

    public UFMessageLog() { this("?", 1, 100); }
//-------------------------------------------------------------------------------

    public void viewFrame(int hsize, int vsize) { viewFrame(this.Name, hsize, vsize); }

    public void viewFrame(String Title, int hsize, int vsize)
    {
	if (_LogFrame != null) {
	    if( _Hsize == hsize && _Vsize == vsize ) {
		_LogFrame.setVisible(true);
		_LogFrame.setState( Frame.NORMAL );
		return;
	    }
	    else _LogFrame.dispose();
	}

	_Hsize = hsize;
	_Vsize = vsize;
	JPanel LogPanel = new JPanel();
	JScrollPane LogScroll = new JScrollPane(this);
	LogScroll.setPreferredSize(new Dimension(hsize,vsize));
	LogPanel.add( LogScroll );

	JPanel controlPanel = new JPanel();
	JButton closeButton = new JButton("Close");
	controlPanel.add(closeButton);
	closeButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    _LogFrame.dispose();
		}
	    });
	final JButton refresh = new JButton("Clear Dups");
	controlPanel.add(refresh);
	refresh.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    showAllMessages();
		}
	    });
	final JCheckBox AutoScroll = new JCheckBox("AutoScroll ",true); 
	controlPanel.add(AutoScroll);
	AutoScroll.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    autoScrollToBottom(AutoScroll.isSelected());
		}
	    });
	final JCheckBox doTimeStamps = new JCheckBox("Times ",true); 
	controlPanel.add(doTimeStamps);
	doTimeStamps.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    showTimeStamps(doTimeStamps.isSelected());
		    showAllMessages();
		}
	    });
	final JCheckBox duplicates = new JCheckBox("Dups ",false); 
	controlPanel.add(duplicates);
	duplicates.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    addDuplicates(duplicates.isSelected());
		    showDuplicates(duplicates.isSelected());
		}
	    });
	final String[] nSave = {"1000","2000","4000","8000"};
	final JComboBox maxSave = new JComboBox(nSave);
	controlPanel.add(new JLabel("Max Save="));
	controlPanel.add(maxSave);
	maxSave.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    setMaxLogSize( Integer.parseInt( (String)maxSave.getSelectedItem() ) );
		}
	    });

	_LogFrame = new JFrame("Log: " + Title);
	_LogFrame.getContentPane().setLayout(new BorderLayout()); 
	_LogFrame.getContentPane().add( LogPanel, BorderLayout.NORTH );
	_LogFrame.getContentPane().add( controlPanel, BorderLayout.SOUTH );
	_LogFrame.pack();
	_LogFrame.setVisible(true);
	this.showAllMessages();
    }
//--------------------------------------------------------------------------------------------------

    public JPopupMenu createPopupMenu()
    {
	JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem viewLogSmall = new JMenuItem("View Log:  small");
        JMenuItem viewLogWide =  new JMenuItem("View Log:  wide");
        JMenuItem viewLogLong =  new JMenuItem("View Log:  Long");
        JMenuItem viewLogLarge = new JMenuItem("View Log:  Large");
        JMenuItem viewLogHuge =  new JMenuItem("View Log:  Huge");
        popupMenu.add(viewLogSmall);
        popupMenu.add(viewLogWide);
        popupMenu.add(viewLogLong);
        popupMenu.add(viewLogLarge);
        popupMenu.add(viewLogHuge);

        viewLogSmall.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) { viewFrame( 550, 200 ); } });

        viewLogWide.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) { viewFrame( 800, 200 ); } });

        viewLogLong.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) { viewFrame( 600, 500 ); } });

        viewLogLarge.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) { viewFrame( 770, 500 ); } });

        viewLogHuge.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) { viewFrame( 800, 800 ); } });

	return popupMenu;
    }
//--------------------------------------------------------------------------------------------------

    public void setName(String name) { this.Name = name; }

    public void setTimestamps(boolean b) { this._showTimes = b; }

    public void showTimeStamps(boolean b) { this._showTimes = b; }

    public void showDuplicates(boolean b) { this._showDups = b; }

    public void addDuplicates(boolean b) {
	this._addDups = b;
	if( _addDups ) _newDups = true;
    }

    public void newDuplicates(boolean b) {
	this._newDups = b;
	if( !_newDups ) _addDups = false;
    }

    public int getLogSize() { return _VLog.size(); }

    public void setMaxLogSize(int n) {
	if( n > _maxAllowSize ) n = _maxAllowSize;
	_maxLogSize = n;
    }

    public void setMaxAllowSize(int n) { _maxAllowSize = n; }

    public int maxAllowSize() { return _maxAllowSize; }
    public int maxLogSize() { return _maxLogSize; }

    private void setToolTipText() {
	this.setToolTipText("# msgs displayed="+_NmsgDisp+", # recvd="+_NmsgRecvd+", saved="+_VLog.size());
    }
//--------------------------------------------------------------------------------------------------
    /*
     * UFMessageLog#appendMessage
     * Adds message to vector Log, first checking for duplicates, and appends it to TextArea.
     * Default is to keep just the oldest and newest (current) of duplicate messages,
     * removing all the middle duplicates.
     *@param  message  String: text to add into vector storage of message Log and append to TextArea.
     */
    public void appendMessage(String message) {
	if( message.length() > 0 ) {
	    if( addMessage( message ) ) appendLastMessage();
	}
    }
//--------------------------------------------------------------------------------------------------
    /*
     * UFMessageLog#addMessage
     * Adds message to vector Log, first checking for duplicates.
     * Default is to keep just the oldest and newest (current) of duplicate messages,
     * removing all the middle duplicates.
     *@param  message  String: text to add into vector storage of message Log.
     */
    public boolean addMessage(String message) {
      ++_NmsgRecvd;
      while( _VLog.size() >= _maxLogSize ) {
	  Object vr = _VLog.remove(0);
	  vr = null;
      }
      boolean duplicate = false;

      // truncate special fields starting with "<[" in string, to create duplicates:
      int dpos = message.indexOf("<[");
      if( dpos > 0 ) message = message.substring( 0, dpos-1 );

      if( !_newDups ) {
	  if( message.equals(_LastMessage) ) duplicate = true;
	  else _LastMessage = message;
      }
      else if( message.equals(_LastMessage) && message.equals(_prevMessage) ) duplicate = true;
      else {
	  _prevMessage = _LastMessage;
	  _LastMessage = message;
      }

      if( duplicate ) {

	  if( _newDups ) {  // if _newDups = false then nothing will be added.

	      if( !_addDups ) { //remove the older middle duplicate (keeping newest and oldest).
		  int size = _VLog.size();
		  if (size > 0) {
		      Object vr = _VLog.remove(size-1);
		      vr = null;
		  }
	      }

	      _VLog.add(new UFMessage( message, duplicate ));
	      return true;
	  }
	  else return false;
      }
      else _VLog.add(new UFMessage( message ));

      return true;
    }

    public void addMessageAt(int x, String message) {
	++_NmsgRecvd;
	if (x >= 0 && x < _VLog.size()) _VLog.add(x, new UFMessage(message));
    }

    // Display all messages in text area.
    // If _showDups==false then display just the first and last in each sequence of duplicates.
    // This is done by checking if next message is also a duplicate.

    public void showAllMessages() {
	int size = _VLog.size();
	if( size < 1 ) return;
	String messages = "";
	UFMessage M = (UFMessage)_VLog.firstElement();
	_NmsgDisp = 0;
	for( int i=1; i < size; i++ ) {
	    UFMessage nextM = (UFMessage)_VLog.elementAt(i);
	    if( _showDups || !M.duplicate || !nextM.duplicate  ) {
		messages += M.getMessage(_showTimes) ; 
		++_NmsgDisp;
	    }
	    M = nextM;
	}
	//always include the last message:
	messages += M.getMessage(_showTimes); 
	++_NmsgDisp;
	this.setText(messages);
	this.setToolTipText();
    }

    public String[] getAllMessages() {
	int size = _VLog.size();
	if( size < 1 ) return null;
	UFMessage[] Mall = new UFMessage[size];
	UFMessage M = (UFMessage)_VLog.firstElement();
	int n = 0;
	for( int i=1; i < size; i++ ) {
	    UFMessage nextM = (UFMessage)_VLog.elementAt(i);
	    if( _showDups || !M.duplicate || !nextM.duplicate  ) Mall[n++] = M;
	    M = nextM;
	}
	//always include the last message:
	Mall[n++] = M;
	String[] messages = new String[n];
	for (int j = 0; j < n; j++) messages[j] = Mall[j].getMessage(_showTimes); 
	return messages;
    }

    public void appendLastMessage() {
	if( _VLog.size() < 1 ) return;
	UFMessage M = (UFMessage)_VLog.lastElement();
	this.append( M.getMessage(_showTimes) );
	this.setToolTipText();
    }

    public String getLastMessage() {
	if( _VLog.size() < 1 ) return null;
	UFMessage M = (UFMessage)_VLog.lastElement();
	return M.getMessage(_showTimes);
    }

    public void showLastMessage() {
	if( _VLog.size() < 1 ) return;
	UFMessage M = (UFMessage)_VLog.lastElement();
	_NmsgDisp = 1;
	this.setText( M.getMessage(_showTimes) );
	this.setToolTipText();
    }

    public String[] getLastMessages(int x) {
	int size = _VLog.size();
	if( size < 1 ) return null;
	if( x > size ) x = size;
	UFMessage[] M = new UFMessage[x];
	int n = 0;
	for (int j = size-x; j < size; j++) {
	    UFMessage temp = (UFMessage)_VLog.elementAt(j);
	    if( _showDups || !temp.duplicate || j==size-1 ) M[n++] = temp;
	}
	String[] messages = new String[n];
	for (int j = 0; j < n; j++) messages[j] = M[j].getMessage(_showTimes); 
	return messages;
    }

    public void showLastMessages(int x) {
	int size = _VLog.size();
	if( size < 1 ) return;
	if( x > size ) x = size;
	String messages = "";
	_NmsgDisp = 0;
	for (int j = size-x; j < size; j++) {
	    UFMessage M = (UFMessage)_VLog.elementAt(j);
	    if( _showDups || !M.duplicate || j==size-1 ) {
		messages += M.getMessage(_showTimes);
		++_NmsgDisp;
	    }
	}
	this.setText(messages);
	this.setToolTipText();
    }

    public void appendLastMessages(int x) {
	int size = _VLog.size();
	if( size < 1 ) return;
	if( x > size ) x = size;
	for (int j = size-x; j < size; j++) {
	    UFMessage M = (UFMessage)_VLog.elementAt(j);
	    if( _showDups || !M.duplicate || j==size-1 ) {
		this.append( M.getMessage(_showTimes) );
	    }
	}
    }

    public String getFirstMessage() {
	if( _VLog.size() < 1 ) return null;
	UFMessage M = (UFMessage)_VLog.firstElement();
	return M.getMessage(_showTimes);
    }

    public void showFirstMessage() {
	if( _VLog.size() < 1 ) return;
	UFMessage M = (UFMessage)_VLog.firstElement();
	_NmsgDisp = 1;
	this.setText( M.getMessage(_showTimes) );
	this.setToolTipText();
    }

    public void appendFirstMessage() {
	if( _VLog.size() < 1 ) return;
	UFMessage M = (UFMessage)_VLog.firstElement();
	this.append( M.getMessage(_showTimes) );
	this.setToolTipText();
    }

    public String[] getFirstMessages(int x) {
	int size = _VLog.size();
	if( size < 1 ) return null;
	if( x > size ) x = size;
	UFMessage[] M = new UFMessage[x];
	UFMessage temp;
	int n = 0;
	for (int j = 0; j < x; j++) {
	    temp = (UFMessage)_VLog.elementAt(j);
	    if (_showDups || !temp.duplicate) {
		M[n] = temp; 
		n++;
	    }
	}
	String[] messages = new String[n];
	for (int j = 0; j < n; j++) messages[j] = M[j].getMessage(_showTimes); 
	return messages;
    }

    public void showFirstMessages(int x) {
	int size = _VLog.size();
	if( size < 1 ) return;
	if( x > size ) x = size;
	String messages = "";
	_NmsgDisp = 0;
	for (int j = 0; j < x; j++) {
	    UFMessage M = (UFMessage)_VLog.elementAt(j);
	    if (_showDups || !M.duplicate) {
		messages += M.getMessage(_showTimes) ;
		++_NmsgDisp;
	    }
	}
	this.setText(messages);
	this.setToolTipText();
    }

    public void appendFirstMessages(int x) {
	int size = _VLog.size();
	if( size < 1 ) return;
	if( x > size ) x = size;

	for (int j = 0; j < x; j++) {
	    UFMessage M = (UFMessage)_VLog.elementAt(j);
	    if( _showDups || !M.duplicate )
		this.append( M.getMessage(_showTimes) );
	}

	this.setToolTipText();
    }

   public String getMessageAt(int x) {
      UFMessage M = (UFMessage)_VLog.elementAt(x);
      return M.getMessage(_showTimes);
   }

   public void showMessageAt(int x) {
      UFMessage M = (UFMessage)_VLog.elementAt(x);
      _NmsgDisp = 1;
      this.setText( M.getMessage(_showTimes) );
      this.setToolTipText();
   }

   public void appendMessageAt(int x) {
      UFMessage M = (UFMessage)_VLog.elementAt(x);
      this.append( M.getMessage(_showTimes) );
      this.setToolTipText();
   }

   public String[] getMessagesBetween(int x, int y) {
      if (x > y) {
	int temp = x;
	x = y;
	y = temp;
      }
      UFMessage[] M = new UFMessage[y-x];
      UFMessage temp;
      int n = 0;
      for (int j = x; j <= y; j++) {
	  if (j >= 0 && j < _VLog.size()) {
	      temp=(UFMessage)_VLog.elementAt(j);
	      if (_showDups || !temp.duplicate) M[n++] = temp;
	  }
      }
      String[] messages = new String[n];
      for (int j = 0; j < n; j++) messages[j] = M[j].getMessage(_showTimes); 
      return messages;
   }

   public void showMessagesBetween(int x, int y) {
      if (x > y) {
        int temp = x;
        x = y;
        y = temp;
      }
      String messages = "";
      _NmsgDisp = 0;
      for (int j = x; j <= y; j++) {
        if (j >= 0 && j < _VLog.size()) {
	   UFMessage M = (UFMessage)_VLog.elementAt(j);
           if (_showDups || !M.duplicate) {
              messages += M.getMessage(_showTimes) ;
	      ++_NmsgDisp;
           }
        }
      }
      this.setText(messages);
      this.setToolTipText();
   }

   public void appendMessagesBetween(int x, int y) {
      if (x > y) {
        int temp = x;
        x = y;
        y = temp;
      }

      for (int j = x; j <= y; j++) {
        if (j >= 0 && j < _VLog.size()) {
	   UFMessage M = (UFMessage)_VLog.elementAt(j);
	   if( _showDups || !M.duplicate )
	       this.append( M.getMessage(_showTimes) );
        }
      }

      this.setToolTipText();
   }

   public void removeLastMessages(int x) {
      int size = _VLog.size();
      for (int j = size-x; j < size; j++) {
	  if (j >=0 && j < _VLog.size()) {
	      Object vr = _VLog.remove(j);
	      vr = null;
	  }
      }
   }

   public void removeFirstMessages(int x) {
      for (int j = 0; j < x; j++) {
	  if (j >=0 && j < _VLog.size()) {
	      Object vr = _VLog.remove(j);
	      vr = null;
	  }
      }
   }

   public void removeMessagesBetween(int x, int y) {
      if (x > y) {
        int temp = x;
        x = y;
        y = temp;
      }
      for (int j = x; j <= y; j++) {
	  if (j >=0 && j < _VLog.size()) {
	      Object vr = _VLog.remove(j);
	      vr = null;
	  }
      }
   }

   public static void main(String[] args) {
      UFMessageLog log = new UFMessageLog();
      JFrame f = new JFrame();
      JPanel p = new JPanel();
      p.setPreferredSize(new Dimension(450,300));
      p.add(log);
      f.getContentPane().add(p);
      f.pack();
      f.setVisible(true);
      log.addMessage("This is a test.");
      log.addMessage("Test 2");
      log.addMessage("Test 2");
      log.addMessage("Test 3");
      log.showMessageAt(1);
   }
}
//----------------------------------------------------------------------------------------

class UFMessage {
   public String message, timeStamp;
   public boolean duplicate;

   public UFMessage(String message, String timeStamp, boolean duplicate) {
      this.message = message;
      this.timeStamp = timeStamp;
      this.duplicate = duplicate;
   }

    public UFMessage(String message, String timeStamp) {
	this(message, timeStamp, false);
    }

    public UFMessage(String message) {
	this(message, false);
    }

    public UFMessage(String message, boolean duplicate) {
	this.message = message;
	this.duplicate = duplicate;
	String date = new Date( System.currentTimeMillis() ).toString();
	this.timeStamp = date.substring(4,19);
    }

    public String getMessage(boolean withTimeStamp)
    {
	if( withTimeStamp )
	    return new String(timeStamp + " >  " + message + "\n");
	else
	    return new String(message + "\n");
    }
}
