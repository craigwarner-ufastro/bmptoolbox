package javaUFLib;

//Title:        UFTail
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2003-8
//Author:       David Rashkin, Craig Warner
//Company:      University of Florida
//Description:  Extension of JTabbedPane to view log files of agents.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.Vector;

public class UFTail extends JTabbedPane {

    public static final
	String rcsID = "$Name:  $ $Id: UFTail.java,v 1.17 2013/10/30 22:14:12 warner Exp $";
    private JFrame _tailFrame;
    private boolean _autoScroll = true;
    protected Vector <String> v = new Vector();

    public UFTail(String filename) {
	/* New constructor for a tail of a single file */
	addNewTail(filename);
        addChangeListener(new ChangeListener(){
                public void stateChanged(ChangeEvent e) {
                    setBackgroundAt( getSelectedIndex(), null );
                }
            });
    }
    public UFTail(String[] fileNames) {	this( null, fileNames ); }
    public UFTail(String[] fileNames, String directory) { this( directory, fileNames ); }

    public UFTail(String directory, String[] fileNames)
    {
	if( directory != null ) {
	    if( directory.length() > 0 ) {
		int ps = directory.lastIndexOf("/");
		if( ps != directory.length()-1 ) directory += "/";
		for( int i=0; i<fileNames.length; i++ ) addNewTail( directory + fileNames[i] );
	    }
	    else for( int i=0; i<fileNames.length; i++ ) addNewTail( fileNames[i] );
	}
	else for( int i=0; i<fileNames.length; i++ ) addNewTail( fileNames[i] );

	addChangeListener(new ChangeListener(){
		public void stateChanged(ChangeEvent e) {
		    setBackgroundAt( getSelectedIndex(), null );
		}
	    });
    }

    public void addNewTail(String fileName) {
	final String _fileName = fileName.trim();
	try{
	    File f = new File( _fileName );
	    final BufferedInputStream fbr = new BufferedInputStream(new FileInputStream(f));
	    final UFTextArea textArea = new UFTextArea(3); // 3MB max.
	    textArea.setEditable(false);
	    textArea.autoScrollToBottom(false); //this is done here below.
	    textArea.setToolTipText("/ or ^F to Search, Left-click = no-auto-scroll, Right-click = auto-scroll");
	    final int myIdx = getTabCount();
	    final JScrollPane jsp = new JScrollPane(textArea);
	    add( jsp, f.getName() );
	    v.add(_fileName);

	    textArea.addKeyListener(new KeyListener(){
		    public void keyPressed(KeyEvent ke) {
			if( (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_F) ||
			    (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_S) ||
			    (ke.getKeyCode() == KeyEvent.VK_SLASH) ||
			    (ke.getKeyCode() == KeyEvent.VK_FIND) ) {

			    //extract name for JFrame:
			    String jfname;
			    int ppos = _fileName.indexOf(".");

			    if( ppos > 0 )
				jfname = _fileName.substring(++ppos);
			    else {
				int spos = _fileName.lastIndexOf("/");
				if( spos > 0 )
				    jfname = _fileName.substring(++spos);
				else
				    jfname = _fileName;
			    }

			    final JFrame jf = new JFrame(jfname);
			    JPanel searchPanel = new JPanel();
			    final JLabel errLabel = new JLabel("");
			    final JTextField searchField = new JTextField("",10);
			    final JCheckBox caseBox = new JCheckBox("Case Sensitive",true);
			    final JCheckBox wrapBox = new JCheckBox("Wrap search",true);
			    searchPanel.add(new JLabel("Search String: "));
			    searchPanel.add(searchField);
			    searchPanel.add(caseBox);
			    searchPanel.add(wrapBox);
			    searchPanel.add(errLabel);
			    JButton searchButton = new JButton("Search");

			    searchButton.addActionListener(new ActionListener(){
				    public void actionPerformed(ActionEvent ae) {
					String ahhhh = textArea.getText();
					String searchy = searchField.getText();
					errLabel.setText("");
					if (!caseBox.isSelected()) {
					    ahhhh = ahhhh.toLowerCase();
					    searchy = searchy.toLowerCase();
					}
					int pos = textArea.getCaretPosition();
					int foundPos = ahhhh.indexOf(searchy,pos+1);
					if (foundPos != -1) {
					    textArea.setCaretPosition(foundPos);
					    textArea.setSelectionStart(foundPos);
					    textArea.setSelectionEnd( foundPos + searchy.length() );
					}
					else if (wrapBox.isSelected()) {
					    pos = 0;
					    foundPos = ahhhh.indexOf(searchy,pos);
					    if (foundPos != -1) {
						textArea.setCaretPosition(foundPos);
						textArea.setSelectionStart(foundPos);
						textArea.setSelectionEnd( foundPos + searchy.length() );
					    }
					    else errLabel.setText("String not found");
					}
					else errLabel.setText("String not found");
				    }
				});

			    JButton cancelButton = new JButton("Cancel");

			    cancelButton.addActionListener(new ActionListener(){
				    public void actionPerformed(ActionEvent ae) {
					jf.setVisible(false);
				    }
				});

			    JPanel buttonPanel = new JPanel();
			    buttonPanel.add(searchButton);
			    buttonPanel.add(cancelButton);

			    jf.getContentPane().add(searchPanel,BorderLayout.CENTER);
			    jf.getContentPane().add(buttonPanel,BorderLayout.SOUTH);
			    jf.setSize(200,220);
			    jf.setLocation(getLocation());
			    jf.setVisible(true);
			    jf.setAlwaysOnTop(true);
			}
		    }

		    public void keyReleased(KeyEvent ke) {}
		    public void keyTyped(KeyEvent ke) {}
		});

	    //create thread that reads the file:

	    new Thread() {
		private int _maxBytes = 2*1024*1024;

		public void run() {
		    _autoScroll = true;
		    
		    textArea.addMouseListener(new MouseListener(){
			    public void mousePressed(MouseEvent me) {
				if (me.getButton() == me.BUTTON1) _autoScroll = false;
				else if (me.getButton() == me.BUTTON3) _autoScroll = true;
			    }
			    public void mouseReleased(MouseEvent me) {}
			    public void mouseClicked(MouseEvent me){}
			    public void mouseExited(MouseEvent me){}
			    public void mouseEntered(MouseEvent me){}
			});

		    int nreads = 0;
		    int nerrs = 0;
		    byte bLast = 0; //this keeps track of last character read.

		    while (true) {
			try {
			    int nba = fbr.available();

			    if( nba > 0 ) {

				int bstart = 0;
				int nbread = nba;

				if( nba > _maxBytes ) {
				    nbread = _maxBytes;
				    long nskip = fbr.skip( nba - nbread );
				    System.out.println("UFTail(" + _fileName + ")> " + nba +
						       " bytes available, skipped " + nskip +
						       " bytes, reading " + nbread + " bytes.");
				}

				byte[] bf = new byte[nbread];
				fbr.read( bf, 0, bf.length );
				++nreads;
				//convert "\r" to "\n" if previous byte was not "\n", else blank:

				for( int i=0; i<bf.length; i++ ) {
				    if( bf[i] == 13 ) {
					bf[i] = 10;
					if( i > 0 ) {
					    if( bf[i-1] == 10 ) bf[i] = 32;
					}
					else if( bLast == 10 ) bf[i] = 32;
				    }
				}

				textArea.append(new String(bf));
				bLast = bf[bf.length-1];

				if (myIdx != getSelectedIndex())
				    setBackgroundAt( myIdx, Color.yellow );

				if (_autoScroll) {
				    JScrollBar jsb = jsp.getVerticalScrollBar();
				    jsb.setValue( jsb.getMaximum() );
                   		    textArea.setCaretPosition( textArea.getDocument().getLength() );
				}
			    }

			    yield();
			    sleep(900);
			}
			catch (Exception e) {
			    e.printStackTrace();
			    System.err.println("UFTail("+_fileName+").run> "+e.toString());
			    System.err.println("UFTail("+_fileName+").run> nreads=" + nreads);
			    if( ++nerrs > 30 ) break;
			    yield();
			    try{ sleep(2000); } catch (Exception x) {}
			}
		    }
		}
	    }.start();
	}
	catch (Exception e) {
	    System.err.println("UFTail> " + _fileName + " : " + e.toString());
	}
    }

