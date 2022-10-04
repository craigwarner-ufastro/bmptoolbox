package javaUFLib;
/**
 * Title:        UFHelperPlot.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2005
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  UFPlotPanel for flam2helper
 */

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.event.*;

public class UFHelperPlot extends UFPlotPanel implements ActionListener {
    public static final
        String rcsID = "$Name:  $ $Id: UFHelperPlot.java,v 1.2 2011/01/20 23:07:14 warner Exp $";

    protected Vector vx;
    protected Vector[] vy;
    protected GraphThread graphThread;
    protected boolean showY[];
    protected int numSensors = 0, startIndex, minTokens = 14;
    protected String[] sensorNames, oPlotOpts;
    protected String title = "", plotOpts = "", units = "hours", addOpts = "";
    protected JMenuItem resetRangeItem;

    String logFileName, reqChar = "/";
    long currTime;
    NewFlam2Helper.UFHelperPlotPanel hpp = null;

    protected String[] dragObjectStatus = {"Hidden", "Hidden", "Hidden", "Hidden"};
    protected Color[] dragObjectColor = {Color.RED, Color.ORANGE, Color.GREEN, Color.BLUE};
    protected int[] dragObjectX = {100, 150, 200, 250};
    protected int[] dragObjectY = {100, 150, 200, 250};
    protected boolean[] dragObject = {false, false, false, false};
    protected int mouseButton = 0;
    protected int xisav, yisav, xInit = 0, yInit = 0;
    protected boolean isOldLog = false;
    int pos = 0;

    public UFHelperPlot() {
      super();
    }

    public UFHelperPlot(int numberOfSensors, int startIndex, String logFile) {
      logFileName = logFile;
      numSensors = numberOfSensors;
      this.startIndex = startIndex;
      vx = new Vector(100000,10000);
      vy = new Vector[numSensors];
      showY = new boolean[numSensors];
      sensorNames = new String[numSensors];
      oPlotOpts = new String[numSensors];
      for (int j = 0; j < numSensors; j++) {
	vy[j] = new Vector(100000,10000);
	showY[j] = true;
	sensorNames[j] = "Sensor "+j;
	oPlotOpts[j] = "";
      }
      setupPlot(640, 512);
    }

    public UFHelperPlot(String[] namesOfSensors, int startIndex, String logfile) {
      this(namesOfSensors.length, startIndex, logfile);
      for (int j = 0; j < numSensors; j++) {
	sensorNames[j] = namesOfSensors[j];
      }
    }

    public void setMinTokens(int minTokens) {
      this.minTokens = minTokens;
    }

    public void setReqChar(String reqChar) {
      this.reqChar = reqChar;
    }

    public void updateLogFile(String logFile) {
      if (! this.logFileName.equals(logFile)) {
	logFileName = logFile;
	vx.removeAllElements();
	vx = new Vector(100000,10000);
        for (int j = 0; j < numSensors; j++) {
	   vy[j].removeAllElements();
	   vy[j] = new Vector(100000,10000);
	}
	if (plotOpts.indexOf(",*title") != -1) {
	   plotOpts = plotOpts.substring(0, plotOpts.indexOf(",*title"));
	}
	startThread();
      }
    }

    public void updatePlotOpts(String plotOpts, String[] oPlotOpts) {
      this.plotOpts = plotOpts;
      this.oPlotOpts = oPlotOpts;
    }

    public void setPlotPanel(NewFlam2Helper.UFHelperPlotPanel hpp) {
      this.hpp = hpp;
      if (!resetRangeItem.isVisible()) resetRangeItem.setVisible(true);
    }