    public void viewFrame(String title) {
      if (_tailFrame != null) {
	_tailFrame.setVisible(true);
	_tailFrame.setState( Frame.NORMAL );
	return;
      }
      JPanel controlPanel = new JPanel();
      JButton closeButton = new JButton("Close");
      controlPanel.add(closeButton);
      closeButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	  _tailFrame.dispose();
	}
      });
      final JCheckBox AutoScroll = new JCheckBox("AutoScroll ",true);
      controlPanel.add(AutoScroll);
      AutoScroll.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	  setAutoScroll(AutoScroll.isSelected());
	}
      });

      _tailFrame = new JFrame("UFTail: " + title);
      _tailFrame.setSize(1000,780);
      setPreferredSize(new Dimension(1000,740));
      controlPanel.setPreferredSize(new Dimension(900,40));
      _tailFrame.getContentPane().setLayout(new BorderLayout());
      _tailFrame.getContentPane().add( this, BorderLayout.NORTH );
      _tailFrame.getContentPane().add( controlPanel, BorderLayout.SOUTH );
      _tailFrame.pack();
      _tailFrame.setVisible(true);
    }

    public void setAutoScroll(boolean autoscroll) {
      _autoScroll = autoscroll;
    }

    public boolean hasTail(String fileName) {
        String _fileName = fileName.trim();
	if (v.contains(_fileName)) return true;
	return false;
    }

    public static void main(String [] args) {
	JFrame x = new JFrame();
	int narg = args.length;
	String directory = args[0] + "/";
	String[] files = new String[narg-1];
	for( int i=1; i<narg; i++ ) files[i-1] = args[i];
	x.setContentPane( new UFTail( directory, files ) );
	x.setSize(800,440);
	x.setTitle("UFTail:"+directory);
	x.setDefaultCloseOperation(3);
	x.setVisible(true);
    }
}