    public void setupPlot(int xdim, int ydim) {
      this.xdim = xdim;
      this.ydim = ydim;
      this.xpos2 = xdim - 20;
      this.ypos2 = ydim - 47;
      this.setBackground(Color.black);
      this.setForeground(Color.white);
      this.setPreferredSize(new Dimension(xdim, ydim));

      popMenu = new JPopupMenu();

      exportItem = new JMenuItem("Export as PNG/JPEG");
      popMenu.add(exportItem);
      exportItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
           JFileChooser jfc = new JFileChooser(saveDir);
           int returnVal = jfc.showSaveDialog((Component)ev.getSource());
           if (returnVal == JFileChooser.APPROVE_OPTION) {
              String filename = jfc.getSelectedFile().getAbsolutePath();
              saveDir = jfc.getCurrentDirectory();
              File f = new File(filename);
              if (f.exists()) {
                String[] saveOptions = {"Overwrite","Cancel"};
                int n = JOptionPane.showOptionDialog(UFHelperPlot.this, filename+" already exists.", "File exists!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, saveOptions, saveOptions[1]);
                if (n == 1) {
                   return;
                }
              }
              String format = "png";
              if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) format = "jpeg";
	      UFHelperPlot upp = UFHelperPlot.this;
              try {
                BufferedImage image = new BufferedImage(upp.xdim, upp.ydim, BufferedImage.TYPE_INT_BGR);
                image.createGraphics().drawImage(offscreenImg,0,0,upp.xdim,upp.ydim,upp);
                ImageIO.write(image, format, f);
              } catch(IOException e) {
                System.err.println("UFPlotPanel error > could not create JPEG image!");
              }
           }
        }
      });

      printItem = new JMenuItem("Print or Save");
      popMenu.add(printItem);
      printItem.addActionListener(this);

      resetRangeItem = new JMenuItem("Reset Range");
      resetRangeItem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	   hpp.xMinField.setText("0");
           hpp.yMinField.setText("");
           hpp.xMaxField.setText("");
           hpp.yMaxField.setText("");
           hpp.plotButton.doClick();
        }
      });
      popMenu.add(resetRangeItem);
      if (hpp == null) resetRangeItem.setVisible(false);

      addMouseListener(new MouseListener() {
        public void mouseClicked(MouseEvent evt) {
        }

        public void mousePressed(MouseEvent ev) {
	   for (int j = 0; j < 4; j++) dragObject[j] = false;
           if ((ev.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
              if(ev.isPopupTrigger()) {
                popMenu.show(ev.getComponent(), ev.getX(), ev.getY());
              }
           }

           if ((ev.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
              if (xyFrame != null) {
                xyFrame.xField.setText(""+ev.getX());
                xyFrame.yField.setText(""+ev.getY());
                xyFrame.deviceButton.setSelected(true);
              }
           }
	   if ((ev.getModifiers() & InputEvent.BUTTON2_MASK) != 0 ) {
	      mouseButton = 2;
	      xInit = ev.getX();
	      yInit = ev.getY();
	      for (int j = 0; j < 4; j++) {
		if (dragObjectStatus[j].equals("V Line") && Math.abs(xInit-dragObjectX[j]) < 30) {
		   xisav = dragObjectX[j];
                   yisav = 0;
                   for (int l = 0; l < 4; l++) {
		      if (l == j) dragObject[l] = true; else dragObject[l] = false;
		   }
		} else if (dragObjectStatus[j].equals("H Line") && Math.abs(yInit-dragObjectY[j]) < 30) {
		   xisav = 0;
                   yisav = dragObjectY[j];
                   for (int l = 0; l < 4; l++) {
		      if (l == j) dragObject[l] = true; else dragObject[l] = false;
                   }
                } else if (dragObjectStatus[j].equals("+") && Math.abs(xInit-dragObjectX[j]) < 30 && Math.abs(yInit-dragObjectY[j]) < 30) {
                   xisav = dragObjectX[j];
                   yisav = dragObjectY[j];
                   for (int l = 0; l < 4; l++) {
		      if (l == j) dragObject[l] = true; else dragObject[l] = false;
                   }
                } else if (dragObjectStatus[j].equals("X") && Math.abs(xInit-dragObjectX[j]) < 30 && Math.abs(yInit-dragObjectY[j]) < 30) {
                   xisav = dragObjectX[j];
                   yisav = dragObjectY[j];
                   for (int l = 0; l < 4; l++) {
	 	      if (l == j) dragObject[l] = true; else dragObject[l] = false;
                   }
                } else if (dragObjectStatus[j].equals("Circle")  && Math.abs(xInit-dragObjectX[j]) < 30 && Math.abs(yInit-dragObjectY[j]) < 30) {
                   xisav = dragObjectX[j];
                   yisav = dragObjectY[j];
                   for (int l = 0; l < 4; l++) {
		      if (l == j) dragObject[l] = true; else dragObject[l] = false;
                   }
                } else dragObject[j] = false;
	      }
	   }
        }

        public void mouseReleased(MouseEvent evt) {
        }

        public void mouseEntered(MouseEvent evt) {
        }

        public void mouseExited(MouseEvent evt) {
        }
      });

      addMouseMotionListener(new MouseMotionAdapter() {
	public void mouseMoved(MouseEvent mev) {
        }

        public void mouseDragged(MouseEvent mev) {
	   int xOffset = mev.getX()-xInit;
	   int yOffset = mev.getY()-yInit;
	   int xi = xisav + xOffset;
	   int yi = yisav + yOffset;
	   if (mouseButton == 2 ) {
	      for (int j = 0; j < 4; j++) {
		if (dragObject[j]) {
		   dragObjectX[j] = xi;
		   dragObjectY[j] = yi;
	        }
	      }
	      repaint();
	   }
        }
      });
    }

    public void initPlot() {
      offscreenImg = createImage(xdim,ydim);
      while( offscreenImg == null ) {
        offscreenImg = createImage(xdim,ydim);
	try {
	  Thread.sleep(50);
	} catch(InterruptedException e) {}
      }
      offscreenG = (Graphics2D)offscreenImg.getGraphics();
      offscreenG.setColor(backColor);
      offscreenG.fillRect(0,0,xdim,ydim);
      offscreenG.setColor(plotColor);
      offscreenG.setFont(mainFont);
    }

    public void parseParams(String s) {
      super.parseParams(s);
      if (xrange[1] - xrange[0] > 20) {
	for (int j = 0; j < xtickname.length; j++) {
	   xtickname[j] = ""+(int)(Float.parseFloat(xtickname[j]));
	}
      }
      if (yrange[1] - yrange[0] > 20) {
	for (int j = 0; j < ytickname.length; j++) {
	   ytickname[j] = ""+(int)(Float.parseFloat(ytickname[j]));
	}
      }
    }

    public void setLinLog(float[] x, float[] y) {
      linX = new float[x.length];
      logX = new float[x.length];
      linY = new float[y.length];
      logY = new float[y.length];
      for (int j = 0; j < x.length; j++) {
        linX[j] = x[j];
      }
      for (int j = 0; j < y.length; j++) {
        linY[j] = y[j];
      }
    }

    public void updateUnits(String units) {
      this.units = units;
    }

    public void updatePlot() {
      long t = System.currentTimeMillis();
      long[] x = new long[vx.size()];
      float[][] ys = new float[vy.length][vy[vy.length-1].size()];
      float[] tempy = new float[ys[0].length];
      for (int j = 0; j < x.length; j++) {
        x[j] = ((Long)(vx.get(j))).longValue();
        for (int l = 0; l < ys.length; l++) {
           ys[l][j] = ((Double)(vy[l].get(j))).floatValue();
        }
      }
      tempy[0] = UFArrayOps.minValue(ys);
      tempy[x.length-1] = UFArrayOps.maxValue(ys);
//System.out.println(tempy[0]+" "+tempy[x.length-1]+" "+UFArrayOps.minValue(ys)+" "+UFArrayOps.maxValue(ys));
      long minX = UFArrayOps.minValue(x);
      float[] newx;
      if (minX > 3600) {
	newx = UFArrayOps.castAsFloats(UFArrayOps.subArrays(x, minX));
      } else newx = UFArrayOps.castAsFloats(x);
      for (int j = 1; j < tempy.length-1; j++) {
	tempy[j] = tempy[0];
      }
      plotOpts+=",*title="+title;
      if (units.equalsIgnoreCase("minutes")) {
	newx = UFArrayOps.divArrays(newx, 60.0f);
	plotOpts+=",*xtitle=Minutes";
      } else if (units.equalsIgnoreCase("hours")) {
	newx = UFArrayOps.divArrays(newx, 3600.0f);
	plotOpts+=",*xtitle=Hours";
      } else if (units.equalsIgnoreCase("days")) {
	newx = UFArrayOps.divArrays(newx, 86400.0f);
	plotOpts+=",*xtitle=Days";
      } else {
	plotOpts+=",*xtitle=Seconds";
      }
//System.out.println("time: "+(System.currentTimeMillis()-t));
      super.plot(newx, tempy, "*nodata"+plotOpts+addOpts);
      for (int j = 0; j < ys.length; j++) {
	if (showY[j]) super.overplot(newx, ys[j], oPlotOpts[j]);
      }
    }

    public String [] getSensorNames() { return sensorNames;}

    public void calcZoom() {
      if (sxinit==0 || syinit==0 || sxfin==0 || syfin==0 ) return;
      if (Math.abs(sxinit-sxfin) < 3 && Math.abs(syinit-syfin) < 3) return;
      float x1 = (Math.min( sxinit, sxfin )-xoff)/xscale;
      float y1 = (Math.min( yoff-syinit, yoff-syfin ))/yscale;
      float x2 = (Math.max( sxinit, sxfin )-xoff)/xscale;
      float y2 = (Math.max( yoff-syinit, yoff-syfin ))/yscale;
      x1 = (float)(Math.floor(x1*1000)*.001);
      x2 = (float)(Math.floor(x2*1000)*.001);
      y1 = (float)(Math.floor(y1*1000)*.001);
      y2 = (float)(Math.floor(y2*1000)*.001);
      if ((""+x1).equals("NaN") || (""+y1).equals("NaN") || (""+x2).equals("NaN") || (""+y2).equals("NaN")) return;
      //String s = "*xrange=["+(int)x1+","+(int)x2+"], *yrange=["+(int)y1+","+(int)y2+"],"+plotOpts;
      if (hpp != null) {
        hpp.xMinField.setText(""+x1);
        hpp.xMaxField.setText(""+x2);
        hpp.yMinField.setText(""+y1);
        hpp.yMaxField.setText(""+y2);
        hpp.plotButton.doClick();
      }
    }

   public void paintComponent( Graphics g ) {
      super.paintComponent(g);
      int xlines = 0;
      int ylines = 0;
      float x1p = 0,x2p,y1p = 0,y2p;
      if (hpp == null) return;
      for (int j = 0; j < 4; j++) {
        g.setColor(dragObjectColor[j]);
        if (dragObjectStatus[j].equals("V Line")) {
           g.drawLine(dragObjectX[j], 0, dragObjectX[j], ydim);
           xlines++;
           if (xlines == 1) {
              hpp.x1l.setForeground(dragObjectColor[j]);
              x1p = (dragObjectX[j]-xoff)/xscale;
              x1p = (float)(Math.floor(x1p*1000)*.001);
              hpp.x1l.setText("X1="+x1p);
           } else if (xlines == 2) {
              hpp.x2l.setForeground(dragObjectColor[j]);
              x2p = (dragObjectX[j]-xoff)/xscale;
              x2p = (float)(Math.floor(x2p*1000)*.001);
              hpp.x2l.setText("X2="+x2p);
              hpp.dxl.setText("dX="+(x1p-x2p));
           }
        } else if (dragObjectStatus[j].equals("H Line")) {
           g.drawLine(0, dragObjectY[j], xdim, dragObjectY[j]);
           ylines++;
           if (ylines == 1) {
              hpp.y1l.setForeground(dragObjectColor[j]);
              y1p = (yoff-dragObjectY[j])/yscale;
              y1p = (float)(Math.floor(y1p*1000)*.001);
              hpp.y1l.setText("Y1="+y1p);
           } else if (ylines == 2) {
              hpp.y2l.setForeground(dragObjectColor[j]);
              y2p = (yoff-dragObjectY[j])/yscale;
              y2p = (float)(Math.floor(y2p*1000)*.001);
              hpp.y2l.setText("Y2="+y2p);
              hpp.dyl.setText("dY="+(y1p-y2p));
           }
        } else if (dragObjectStatus[j].equals("+")) {
           int xc = dragObjectX[j];
           int yc = dragObjectY[j];
           g.drawLine(xc,yc-20,xc,yc+20);
           g.drawLine(xc-20,yc,xc+20,yc);
           g.setColor(Color.BLACK);
           g.drawLine(xc-1,yc-20,xc-1,yc+20);
           g.drawLine(xc+1,yc-20,xc+1,yc+20);
           g.drawLine(xc-20,yc-1,xc+20,yc-1);
           g.drawLine(xc-20,yc+1,xc+20,yc+1);
           g.setColor(dragObjectColor[j]);
        } else if (dragObjectStatus[j].equals("X")) {
           int xc = dragObjectX[j];
           int yc = dragObjectY[j];
           g.drawLine(xc-20,yc-20,xc+20,yc+20);
           g.drawLine(xc+20,yc-20,xc-20,yc+20);
           g.setColor(Color.BLACK);
           g.drawLine(xc-21,yc-20,xc+19,yc+20);
           g.drawLine(xc-19,yc-20,xc+21,yc+20);
           g.drawLine(xc+21,yc-20,xc-19,yc+20);
           g.drawLine(xc+19,yc-20,xc-21,yc+20);
           g.setColor(dragObjectColor[j]);
        } else if (dragObjectStatus[j].equals("Circle")) {
           g.drawOval(dragObjectX[j]-10, dragObjectY[j]-10, 20, 20);
           g.setColor(Color.BLACK);
           g.drawOval(dragObjectX[j]-11, dragObjectY[j]-11, 22, 22);
           g.drawOval(dragObjectX[j]-9, dragObjectY[j]-9, 18, 18);
           g.setColor(dragObjectColor[j]);
        }
      }
      if (xlines < 2) {
        hpp.x2l.setForeground(Color.BLACK);
        hpp.x2l.setText("X2=");
        hpp.dxl.setText("");
      }
      if (xlines < 1) {
        hpp.x1l.setForeground(Color.BLACK);
        hpp.x1l.setText("X1=");
      }
      if (ylines < 2) {
        hpp.y2l.setForeground(Color.BLACK);
        hpp.y2l.setText("Y2=");
        hpp.dyl.setText("");
      }
      if (ylines < 1) {
        hpp.y1l.setForeground(Color.BLACK);
        hpp.y1l.setText("Y1=");
      }
    }

    public void startThread() {
        if (isThreadAlive())
            stopThread();
        graphThread = new GraphThread();
        graphThread.start();
    }

    public boolean isThreadAlive() {
        if (graphThread == null) return false;
        return graphThread.isAlive();
    }

    public void stopThread() {

        graphThread.keepRunning = false;
        while (graphThread.stillRunning);//wait for graphThread to exit
    }

    /* -------------------------------------------------------------------------------  */

    //Class GraphThread
    public class GraphThread extends Thread {
        public boolean keepRunning = true;
        public boolean stillRunning = false;

        public void run() {
	    BufferedReader br = null;
            long beginTimestamp = 0;
            keepRunning = true; stillRunning = true;
	    String s = " ";
	    StringTokenizer st;
	    boolean hasData = true;
	    double temp;
            while (keepRunning) {
                try {
                    if (br == null) {
			/* setup Buffered Reader */
			br = new BufferedReader(new FileReader(logFileName));
			try {
			    st = new StringTokenizer(br.readLine());
                            st.nextToken();st.nextToken();
                            beginTimestamp = Long.parseLong(st.nextToken());
			    title = "Relative to";
			    while (st.hasMoreTokens()) {
			      title += " " + st.nextToken(); 
			    }
			} catch (NoSuchElementException ne) {
			    title = "Relative to ????";
			    beginTimestamp = 0;
			}
                        br.readLine(); // skip over header line
			int n = 0;
			while (s != null) {
			   s = br.readLine();
			   if (s == null) break;
			   if (s.indexOf(reqChar) == -1) continue;
                           if (s.endsWith(",")) continue;
                           st = new StringTokenizer(s,",");
			   if (st.countTokens() < minTokens) continue;
			   st.nextToken();
			   currTime = Integer.parseInt(st.nextToken().trim());
			   //skip to the start index specified in the constructor
			   for (int i=0; i<startIndex; i++) st.nextToken();
			   vx.add(new Long(currTime));
			   for (int i=0; i<numSensors; i++) {
			      try {
				temp = Double.parseDouble(st.nextToken().trim());
			      } catch(Exception e) {
				temp = 0;
			      }
			      if (temp > -2000) {
				vy[i].add(new Double(temp));
			      } else vy[i].add(new Double(0.));
			   }
			} /* end while */
		    } /* end first pass of reading */

                    stillRunning = true;
                    s = br.readLine();
		    if (s == null && hasData) { 
			updatePlot();
			hasData = false;
		    }

                    if (s == null || s.trim().equals("")) { 
			Thread.sleep(1000);
			continue;
		    }
		    hasData = true;
                    if (s.indexOf(reqChar) == -1) continue;
		    if (s.endsWith(",")) continue;
                    st = new StringTokenizer(s,",");
                    if (st.countTokens() < minTokens) continue;
                    st.nextToken();
                    currTime = Integer.parseInt(st.nextToken().trim());
                    //skip to the start index specified in the constructor
                    for (int i=0; i<startIndex; i++) {
			try {
			    st.nextToken();
			} catch (NoSuchElementException e) {
			    e.printStackTrace();
			    continue;
			}
		    }
		    vx.add(new Long(currTime));
		    for (int i=0; i<numSensors; i++) {
			try {
			    temp = Double.parseDouble(st.nextToken().trim());
			} catch(Exception e) {
			    temp = 0;
			}
			if (temp > -2000) {
			    vy[i].add(new Double(temp));
			} else vy[i].add(new Double(0.));
                    }
                } catch (Exception e) {
                    System.err.println("UFGraphPanel.GraphThread.run> "+e.toString());
		    e.printStackTrace();
		    if (e.toString().indexOf("Element") == -1) keepRunning = stillRunning = false;
                }
            }
            stillRunning = false;
        }
    } //end of class GraphThread

//-----------------------------------------------------------------------------//

    public static void main(String[] args) {
      JFrame jf = new JFrame();
      UFHelperPlot hp = new UFHelperPlot(2, 5, "testlog");
      jf.getContentPane().add(hp);
      jf.pack();
      jf.setVisible(true);
      hp.startThread();
    }
}
