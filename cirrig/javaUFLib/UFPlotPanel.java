package javaUFLib;

//Title:        UFPlotPanel.java
//Version:      2.0
//Copyright:    Copyright (c) UF 2008
//Author:       Craig Warner (some mods by Frank Varosi)
//Company:      University of Florida
//Description:  Extends JPanel for purpose of plotting x-y arrays.

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class UFPlotPanel extends javax.swing.JPanel implements ActionListener {
   public static final
	String rcsID = "$Name:  $ $Id: UFPlotPanel.java,v 1.47 2017/05/22 18:24:56 warner Exp $";
   protected int xdim, ydim;
   protected final int ufpxdim, ufpydim;
   protected int pxdim1, pxdim2, pydim1, pydim2;
   protected Graphics2D offscreenG;
   protected Image offscreenImg;
   protected float xmin, xmax, ymin, ymax, xscale, yscale, xoff, yoff, xstep, ystep;
   protected float xtickinterval, ytickinterval, binsize, minhist, maxhist;
   protected float[] xrange = {-1, -1};
   protected float[] yrange = {-1, -1};
   protected int xticks = -1, yticks = -1, charSize = 12, xcharSize, ycharSize;
   protected int xminor, yminor, psym, symsize, xticklen, yticklen, nbins;
   protected int xpos1 = 60, ypos1 = 25, xpos2, ypos2; 
   protected float[] xtickv, ytickv;
   public float[] logX, linX, logY, linY, x, y;
   protected int[] oldYh, usymxs, usymys, multi={0, 1, 1};
   protected String[] xtickname, ytickname;
   protected String fontName = "Dialog", xfontName, yfontName, title, xtitle, ytitle;
   protected Font mainFont, xFont, yFont;
   protected Color plotColor, backColor, oplotColor, axesColor;
   protected boolean noData = false, xlog = false, ylog = false;
   protected boolean doHist = false, fillHist = false;
   protected boolean endXLabels = true, endYLabels = true;
   protected JPopupMenu popMenu;
   public JMenuItem exportItem, printItem, quitItem, resetSizeItem, gpPanelItem, resetRangeItem, saveItem;
   protected final JFrame ufp;
   protected XYFrame xyFrame;
   protected int sxinit, syinit, sxfin, syfin, mouseButton = 0;
   protected boolean zoomMode = false;
   protected String plotOpts;
   protected File saveDir = new File(".");

   public UFPlotPanel() {
      this(640,512,new JFrame());
   }

   public UFPlotPanel(int xdim, int ydim, final JFrame ufp) {
      this.xdim = xdim;
      this.ydim = ydim;
      this.ufpxdim = xdim;
      this.ufpydim = ydim;
      this.ufp = ufp;
      this.xpos2 = xdim - 20;
      this.ypos2 = ydim - 47;
      this.setBackground(Color.black);
      this.setForeground(Color.white);
      this.setPreferredSize(new Dimension(xdim, ydim));
      popMenu = new JPopupMenu();

      //export menu option:
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
                int n = JOptionPane.showOptionDialog(UFPlotPanel.this, filename+" already exists.", "File exists!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, saveOptions, saveOptions[1]);
                if (n == 1) {
                   return;
                }
	      }
	      String format = "png";
	      if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) format = "jpeg";
	      UFPlotPanel upp = UFPlotPanel.this;
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

      // add the mouse event and motion Listeners here first:

      addMouseListener(new java.awt.event.MouseListener() {

	public void mouseClicked(java.awt.event.MouseEvent evt) {}

        public void mousePressed(java.awt.event.MouseEvent ev) {
	   mouseButton = 0;
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
	      } else { 
		mouseButton = 1;
		zoomMode = true;
		sxinit = sxfin = ev.getX();
		syinit = syfin = ev.getY();
	      }
	   }
	}

        public void mouseReleased(java.awt.event.MouseEvent evt) {
	   if( zoomMode ) {
	      sxfin = evt.getX();
	      syfin = evt.getY();
	      zoomMode = false;
	      repaint();
	      calcZoom();
	   }
	}

        public void mouseEntered(java.awt.event.MouseEvent evt) {}

        public void mouseExited(java.awt.event.MouseEvent evt) {}
      });

      addMouseMotionListener(new MouseMotionListener() {

	public void mouseMoved(MouseEvent evt) {}
	
	public void mouseDragged(MouseEvent evt) {
	   if( mouseButton == 1) {
	      if( zoomMode ) {
		sxfin = evt.getX();
		syfin = evt.getY();
		repaint();
	      }
	   }
	}
      });

      // if not hooked to a JFrame then do not add any further menu items.
      if( this.ufp == null ) return;

      //menu option to reset size:
      resetSizeItem = new JMenuItem("Reset Size");
      popMenu.add(resetSizeItem);
      resetSizeItem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
           resizePlot(ufpxdim, ufpydim);
	   if (ufp.getClass().getName().indexOf("GatorPlot") != -1) {
	      GatorPlot gp = (GatorPlot)ufp;
	      if (gp.showPanels)
		ufp.setSize(ufpxdim + 164, ufpydim + 154);
	      else ufp.setSize(ufpxdim + 4, ufpydim + 26);
	   }
	   else ufp.setSize(ufpxdim + 4, ufpydim + 26);
	}
      });

      if (ufp.getClass().getName().indexOf("GatorPlot") != -1) {
	final GatorPlot gp = (GatorPlot)ufp;
	resetRangeItem = new JMenuItem("Reset Range");
	resetRangeItem.addActionListener(new ActionListener() {
	   public void actionPerformed(ActionEvent ev) {
	      gp.xMinField.setText("");
	      gp.yMinField.setText("");
	      gp.xMaxField.setText("");
              gp.yMaxField.setText("");
	      gp.plotButton.doClick();
	   }
	});
        popMenu.add(resetRangeItem);
	if (gp.showPanels) gpPanelItem = new JMenuItem("Hide Option Panels");
	else gpPanelItem = new JMenuItem("Show Option Panels");
	gpPanelItem.addActionListener(new ActionListener() {
	   public void actionPerformed(ActionEvent ev) {
	      if (gp.showPanels) {
		gp.showPanels = false;
		gpPanelItem.setText("Show Option Panels");
		gp.remove(gp.leftPanel);
		gp.remove(gp.bottomPanel);
		gp.setSize(ufpxdim + 4, ufpydim + 26);
		gp.validate();
		gp.repaint();
	      } else {
		gp.showPanels = true;
		gpPanelItem.setText("Hide Option Panels");
		gp.getContentPane().add(gp.leftPanel, BorderLayout.WEST);
		gp.getContentPane().add(gp.bottomPanel, BorderLayout.SOUTH);
                gp.setSize(ufpxdim + 164, ufpydim + 154);
		gp.validate();
		gp.repaint();
	      }
	   }
	});
	popMenu.add(gpPanelItem);
      }
      
      quitItem = new JMenuItem("Quit");
      popMenu.add(quitItem);
      quitItem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	   ufp.dispose();
	}
      });

   }

   public void initPlot() {
      offscreenImg = createImage(xdim,ydim);
      if( offscreenImg == null ) {
	  ufp.setVisible(true);
	  offscreenImg = createImage(xdim,ydim);
      }
      offscreenG = (Graphics2D)offscreenImg.getGraphics();
      offscreenG.setColor(backColor);
      offscreenG.fillRect(0,0,xdim,ydim);
      offscreenG.setColor(plotColor);
      offscreenG.setFont(mainFont);
   }

   public void parseParams(String s) {
      int n;
      int m;

      n = s.indexOf("*xlog");
      if (n != -1) xlog = true;
      n = s.indexOf("*ylog");
      if (n != -1) ylog = true;

      n = s.indexOf("*xlinear");
      if (n != -1) xlog = false;
      n = s.indexOf("*ylinear");
      if (n != -1) ylog = false;

      if (!doHist) {
	if (xlog) x = logX; else x = linX;
	if (ylog) y = logY; else y = linY;

        //Set mins, maxes after getting rid of <= 0 for log
        try { xmin = UFArrayOps.minValue(x); }
        catch (NullPointerException e) { xmin = 0; }
        try { xmax = UFArrayOps.maxValue(x); }
        catch (NullPointerException e) { xmax = 0; }
        try { ymin = UFArrayOps.minValue(y); }
        catch (NullPointerException e) { ymin = 0; }
        try { ymax = UFArrayOps.maxValue(y); }
        catch (NullPointerException e) { ymax = 0; }
      }

      n = s.indexOf("*xrange");
      if (n != -1) {
	n = s.indexOf("[",n);
	m = s.indexOf(",",n);
	xrange[0] = Float.parseFloat(s.substring(n+1, m));
	if (xrange[0] <= 0 && xlog) xrange[0] = (float)Math.pow(10, Math.floor(Math.log(xmin)/Math.log(10))); 
	n = s.indexOf("]",m);
	xrange[1] = Float.parseFloat(s.substring(m+1, n));
      } else {
	if (xlog) {
	   xrange[0] = (float)Math.pow(10, Math.floor(Math.log(xmin)/Math.log(10)));
	   xrange[1] = (float)Math.pow(10, 1+Math.floor(Math.log(xmax)/Math.log(10)));
	} else {
           xrange[0] = (float)(Math.floor(xmin/Math.pow(10, Math.floor(Math.log(xmax-xmin)/Math.log(10)))-0.1)*Math.pow(10, Math.floor(Math.log(xmax-xmin)/Math.log(10))));
           xrange[1] = (float)(Math.floor(1+xmax/Math.pow(10, Math.floor(Math.log(xmax-xmin)/Math.log(10))))*Math.pow(10, Math.floor(Math.log(xmax-xmin)/Math.log(10))));
	}
	if (doHist) xrange[0] = 0;
      }
      n = s.indexOf("*yrange");
      if (n != -1) {
        n = s.indexOf("[",n);
        m = s.indexOf(",",n);
        yrange[0] = Float.parseFloat(s.substring(n+1, m));
	if (yrange[0] <= 0 && ylog) yrange[0] = (float)Math.pow(10, Math.floor(Math.log(ymin)/Math.log(10))); 
        n = s.indexOf("]",m);
        yrange[1] = Float.parseFloat(s.substring(m+1, n));
      } else {
        if (ylog) {
           yrange[0] = (float)Math.pow(10, Math.floor(Math.log(ymin)/Math.log(10)));
	   yrange[1] = (float)Math.pow(10, 1+Math.floor(Math.log(ymax)/Math.log(10)));
        } else {
           yrange[0] = (float)(Math.floor(ymin/Math.pow(10, Math.floor(Math.log(Math.abs(ymax-ymin))/Math.log(10)))-0.1)*Math.pow(10, Math.floor(Math.log(Math.abs(ymax-ymin))/Math.log(10))));
           yrange[1] = (float)(Math.floor(1+ymax/Math.pow(10, Math.floor(Math.log(Math.abs(ymax-ymin))/Math.log(10))))*Math.pow(10, Math.floor(Math.log(Math.abs(ymax-ymin))/Math.log(10))));
	}
	if (ymin == ymax && Float.isNaN(yrange[0])) {
	    yrange[0] = ymin-1;
	    yrange[1] = ymax+1;
	}
      }

      n = s.indexOf("*xminval");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xrange[0] = Float.parseFloat(s.substring(n+1,m));
	if (xlog && xrange[0] <= 0) xrange[0] = (float)Math.pow(10, Math.floor(Math.log(xmin)/Math.log(10)));
      }

      n = s.indexOf("*xmaxval");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xrange[1] = Float.parseFloat(s.substring(n+1,m));
      }

      n = s.indexOf("*yminval");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        yrange[0] = Float.parseFloat(s.substring(n+1,m));
	if (ylog && yrange[0] <= 0) yrange[0] = (float)Math.pow(10, Math.floor(Math.log(ymin)/Math.log(10)));
      }

      n = s.indexOf("*ymaxval");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        yrange[1] = Float.parseFloat(s.substring(n+1,m));
      }

      xstep=(float)Math.pow(10, Math.floor(Math.log(Math.abs(xrange[1]-xrange[0]))/Math.log(10))); 
      ystep=(float)Math.pow(10, Math.floor(Math.log(Math.abs(yrange[1]-yrange[0]))/Math.log(10)));
      n = s.indexOf("*xticks");
      if (n != -1) {
	n = s.indexOf("=", n);
	m = s.indexOf(",", n);
	if (m < 0) m = s.length();
	xticks = (int)Float.parseFloat(s.substring(n+1,m));
      } else {
	if (xlog) xticks = (int)Math.ceil(Math.log(xrange[1])/Math.log(10.0)) - (int)Math.floor(Math.log(xrange[0])/Math.log(10.0)) + 1;
	else xticks = (int)Math.floor((xrange[1]-xrange[0])/xstep+1);
      }
      if (xticks < 2) xticks = 2;
      if (doHist && xticks < 4) xticks = 4;
      n = s.indexOf("*yticks");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        yticks = (int)Float.parseFloat(s.substring(n+1,m));
      } else {
	if (ylog) yticks = (int)Math.ceil(Math.log(Math.abs(yrange[1]))/Math.log(10.0)) - (int)Math.floor(Math.log(Math.abs(yrange[0]))/Math.log(10.0)) + 1;
	else yticks = (int)Math.floor((yrange[1]-yrange[0])/ystep+1);
      }

      if (yticks < 3) yticks = 3;

      //if (doHist) xstep = nbins/xticks;
      if (doHist) {
      	xstep=(float)Math.pow(10, Math.floor(Math.log((maxhist-minhist)/3)/Math.log(10)));
      	xstep=(float)Math.floor((maxhist-minhist)/(xticks-1)/xstep)*xstep;
      }
      xtickv = new float[xticks];
      ytickv = new float[yticks];
      n = s.indexOf("*xtickv");
      if (n != -1) {
	n = s.indexOf("[", n);
	m = s.indexOf(",",n);
	int b = 0;
	while (b < xticks && m > -1 && m < s.indexOf("]", n)) {
	   xtickv[b] = Float.parseFloat(s.substring(n+1,m));
	   b++;
	   n = m; 
	   m = s.indexOf(",", n+1);
	}
	if (b < xticks) {
	   m = s.indexOf("]", n);
	   xtickv[b] = Float.parseFloat(s.substring(n+1, m));
	}
      } else if (!doHist) {
	if (xlog) {
	   int ax = (int)Math.floor(Math.log(xrange[0])/Math.log(10.0));
	   for (int j = ax; j <= (int)Math.ceil(Math.log(xrange[1])/Math.log(10.0)); j++) if (j-ax < xticks) xtickv[j-ax] = (float)Math.pow(10.0,j);
	} else {
	   if (xrange[0] % xstep == 0) xtickv[0] = xrange[0];
	   else xtickv[0] = (float)Math.floor(xrange[0]/xstep + 1)*xstep;
	   for (int j = 1; j < xticks; j++) xtickv[j] = xtickv[0] + j * xstep;
	}
      } else {
	if (minhist % xstep == 0) xtickv[0] = minhist;
        else xtickv[0] = (float)Math.floor(minhist/xstep + 1)*xstep;
        for (int j = 1; j < xticks; j++) xtickv[j] = xtickv[0] + j * xstep;
	for (int j = 0; j < xticks; j++) xtickv[j] = (xtickv[j]-minhist) * xrange[1] / (maxhist-minhist);
      }

      n = s.indexOf("*ytickv");
      if (n != -1) {
        n = s.indexOf("[", n);
        m = s.indexOf(",",n);
        int b = 0;
        while (b < xticks && m > -1 && m < s.indexOf("]", n)) {
           ytickv[b] = Float.parseFloat(s.substring(n+1,m));
           b++;
           n = m;
           m = s.indexOf(",", n+1);
        }
        if (b < yticks) {
           m = s.indexOf("]", n);
           ytickv[b] = Float.parseFloat(s.substring(n+1, m));
        }
      } else {
	if (ylog) {
	   int ay = (int)Math.floor(Math.log(yrange[0])/Math.log(10.0));
	   for (int j = ay; j <= (int)Math.ceil(Math.log(yrange[1])/Math.log(10.0)); j++) ytickv[j-ay] = (float)Math.pow(10.0, j); 
        } else {
           if (yrange[0] % ystep == 0) ytickv[0] = yrange[0];
           else ytickv[0] = (float)Math.floor(yrange[0]/ystep + 1)*ystep;
           for (int j = 1; j < yticks; j++) ytickv[j] = ytickv[0] + j * ystep;
	   if( ytickv[yticks-1] > yrange[1] ) ytickv[yticks-1] = yrange[1];
	}
      }

      n = s.indexOf("*xtickinterval");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xtickinterval = Float.parseFloat(s.substring(n+1,m));
        float xtimin = xtickinterval*(float)Math.floor(xrange[0]/xtickinterval);
        float xtimax = xtickinterval*(float)Math.ceil(xrange[1]/xtickinterval);
        xticks = (int)((xtimax-xtimin)/xtickinterval)+1;
        xtickv = new float[xticks];
        for (int j = 0; j < xticks; j++)
           xtickv[j] = xtimin + j*xtickinterval;
      }

      n = s.indexOf("*ytickinterval");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        ytickinterval = Float.parseFloat(s.substring(n+1,m));
        float ytimin = ytickinterval*(float)Math.floor(yrange[0]/ytickinterval);
        float ytimax = ytickinterval*(float)Math.ceil(yrange[1]/ytickinterval);
        yticks = (int)((ytimax-ytimin)/ytickinterval)+1;
        ytickv = new float[yticks];
        for (int j = 0; j < yticks; j++)
           ytickv[j] = ytimin + j*ytickinterval;
      }

      xtickname = new String[xticks];
      ytickname = new String[yticks];
      n = s.indexOf("*xtickname");

      if (n != -1) {
        n = s.indexOf("[", n);
        m = s.indexOf(",",n);
        int b = 0;
        while (b < xticks && m > -1 && m < s.indexOf("]", n)) {
           xtickname[b] = s.substring(n+1,m);
           b++;
           n = m;
           m = s.indexOf(",", n+1);
        }
        if (b < xticks) {
           m = s.indexOf("]", n);
           xtickname[b] = s.substring(n+1, m);
        }
      } else {
        for (int j = 0; j < xticks; j++) {
	   if (doHist) {
	       xtickname[j] = "" + (int)Math.floor(xtickv[j]*(maxhist-minhist)/xrange[1]+minhist);//+0.5);
	   }
	   else {
	       xtickname[j] = String.valueOf( xtickv[j] );
	       if( xtickname[j] != null && !xlog && xtickname[j].indexOf("E") < 0 ) {
		   long rtickv = (long)Math.round( (double)xtickv[j] * 10000 );
		   xtickname[j] = String.valueOf( (double)rtickv/10000 );
	       }
	   }
	}
      }

      n = s.indexOf("*ytickname");
      if (n != -1) {
        n = s.indexOf("[", n);
        m = s.indexOf(",",n);
        int b = 0;
        while (b < yticks && m > -1 && m < s.indexOf("]", n)) {
           ytickname[b] = s.substring(n+1,m);
           b++;
           n = m;
           m = s.indexOf(",", n+1);
        }
        if (b < yticks) {
           m = s.indexOf("]", n);
           ytickname[b] = s.substring(n+1, m);
        }
      } else {
	  for (int j = 0; j < yticks; j++) ytickname[j] = String.valueOf( ytickv[j] );
	  if( ! ylog ) {
	      for (int j = 0; j < yticks; j++) {
		  if( ytickname[j] != null && ytickname[j].indexOf("E") < 0 ) {
		      long rtickv = (long)Math.round( (double)ytickv[j] * 10000.0 );
		      ytickname[j] = String.valueOf( (double)rtickv/10000 );
		  }
	      }
	  }
      }

      n = s.indexOf("*xminor");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xminor = (int)Float.parseFloat(s.substring(n+1,m));
      } else {
	if (xlog) xminor = 1;
	else if (xstep % 6 == 0) xminor = 6;
	else if (xstep % 5 == 0) xminor = 5;
	else if (xstep % 4 == 0) xminor = 4;
	else if (xstep % 3 == 0) xminor = 3;
	else if (xstep % 2 == 0) xminor = 2;
	else xminor = 5;
      } 
      n = s.indexOf("*yminor");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        yminor = (int)Float.parseFloat(s.substring(n+1,m));
      } else {
	if (ylog) yminor = 1;
        else if (ystep % 6 == 0) yminor = 6;
        else if (ystep % 5 == 0) yminor = 5;
        else if (ystep % 4 == 0) yminor = 4;
        else if (ystep % 3 == 0) yminor = 3;
        else if (ystep % 2 == 0) yminor = 2;
        else yminor = 5;
      } 

      n = s.indexOf("*xticklen");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
	xticklen = (int)Float.parseFloat(s.substring(n+1,m));
      } else xticklen = 10;

      n = s.indexOf("*yticklen");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        yticklen = (int)Float.parseFloat(s.substring(n+1,m));
      } else yticklen = 10;

      n = s.indexOf("*charsize");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        charSize = (int)Float.parseFloat(s.substring(n+1,m));
      }
      n = s.indexOf("*font");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
	fontName = s.substring(n+1, m);
      }
      n = s.indexOf("*xcharsize");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xcharSize = (int)Float.parseFloat(s.substring(n+1,m));
      } else xcharSize = charSize;
      n = s.indexOf("*xfont");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xfontName = s.substring(n+1, m);
      } else xfontName = fontName;
      n = s.indexOf("*ycharsize");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        ycharSize = (int)Float.parseFloat(s.substring(n+1,m));
      } else ycharSize = charSize;
      n = s.indexOf("*yfont");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        yfontName = s.substring(n+1, m);
      } else yfontName = fontName;
      mainFont = new Font(fontName, 0, charSize);
      xFont = new Font(xfontName, 0, xcharSize);
      yFont = new Font(yfontName, 0, ycharSize);

      n = s.indexOf("*title");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        title = s.substring(n+1, m);
      } else title = "";
      n = s.indexOf("*xtitle");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xtitle = s.substring(n+1, m);
      } else xtitle = "";
      n = s.indexOf("*ytitle");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        ytitle = s.substring(n+1, m);
      } else ytitle = "";

      n = s.indexOf("*color");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        int red = (int)Float.parseFloat(s.substring(n+1, m));
        n = s.indexOf(",",m+1);
        int green = (int)Float.parseFloat(s.substring(m+1, n));
	m = s.indexOf(",", n+1);
	if (m < 0) m = s.length();
	int blue = (int)Float.parseFloat(s.substring(n+1, m));
	plotColor = new Color(red, green, blue);
      } else plotColor = new Color(0, 0, 0);

      n = s.indexOf("*background");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        int red = (int)Float.parseFloat(s.substring(n+1, m));
        n = s.indexOf(",",m+1);
        int green = (int)Float.parseFloat(s.substring(m+1, n));
        m = s.indexOf(",", n+1);
        if (m < 0) m = s.length();
        int blue = (int)Float.parseFloat(s.substring(n+1, m));
        backColor = new Color(red, green, blue);
      } else backColor = new Color(255, 255, 255);

      n = s.indexOf("*axescolor");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        int red = (int)Float.parseFloat(s.substring(n+1, m));
        n = s.indexOf(",",m+1);
        int green = (int)Float.parseFloat(s.substring(m+1, n));
        m = s.indexOf(",", n+1);
        if (m < 0) m = s.length();
        int blue = (int)Float.parseFloat(s.substring(n+1, m));
        axesColor = new Color(red, green, blue);
      } else axesColor = new Color(0, 0, 0);

      n = s.indexOf("*nodata");
      if (n != -1) noData = true; else noData = false;

      n = s.indexOf("*psym");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
	psym = (int)Float.parseFloat(s.substring(n+1, m));
      } else psym = 0;

      n = s.indexOf("*symsize");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
	symsize = (int)Float.parseFloat(s.substring(n+1, m));
      } else symsize = 5;

      n = s.indexOf("*xmargin");
      if (n != -1) {
        n = s.indexOf("[",n);
        m = s.indexOf(",",n);
        xpos1 = pxdim1 + (int)Float.parseFloat(s.substring(n+1, m));
        n = s.indexOf("]",m);
        xpos2 = pxdim2 - (int)Float.parseFloat(s.substring(m+1, n));
      }

      n = s.indexOf("*ymargin");
      if (n != -1) {
        n = s.indexOf("[",n);
        m = s.indexOf(",",n);
        ypos1 = pydim1 + (int)Float.parseFloat(s.substring(n+1, m));
        n = s.indexOf("]",m);
        ypos2 = pydim2 - (int)Float.parseFloat(s.substring(m+1, n));
      }

      n = s.indexOf("*position");
      if (n != -1) {
        n = s.indexOf("[",n);
        m = s.indexOf(",",n);
        xpos1 = (int)(pxdim1+(pxdim2-pxdim1)*Float.parseFloat(s.substring(n+1, m)));
        n = s.indexOf(",",m+1);
        ypos1 = (int)(pydim1+(pydim2-pydim1)*Float.parseFloat(s.substring(m+1, n)));
        m = s.indexOf(",", n+1);
	xpos2 = (int)(pxdim1+(pxdim2-pxdim1)*Float.parseFloat(s.substring(n+1, m)));
	n = s.indexOf("]", m+1);
	ypos2 = (int)(pydim1+(pydim2-pydim1)*Float.parseFloat(s.substring(m+1, n)));
      }

      n = s.indexOf("*noendxlabels");
      if (n != -1) endXLabels = false; else endXLabels = true;

      n = s.indexOf("*noendylabels");
      if (n != -1) endYLabels = false; else endYLabels = true;
   }

   public void drawAxes(float[] x, float[] y, String s) {
      pxdim1 = (int)((xdim/multi[1]) * (multi[0] % multi[1]));
      pxdim2 = (int)((xdim/multi[1]) * (1 + multi[0] % multi[1]));
      pydim1 = (int)((ydim/multi[2]) * (multi[0] / multi[2]));
      pydim2 = (int)((ydim/multi[2]) * (1 + multi[0] / multi[2]));
      xpos1 = pxdim1 + 60;
      xpos2 = pxdim2 - 20;
      ypos1 = pydim1 + 25;
      ypos2 = pydim2 - 47;
      setLinLog(x, y);
      parseParams(s);
      if (multi[0] == 0) this.initPlot();
      offscreenG.setColor(axesColor);
      if (!xlog) {
	xscale = (xpos2-xpos1)/(xrange[1]-xrange[0]);
	xoff = xpos1-(xscale*xrange[0]);
      } else x = doXlog(x);
      if (!ylog) {
	yscale = (ypos2-ypos1)/(yrange[1]-yrange[0]);
        yoff = ypos2+(yscale*yrange[0]);
      } else y = doYlog(y);
      int[] xaxis = {xpos1, xpos1, xpos2, xpos2, xpos1};
      int[] yaxis = {ypos1, ypos2, ypos2, ypos1, ypos1};
      offscreenG.drawPolyline(xaxis, yaxis, 5);
      this.drawTicks();
      offscreenG.setFont(xFont);
      offscreenG.drawString(xtitle, Math.max(xpos1, (int)((xpos2+xpos1)/2)-(int)((xtitle.length()-1)*(0.25*charSize))), ypos2+35);
      offscreenG.setFont(yFont);
      for (int j = 0; j < ytitle.length(); j++) {
	offscreenG.drawString( ytitle.substring(j, j+1), xpos1-54,
			       Math.max(ypos1, (int)((ypos2+ypos1)/2))-(int)((ytitle.length()-1)*(0.25*charSize))+j*12);
      }
      offscreenG.setFont(mainFont);
      offscreenG.drawString(title, Math.max(xpos1, (int)((xpos2+xpos1)/2)-(int)((title.length()-1)*(0.26*charSize))), ypos1-7);
      offscreenG.setColor(plotColor);
      repaint();
   }

   public void setLinLog(float[] x, float[] y) {
      linX = new float[x.length];
      logX = new float[x.length];
      linY = new float[y.length];
      logY = new float[y.length];
      float subx, suby;
      if (UFArrayOps.maxValue(x) > 0) {
	subx = UFArrayOps.minValue(UFArrayOps.extractValues(x, UFArrayOps.where(x, ">", 0)))/10.f;
      } else subx = 1.e-10f;
      for (int j = 0; j < x.length; j++) {
	linX[j] = x[j];
	logX[j] = Math.max(x[j], subx);
      } 
      if (UFArrayOps.maxValue(y) > 0) { 
	suby = UFArrayOps.minValue(UFArrayOps.extractValues(y, UFArrayOps.where(y, ">", 0)))/10.f;
      } else suby = 1.e-10f;
      for (int j = 0; j < y.length; j++) {
        linY[j] = y[j];
        logY[j] = Math.max(y[j], suby);
      }
   }

   public float[] doXlog(float[] x) {
      xscale = (xpos2-xpos1)/(float)(Math.log(xrange[1])/Math.log(10.0)-Math.log(xrange[0])/Math.log(10.0));
      xoff = xpos1-(xscale*(float)(Math.log(xrange[0])/Math.log(10.0)));
      for (int j = 0; j < xtickv.length; j++) {
        xtickv[j] = (float)(Math.log(xtickv[j])/Math.log(10.0));
      }
      for (int j = 0; j < x.length; j++) x[j] = (float)(Math.log(logX[j])/Math.log(10.0));
      return x;
   }

   public float[] doYlog(float[] y) {
      yscale = (ypos2-ypos1)/(float)(Math.log(yrange[1])/Math.log(10.0)-Math.log(yrange[0])/Math.log(10.0));
      yoff = ypos2+(yscale*(float)(Math.log(yrange[0])/Math.log(10.0)));
      for (int j = 0; j < ytickv.length; j++) {
        ytickv[j] = (float)(Math.log(ytickv[j])/Math.log(10.0));
      }
      for (int j = 0; j < y.length; j++) y[j] = (float)(Math.log(logY[j])/Math.log(10.0));
      return y;
   }

   public float[] undoXlog(float[] x) {
      for (int j = 0; j < x.length; j++) {
        x[j] = linX[j];
      }
      return x;
   }

   public float[] undoYlog(float[] y) {
      for (int j = 0; j < y.length; j++) {
        y[j] = linY[j];
      }
      return y;
   }

   public void setLinLogX(float[] x) {
      linX = new float[x.length];
      logX = new float[x.length];
      float subx;
      if (UFArrayOps.maxValue(x) > 0) {
	subx = UFArrayOps.minValue(UFArrayOps.extractValues(x, UFArrayOps.where(x, ">", 0)))/10.f;
      } else subx = 1.e-10f;
      for (int j = 0; j < x.length; j++) {
        linX[j] = x[j];
        logX[j] = Math.max(x[j], subx);
      }
   }

   public void setLinLogY(float[] y) {
      linY = new float[y.length];
      logY = new float[y.length];
      float suby;
      if (UFArrayOps.maxValue(y) > 0) {
	suby = UFArrayOps.minValue(UFArrayOps.extractValues(y, UFArrayOps.where(y, ">", 0)))/10.f;
      } else suby = 1.e-10f;
      for (int j = 0; j < y.length; j++) {
        linY[j] = y[j];
        logY[j] = Math.max(y[j], suby);
      }
   }

   public float[] doXlogHist(float[] x) {
      if (xrange[0] < UFArrayOps.minValue(logX)) xrange[0] = UFArrayOps.minValue(logX);
      for (int j = 0; j < x.length; j++) x[j] = (float)(Math.log(logX[j])/Math.log(10.0));
      xrange[0] = (float)(Math.log(xrange[0])/Math.log(10.0));
      xrange[1] = (float)(Math.log(xrange[1])/Math.log(10.0));
      binsize = (xrange[1]-xrange[0])/nbins;
      return x;
   }

   public void drawTicks() {
      int tickpos, minorpos, lastpos;
      offscreenG.setFont(xFont);
      try {
        lastpos = (int)Math.floor(xoff+xscale*(2*xtickv[0]-xtickv[1])+0.5);
      } catch(ArrayIndexOutOfBoundsException e) {
        lastpos = 0;
      }
      if (xticks == 1) { return; }
      for (int j = 0; j <= xticks; j++) {
	if (j < xticks) tickpos = (int)Math.floor(xoff+xscale*xtickv[j]+0.5);
	else tickpos = (int)Math.floor(xoff+xscale*(2*xtickv[j-1]-xtickv[j-2])+0.5);
	if (tickpos <= xpos2 && tickpos >= xpos1 && j < xticks) {
	   offscreenG.drawLine(tickpos, ypos2, tickpos, ypos2-xticklen);
	   offscreenG.drawLine(tickpos, ypos1, tickpos, ypos1+xticklen);
	   if (xtickname[j] != null)
	   offscreenG.drawString(xtickname[j], tickpos-(int)(xtickname[j].length()-1)*4, ypos2+15);
	}
	for (int l = 0; l < xminor; l++) {
	   minorpos = (int)Math.floor(lastpos+l*(tickpos-lastpos)/xminor+0.5);
	   if (minorpos <= xpos2 && minorpos >= xpos1) {
	      offscreenG.drawLine(minorpos, ypos2, minorpos, ypos2-xticklen/2);
	      offscreenG.drawLine(minorpos, ypos1, minorpos, ypos1+xticklen/2);
	   }	
	}
	lastpos = tickpos;
      }
      if (endXLabels && !doHist && xlog && Math.log(xrange[0])/Math.log(10.0) < xtickv[0]) offscreenG.drawString(""+xrange[0], xpos1-(int)((""+xrange[0]).length()-1)*4, ypos2+15);
      if (endXLabels && !doHist && !xlog && xrange[0] < xtickv[0]) offscreenG.drawString(""+xrange[0], xpos1-(int)((""+xrange[0]).length()-1)*4, ypos2+15);
      if (endXLabels && !doHist && xlog && (float)Math.log(xrange[1])/Math.log(10.0) > xtickv[xticks-1]) offscreenG.drawString(""+xrange[1], xpos2-(int)((""+xrange[1]).length()-1)*4, ypos2+15);
      if (endXLabels && !doHist && !xlog && xrange[1] > xtickv[xticks-1]) offscreenG.drawString(""+xrange[1], xpos2-(int)((""+xrange[1]).length()-1)*4, ypos2+15);

      offscreenG.setFont(yFont);
      lastpos = (int)Math.floor(yoff-(yscale*(2*ytickv[0]-ytickv[1])-0.5));
      for (int j = 0; j <= yticks; j++) {
        if (j < yticks) tickpos = (int)Math.floor(yoff-(yscale*ytickv[j]-0.5));
	else tickpos = (int)Math.floor(yoff-(yscale*(2*ytickv[j-1]-ytickv[j-2])-0.5));
        if (tickpos <= ypos2 && tickpos >= ypos1 && j < yticks) {
           offscreenG.drawLine(xpos1, tickpos, xpos1+yticklen, tickpos);
	   offscreenG.drawLine(xpos2, tickpos, xpos2-yticklen, tickpos);
	   if (ytickname[j] != null)
	   offscreenG.drawString(ytickname[j], xpos1-(int)ytickname[j].length()*8, tickpos+4);
	}
        for (int l = 0; l < yminor; l++) {
	   minorpos = (int)Math.floor(lastpos+l*(tickpos-lastpos)/yminor+0.5);
           if (minorpos <= ypos2 && minorpos >= ypos1) {
	      offscreenG.drawLine(xpos2, minorpos, xpos2-yticklen/2, minorpos);
              offscreenG.drawLine(xpos1, minorpos, xpos1+yticklen/2, minorpos);
           }
        }
	lastpos = tickpos;
      }
      if (endYLabels && ylog && Math.log(yrange[0])/Math.log(10.0) < ytickv[0]) offscreenG.drawString(""+yrange[0], xpos1-(int)(""+yrange[0]).length()*8, ypos2);
      if (endYLabels && !ylog && yrange[0] < ytickv[0]) offscreenG.drawString(""+yrange[0], xpos1-(int)(""+yrange[0]).length()*8, ypos2);
      if (endYLabels && ylog && (float)Math.log(yrange[1])/Math.log(10.0) > ytickv[yticks-1]) offscreenG.drawString(""+yrange[1], xpos1-(int)(""+yrange[1]).length()*8, ypos1+4);
      if (endYLabels && !ylog && yrange[1] > ytickv[yticks-1]) offscreenG.drawString(""+yrange[1], xpos1-(int)(""+yrange[1]).length()*8, ypos1+4);
      offscreenG.setFont(mainFont);
   }

   public void plot(float[] x, float[] y, String s) {
      doHist = false;
      this.x = x;
      this.y = y;
      this.plotOpts = s;
      drawAxes(x, y, s);
      multi[0]++;
      if (multi[0] >= multi[1]*multi[2]) multi[0] = 0;
      if (!noData) makePlot(x, y);
      if (xlog) x = undoXlog(x);
      if (ylog) y = undoYlog(y);
   }

   public void makePlot(float[] x, float[] y) {
      int[] drawx = new int[x.length];
      int[] drawy = new int[x.length];
      offscreenG.setClip(xpos1, ypos1, xpos2-xpos1, ypos2-ypos1);
      for (int j = 0; j < Math.min(x.length, y.length); j++) {
	drawx[j] = (int)Math.floor(xoff+xscale*x[j]+0.5);
	drawy[j] = (int)Math.floor(yoff-(yscale*y[j])+0.5);
	switch(Math.abs(psym) % 10) {
	   case 1:
	      offscreenG.drawLine(drawx[j], drawy[j]-symsize, drawx[j], drawy[j]+symsize);
	      offscreenG.drawLine(drawx[j]-symsize, drawy[j], drawx[j]+symsize, drawy[j]);
	      break;
	   case 2:
              offscreenG.drawLine(drawx[j], drawy[j]-symsize, drawx[j], drawy[j]+symsize);
              offscreenG.drawLine(drawx[j]-symsize, drawy[j], drawx[j]+symsize, drawy[j]);
              offscreenG.drawLine(drawx[j]-symsize, drawy[j]-symsize, drawx[j]+symsize, drawy[j]+symsize);
              offscreenG.drawLine(drawx[j]+symsize, drawy[j]-symsize, drawx[j]-symsize, drawy[j]+symsize);
	      break;
	   case 3:
	      offscreenG.drawLine(drawx[j], drawy[j], drawx[j], drawy[j]);
	      break;
	   case 4:
	      int[] symxs4 = {drawx[j], drawx[j]+symsize, drawx[j], drawx[j]-symsize, drawx[j]};
	      int[] symys4 = {drawy[j]-symsize, drawy[j], drawy[j]+symsize, drawy[j], drawy[j]-symsize};
	      if (Math.abs(psym)==4) offscreenG.drawPolygon(symxs4,symys4,5);
	      if (Math.abs(psym)==14) offscreenG.fillPolygon(symxs4,symys4,5);
	      break;
	   case 5:
	      int[] symxs5 = {drawx[j], drawx[j]+symsize, drawx[j]-symsize, drawx[j]};
	      int[] symys5 = {drawy[j]-symsize, drawy[j]+symsize, drawy[j]+symsize, drawy[j]-symsize};
	      if (Math.abs(psym)==5) offscreenG.drawPolygon(symxs5,symys5,4);
	      if (Math.abs(psym)==15) offscreenG.fillPolygon(symxs5,symys5,4);
              break;
	   case 6:
	      if (Math.abs(psym) == 6) offscreenG.drawRect(drawx[j]-symsize, drawy[j]-symsize, 2*symsize, 2*symsize);
	      if (Math.abs(psym) == 16) offscreenG.fillRect(drawx[j]-symsize, drawy[j]-symsize, 2*symsize, 2*symsize);
	      break;
           case 7:
              offscreenG.drawLine(drawx[j]-symsize, drawy[j]-symsize, drawx[j]+symsize, drawy[j]+symsize);
              offscreenG.drawLine(drawx[j]+symsize, drawy[j]-symsize, drawx[j]-symsize, drawy[j]+symsize);
              break;
	   case 8:
	      if (usymxs != null && usymys != null) {
	        int[] symxs8 = new int[usymxs.length]; 
                int[] symys8 = new int[usymys.length];
		for (int l = 0; l < usymxs.length; l++) {
		   symxs8[l] = drawx[j]+usymxs[l];
		   symys8[l] = drawy[j]+usymys[l];
		} 
                if (Math.abs(psym)==8) offscreenG.drawPolygon(symxs8,symys8,usymxs.length);
                if (Math.abs(psym)==18) offscreenG.fillPolygon(symxs8,symys8,usymys.length);
	      }
	      break;
	   case 9:
	      if (Math.abs(psym) == 9) offscreenG.drawOval(drawx[j]-symsize, drawy[j]-symsize, 2*symsize, 2*symsize);
	      if (Math.abs(psym) == 19) offscreenG.fillOval(drawx[j]-symsize, drawy[j]-symsize, 2*symsize, 2*symsize);
	      break;
	   default:
        }
      }
      if (psym <= 0) offscreenG.drawPolyline(drawx, drawy, drawx.length);
      offscreenG.setClip(0, 0, xdim, ydim); 
      repaint();
   }

   public void usersym(int[] usymxs, int[] usymys) {
      this.usymxs = usymxs;
      this.usymys = usymys;
   }

   public void overplot(float[] x, float[] y, String s) {
      this.parseOplotParams(s);
      offscreenG.setColor(oplotColor);
      setLinLog(x, y);
      if (xlog) x = this.doXlog(x);
      if (ylog) y = this.doYlog(y);
      this.makePlot(x, y);
      offscreenG.setColor(plotColor);
      if (xlog) x = this.undoXlog(x);
      if (ylog) y = this.undoYlog(y);
   }

   public void parseOplotParams(String s) {
      int n;
      int m;

      n = s.indexOf("*color");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        int red = (int)Float.parseFloat(s.substring(n+1, m));
        n = s.indexOf(",",m+1);
        int green = (int)Float.parseFloat(s.substring(m+1, n));
        m = s.indexOf(",", n+1);
        if (m < 0) m = s.length();
        int blue = (int)Float.parseFloat(s.substring(n+1, m));
        oplotColor = new Color(red, green, blue);
      } else oplotColor = new Color(0, 0, 0);

      n = s.indexOf("*psym");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        psym = (int)Float.parseFloat(s.substring(n+1, m));
      } else psym = 0;

      n = s.indexOf("*symsize");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        symsize = (int)Float.parseFloat(s.substring(n+1, m));
      } else symsize = 5;
   }

   public void xyouts(float xc, float yc, String text, String s) {
      int xcoord, ycoord, xyCharSize = 12;
      int n;
      int m;
      String style = "device", xyFontName = "Dialog";
      Color xyColor;
      Font xyFont;

      offscreenG.setClip(0, 0, xdim, ydim);

      n = s.indexOf("*normal");
      if (n != -1) style = "normal";

      n = s.indexOf("*data");
      if (n != -1) style = "data";

      n = s.indexOf("*color");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        int red = (int)Float.parseFloat(s.substring(n+1, m));
        n = s.indexOf(",",m+1);
        int green = (int)Float.parseFloat(s.substring(m+1, n));
        m = s.indexOf(",", n+1);
        if (m < 0) m = s.length();
        int blue = (int)Float.parseFloat(s.substring(n+1, m));
        xyColor = new Color(red, green, blue);
      } else xyColor = new Color(0, 0, 0);

      n = s.indexOf("*charsize");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xyCharSize = (int)Float.parseFloat(s.substring(n+1,m));
      }
      n = s.indexOf("*font");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xyFontName = s.substring(n+1, m);
      }
      xyFont = new Font(xyFontName, 0, xyCharSize);

      if (style.equals("normal")) {
	xcoord = (int)Math.floor(xc * xdim + 0.5);
	ycoord = (int)Math.floor(yc * ydim + 0.5);
      } else if (style.equals("data")) {
        xcoord = (int)Math.floor(xoff+xscale*xc+0.5);
        ycoord = (int)Math.floor(yoff-(yscale*yc+0.5));
      } else {
	xcoord = (int)Math.floor(xc+0.5);
	ycoord = (int)Math.floor(yc+0.5);
      }
      offscreenG.setColor(xyColor);
      offscreenG.setFont(xyFont);
      offscreenG.drawString(text, xcoord, ycoord);
      offscreenG.setColor(plotColor);
      offscreenG.setFont(mainFont);
      repaint();
   }

   public void multi(int curr, int col, int row) {
      this.multi[0] = curr;
      this.multi[1] = col;
      this.multi[2] = row;
   }
 
   public void parseHistParams(String s) {
      int n;
      int m;

      n = s.indexOf("*fill");
      if (n != -1) fillHist = true;
      else fillHist = false;

      n = s.indexOf("*nbins");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        nbins = (int)Float.parseFloat(s.substring(n+1,m));
      } else nbins = 10;

      n = s.indexOf("*xlog");
      if (n != -1) xlog = true;
      n = s.indexOf("*ylog");
      if (n != -1) ylog = true;

      n = s.indexOf("*xlinear");
      if (n != -1) xlog = false;
      n = s.indexOf("*ylinear");
      if (n != -1) ylog = false;

      n = s.indexOf("*xrange");
      if (n != -1) {
	n = s.indexOf("[",n);
	m = s.indexOf(",",n);
	xrange[0] = Float.parseFloat(s.substring(n+1, m));
	if (xrange[0] <= 0 && xlog) xrange[0] = (float)Math.pow(10, Math.floor(Math.log(xmin)/Math.log(10))); 
	n = s.indexOf("]",m);
	xrange[1] = Float.parseFloat(s.substring(m+1, n));
      } else {
	xrange[0] = xmin;
	xrange[1] = xmax;
      }

      n = s.indexOf("*xminval");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xrange[0] = Float.parseFloat(s.substring(n+1,m));
        if (xlog && xrange[0] <= 0) xrange[0] = (float)Math.pow(10, Math.floor(Math.log(xmin)/Math.log(10)));
      }

      n = s.indexOf("*xmaxval");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xrange[1] = Float.parseFloat(s.substring(n+1,m));
      }

      n = s.indexOf("*binsize");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        binsize = Float.parseFloat(s.substring(n+1,m));
        nbins = (int)((xrange[1]-xrange[0])/binsize)+1;
      } else binsize = (xrange[1]-xrange[0])/nbins;
   }

   public void parseMoreHistParams(String s) {
      int n;
      int m;

      xrange[0] = 0;
      xrange[1] = xmax;

      if (ylog) y = logY; else y = linY;

      n = s.indexOf("*yrange");
      if (n != -1) {
        n = s.indexOf("[",n);
        m = s.indexOf(",",n);
        yrange[0] = Float.parseFloat(s.substring(n+1, m));
	if (yrange[0] <= 0 && ylog) yrange[0] = (float)Math.pow(10, Math.floor(Math.log(ymin)/Math.log(10))); 
        n = s.indexOf("]",m);
        yrange[1] = Float.parseFloat(s.substring(m+1, n));
      } else {
        if (ylog) {
           yrange[0] = (float)Math.pow(10, Math.floor(Math.log(ymin)/Math.log(10)));
           yrange[1] = (float)Math.pow(10, 1+Math.floor(Math.log(ymax)/Math.log(10)));
        } else {
           yrange[0] = (float)(Math.floor(ymin/Math.pow(10, Math.floor(Math.log(Math.abs(ymax-ymin))/Math.log(10)))-0.1)*Math.pow(10, Math.floor(Math.log(Math.abs(ymax-ymin))/Math.log(10))));
           yrange[1] = (float)(Math.floor(1+ymax/Math.pow(10, Math.floor(Math.log(Math.abs(ymax-ymin))/Math.log(10))))*Math.pow(10, Math.floor(Math.log(Math.abs(ymax-ymin))/Math.log(10))));
        }
      }

      n = s.indexOf("*yminval");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        yrange[0] = Float.parseFloat(s.substring(n+1,m));
	if (ylog && yrange[0] <= 0) yrange[0] = (float)Math.pow(10, Math.floor(Math.log(ymin)/Math.log(10)));
      }

      n = s.indexOf("*ymaxval");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        yrange[1] = Float.parseFloat(s.substring(n+1,m));
      }

      xstep=(float)Math.pow(10, Math.floor(Math.log(Math.abs(xrange[1]-xrange[0]))/Math.log(10))); 
      ystep=(float)Math.pow(10, Math.floor(Math.log(Math.abs(yrange[1]-yrange[0]))/Math.log(10)));
      n = s.indexOf("*xticks");
      if (n != -1) {
	n = s.indexOf("=", n);
	m = s.indexOf(",", n);
	if (m < 0) m = s.length();
	xticks = (int)Float.parseFloat(s.substring(n+1,m));
      } else {
	if (xlog) xticks = (int)Math.ceil(Math.log(xrange[1])/Math.log(10.0)) - (int)Math.floor(Math.log(xrange[0])/Math.log(10.0)) + 1;
	else xticks = (int)Math.floor((xrange[1]-xrange[0])/xstep+1);
      }
      if (xticks < 2) xticks = 2;
      if (doHist && xticks < 4) xticks = 4;
      n = s.indexOf("*yticks");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        yticks = (int)Float.parseFloat(s.substring(n+1,m));
      } else {
	if (ylog) yticks = (int)Math.ceil(Math.log(Math.abs(yrange[1]))/Math.log(10.0)) - (int)Math.floor(Math.log(Math.abs(yrange[0]))/Math.log(10.0)) + 1;
	else yticks = (int)Math.floor((yrange[1]-yrange[0])/ystep+1);
      }
      if (yticks < 3) yticks = 3;

      xstep=(float)Math.pow(10, Math.floor(Math.log((maxhist-minhist))/Math.log(10)));

      xticks = (int)Math.floor((maxhist-minhist)/xstep+1);
      if (xticks < 4) {
	xticks*=2;
	xstep/=2;
      }
      if (xticks == 0) xticks = 4;

      xtickv = new float[xticks];
      ytickv = new float[yticks];
      n = s.indexOf("*xtickv");
      if (n != -1) {
	n = s.indexOf("[", n);
	m = s.indexOf(",",n);
	int b = 0;
	while (b < xticks && m > -1 && m < s.indexOf("]", n)) {
	   xtickv[b] = Float.parseFloat(s.substring(n+1,m));
	   b++;
	   n = m; 
	   m = s.indexOf(",", n+1);
	}
	if (b < xticks) {
	   m = s.indexOf("]", n);
	   xtickv[b] = Float.parseFloat(s.substring(n+1, m));
	}
      } else {
	if (minhist % xstep == 0) xtickv[0] = minhist;
        else xtickv[0] = (float)Math.floor(minhist/xstep + 1)*xstep;
        for (int j = 1; j < xticks; j++) xtickv[j] = xtickv[0] + j * xstep;
	for (int j = 0; j < xticks; j++) xtickv[j] = (xtickv[j]-minhist) * xrange[1] / (maxhist-minhist);
      }

      n = s.indexOf("*ytickv");
      if (n != -1) {
        n = s.indexOf("[", n);
        m = s.indexOf(",",n);
        int b = 0;
        while (b < xticks && m > -1 && m < s.indexOf("]", n)) {
           ytickv[b] = Float.parseFloat(s.substring(n+1,m));
           b++;
           n = m;
           m = s.indexOf(",", n+1);
        }
        if (b < yticks) {
           m = s.indexOf("]", n);
           ytickv[b] = Float.parseFloat(s.substring(n+1, m));
        }
      } else {
	if (ylog) {
	   int ay = (int)Math.floor(Math.log(yrange[0])/Math.log(10.0));
	   for (int j = ay; j <= (int)Math.ceil(Math.log(yrange[1])/Math.log(10.0)); j++) ytickv[j-ay] = (float)Math.pow(10.0, j); 
        } else {
           if (yrange[0] % ystep == 0) ytickv[0] = yrange[0];
           else ytickv[0] = (float)Math.floor(yrange[0]/ystep + 1)*ystep;
           for (int j = 1; j < yticks; j++) ytickv[j] = ytickv[0] + j * ystep;
	   if( ytickv[yticks-1] > yrange[1] ) ytickv[yticks-1] = yrange[1];
	}
      }

      n = s.indexOf("*xtickinterval");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xtickinterval = Float.parseFloat(s.substring(n+1,m));
        float xtimin = xtickinterval*(float)Math.floor(xrange[0]/xtickinterval);
        float xtimax = xtickinterval*(float)Math.ceil(xrange[1]/xtickinterval);
        xticks = (int)((xtimax-xtimin)/xtickinterval)+1;
        xtickv = new float[xticks];
        for (int j = 0; j < xticks; j++)
           xtickv[j] = xtimin + j*xtickinterval;
      }

      n = s.indexOf("*ytickinterval");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        ytickinterval = Float.parseFloat(s.substring(n+1,m));
        float ytimin = ytickinterval*(float)Math.floor(yrange[0]/ytickinterval);
        float ytimax = ytickinterval*(float)Math.ceil(yrange[1]/ytickinterval);
        yticks = (int)((ytimax-ytimin)/ytickinterval)+1;
        ytickv = new float[yticks];
        for (int j = 0; j < yticks; j++)
           ytickv[j] = ytimin + j*ytickinterval;
      }

      xtickname = new String[xticks];
      ytickname = new String[yticks];
      n = s.indexOf("*xtickname");
      if (n != -1) {
        n = s.indexOf("[", n);
        m = s.indexOf(",",n);
        int b = 0;
        while (b < xticks && m > -1 && m < s.indexOf("]", n)) {
           xtickname[b] = s.substring(n+1,m);
           b++;
           n = m;
           m = s.indexOf(",", n+1);
        }
        if (b < xticks) {
           m = s.indexOf("]", n);
           xtickname[b] = s.substring(n+1, m);
        } 
      } else {
        for (int j = 0; j < xticks; j++) {
	   if (!xlog) {
	       float htickv = (float)Math.floor(xtickv[j]*(maxhist-minhist)/xrange[1]+minhist+0.5);
	       if (maxhist-minhist < 4)
		   htickv = 0.01f*(int)Math.floor(100*(xtickv[j]*(maxhist-minhist)/xrange[1]+minhist+0.005));
	       else if (maxhist-minhist < 10)
		   htickv = 0.1f*(int)Math.floor(10*(xtickv[j]*(maxhist-minhist)/xrange[1]+minhist+0.05));
	       long rtickv = (long)Math.round( (double)htickv * 10000 );
	       xtickname[j] = String.valueOf( (double)rtickv/10000 );	       
	   } else {
	      xtickname[j] = "" + (xtickv[j]*(maxhist-minhist)/xrange[1]+minhist+0.005);
	      if (xtickname[j].indexOf(".") != -1) xtickname[j] = xtickname[j].substring(0, xtickname[j].indexOf(".")+3);
	   }
	}
      }

      n = s.indexOf("*ytickname");
      if (n != -1) {
        n = s.indexOf("[", n);
        m = s.indexOf(",",n);
        int b = 0;
        while (b < yticks && m > -1 && m < s.indexOf("]", n)) {
           ytickname[b] = s.substring(n+1,m);
           b++;
           n = m;
           m = s.indexOf(",", n+1);
        }
        if (b < yticks) {
           m = s.indexOf("]", n);
           ytickname[b] = s.substring(n+1, m);
        }
      } else if( ylog ) {
	  for (int j = 0; j < yticks; j++) ytickname[j] = String.valueOf( ytickv[j] );
      } else {
        for (int j = 0; j < yticks; j++) {
	    long rtickv = (long)Math.round( (double)ytickv[j] * 10000.0 );
	    ytickname[j] = String.valueOf( (double)rtickv/10000 );
	}
      }

      n = s.indexOf("*xminor");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xminor = (int)Float.parseFloat(s.substring(n+1,m));
      } else {
	if (xlog) xminor = 1;
	else if (xstep % 6 == 0) xminor = 6;
	else if (xstep % 5 == 0) xminor = 5;
	else if (xstep % 4 == 0) xminor = 4;
	else if (xstep % 3 == 0) xminor = 3;
	else if (xstep % 2 == 0) xminor = 2;
	else xminor = 5;
      } 
      n = s.indexOf("*yminor");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        yminor = (int)Float.parseFloat(s.substring(n+1,m));
      } else {
	if (ylog) yminor = 1;
        else if (ystep % 6 == 0) yminor = 6;
        else if (ystep % 5 == 0) yminor = 5;
        else if (ystep % 4 == 0) yminor = 4;
        else if (ystep % 3 == 0) yminor = 3;
        else if (ystep % 2 == 0) yminor = 2;
        else yminor = 5;
      } 

      n = s.indexOf("*xticklen");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
	xticklen = (int)Float.parseFloat(s.substring(n+1,m));
      } else xticklen = 10;

      n = s.indexOf("*yticklen");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        yticklen = (int)Float.parseFloat(s.substring(n+1,m));
      } else yticklen = 10;

      n = s.indexOf("*charsize");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        charSize = (int)Float.parseFloat(s.substring(n+1,m));
      }
      n = s.indexOf("*font");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
	fontName = s.substring(n+1, m);
      }
      n = s.indexOf("*xcharsize");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xcharSize = (int)Float.parseFloat(s.substring(n+1,m));
      } else xcharSize = charSize;
      n = s.indexOf("*xfont");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xfontName = s.substring(n+1, m);
      } else xfontName = fontName;
      n = s.indexOf("*ycharsize");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        ycharSize = (int)Float.parseFloat(s.substring(n+1,m));
      } else ycharSize = charSize;
      n = s.indexOf("*yfont");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        yfontName = s.substring(n+1, m);
      } else yfontName = fontName;
      mainFont = new Font(fontName, 0, charSize);
      xFont = new Font(xfontName, 0, xcharSize);
      yFont = new Font(yfontName, 0, ycharSize);

      n = s.indexOf("*title");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        title = s.substring(n+1, m);
      } else title = "";
      n = s.indexOf("*xtitle");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        xtitle = s.substring(n+1, m);
      } else xtitle = "";
      n = s.indexOf("*ytitle");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
        ytitle = s.substring(n+1, m);
      } else ytitle = "";

      n = s.indexOf("*color");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        int red = (int)Float.parseFloat(s.substring(n+1, m));
        n = s.indexOf(",",m+1);
        int green = (int)Float.parseFloat(s.substring(m+1, n));
	m = s.indexOf(",", n+1);
	if (m < 0) m = s.length();
	int blue = (int)Float.parseFloat(s.substring(n+1, m));
	plotColor = new Color(red, green, blue);
      } else plotColor = new Color(0, 0, 0);

      n = s.indexOf("*background");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        int red = (int)Float.parseFloat(s.substring(n+1, m));
        n = s.indexOf(",",m+1);
        int green = (int)Float.parseFloat(s.substring(m+1, n));
        m = s.indexOf(",", n+1);
        if (m < 0) m = s.length();
        int blue = (int)Float.parseFloat(s.substring(n+1, m));
        backColor = new Color(red, green, blue);
      } else backColor = new Color(255, 255, 255);

      n = s.indexOf("*axescolor");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        int red = (int)Float.parseFloat(s.substring(n+1, m));
        n = s.indexOf(",",m+1);
        int green = (int)Float.parseFloat(s.substring(m+1, n));
        m = s.indexOf(",", n+1);
        if (m < 0) m = s.length();
        int blue = (int)Float.parseFloat(s.substring(n+1, m));
        axesColor = new Color(red, green, blue);
      } else axesColor = new Color(0, 0, 0);

      n = s.indexOf("*nodata");
      if (n != -1) noData = true; else noData = false;

      n = s.indexOf("*psym");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
	psym = (int)Float.parseFloat(s.substring(n+1, m));
      } else psym = 0;

      n = s.indexOf("*symsize");
      if (n != -1) {
        n = s.indexOf("=", n);
        m = s.indexOf(",", n);
        if (m < 0) m = s.length();
	symsize = (int)Float.parseFloat(s.substring(n+1, m));
      } else symsize = 5;

      n = s.indexOf("*xmargin");
      if (n != -1) {
        n = s.indexOf("[",n);
        m = s.indexOf(",",n);
        xpos1 = pxdim1 + (int)Float.parseFloat(s.substring(n+1, m));
        n = s.indexOf("]",m);
        xpos2 = pxdim2 - (int)Float.parseFloat(s.substring(m+1, n));
      }

      n = s.indexOf("*ymargin");
      if (n != -1) {
        n = s.indexOf("[",n);
        m = s.indexOf(",",n);
        ypos1 = pydim1 + (int)Float.parseFloat(s.substring(n+1, m));
        n = s.indexOf("]",m);
        ypos2 = pydim2 - (int)Float.parseFloat(s.substring(m+1, n));
      }

      n = s.indexOf("*position");
      if (n != -1) {
        n = s.indexOf("[",n);
        m = s.indexOf(",",n);
        xpos1 = (int)(pxdim1+(pxdim2-pxdim1)*Float.parseFloat(s.substring(n+1, m)));
        n = s.indexOf(",",m+1);
        ypos1 = (int)(pydim1+(pydim2-pydim1)*Float.parseFloat(s.substring(m+1, n)));
        m = s.indexOf(",", n+1);
	xpos2 = (int)(pxdim1+(pxdim2-pxdim1)*Float.parseFloat(s.substring(n+1, m)));
	n = s.indexOf("]", m+1);
	ypos2 = (int)(pydim1+(pydim2-pydim1)*Float.parseFloat(s.substring(m+1, n)));
      }

      n = s.indexOf("*noendxlabels");
      if (n != -1) endXLabels = false; else endXLabels = true;

      n = s.indexOf("*noendylabels");
      if (n != -1) endYLabels = false; else endYLabels = true;
   }

   public float[] hist(float[] x, String s) {
      doHist = true;
      xmin = UFArrayOps.minValue(x);
      xmax = UFArrayOps.maxValue(x);
      minhist = UFArrayOps.minValue(x);
      maxhist = UFArrayOps.maxValue(x);
      parseHistParams(s);
      setLinLogX(x);
      if (xlog) x = logX; else x = linX;
      if (xlog) x = doXlogHist(x);
      float[] y = createHist(x);
      setLinLogY(y);
      xmin = 1;
      xmax = nbins;
      ymin = 1; 
      ymax = (float)UFArrayOps.maxValue(y);
      minhist = xrange[0];
      maxhist = xrange[1];
      parseMoreHistParams(s); 
      drawHistAxes(y);
      multi[0]++;
      if (multi[0] >= multi[1]*multi[2]) multi[0] = 0;
      if (!noData) this.makeHist(y);
      if (xlog) x = undoXlog(x);
      if (ylog) y = this.undoYlog(y);
      return y;
   }

   public float[] createHist(float[] x) {
      int n;
      float[] y = new float[nbins];
      for (int j = 0; j < x.length; j++) {
	if (x[j] >= xrange[0] && x[j] <= xrange[1]) {
	   n = (int)((x[j]-xrange[0]) / binsize);
	   if (n < 0 || n > nbins) continue;
	   if (n == nbins) n--;
	   y[n]++;
	}
      }
      return y;
   }

   public void drawHistAxes(float[] y) {
      pxdim1 = (int)((xdim/multi[1]) * (multi[0] % multi[1]));
      pxdim2 = (int)((xdim/multi[1]) * (1 + multi[0] % multi[1]));
      pydim1 = (int)((ydim/multi[2]) * (multi[0] / multi[2]));
      pydim2 = (int)((ydim/multi[2]) * (1 + multi[0] / multi[2]));
      xpos1 = pxdim1 + 60;
      xpos2 = pxdim2 - 20;
      ypos1 = pydim1 + 25;
      ypos2 = pydim2 - 47;
      if (multi[0] == 0) this.initPlot();
      offscreenG.setColor(axesColor);
      xscale = (xpos2-xpos1)/(xrange[1]-xrange[0]);
      if (!ylog) yscale = (ypos2-ypos1)/(yrange[1]-yrange[0]);
      //xoff = xpos1-(xscale*xrange[0]);
      if (!ylog) yoff = ypos2+(yscale*yrange[0]);
      xoff = xpos1;
      yoff = ypos2;
      if (ylog) y = this.doYlog(y);
//System.out.println(xrange[0] + " " + xrange[1]);
//System.out.println(yrange[0] + " " + yrange[1]);
      int[] xaxis = {xpos1, xpos1, xpos2, xpos2, xpos1};
      int[] yaxis = {ypos1, ypos2, ypos2, ypos1, ypos1};
      offscreenG.drawPolyline(xaxis, yaxis, 5);
      this.drawTicks();
      offscreenG.setFont(xFont);
      offscreenG.drawString(xtitle, Math.max(xpos1, (int)((xpos2+xpos1)/2)-(int)((xtitle.length()-1)*(0.25*charSize))), ypos2+35);
      offscreenG.setFont(yFont);
      for (int j = 0; j < ytitle.length(); j++) {
        offscreenG.drawString(ytitle.substring(j, j+1), xpos1-54, Math.max(ypos1, (int)((ypos2+ypos1)/2))-(int)((ytitle.length()-1)*(0.25*charSize))+j*12);
      }
      offscreenG.setFont(mainFont);
      offscreenG.drawString(title, Math.max(xpos1, (int)((xpos2+xpos1)/2)-(int)((title.length()-1)*(0.25*charSize))), ypos1-7);
      offscreenG.setColor(plotColor);
      repaint();
   }

   public void makeHist(float[] y) {
      int[] drawx = new int[2*y.length];
      int[] drawy = new int[2*y.length];
      offscreenG.setClip(xpos1, ypos1, xpos2-xpos1, ypos2-ypos1);
      for (int j = 0; j < y.length; j++) {
        drawx[2*j] = (int)Math.floor(xoff+xscale*j+0.5);
	drawx[2*j+1] = (int)Math.floor(xoff+xscale*(j+1)+0.5);
        drawy[2*j] = (int)Math.floor(yoff-(yscale*y[j])+0.5);
	drawy[2*j+1] = (int)Math.floor(yoff-(yscale*y[j])+0.5);
      }
      if (!fillHist) offscreenG.drawPolyline(drawx, drawy, drawx.length);
      else for (int j = 0; j < drawx.length; j+=2) {
	offscreenG.fillRect(drawx[j],drawy[j],(int)Math.round(xscale),(int)Math.round(yoff-drawy[j]));
      } 
      offscreenG.setClip(0, 0, xdim, ydim);
      repaint();
   }

   public float getYmax() {
      return ymax;
   }

   public void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (offscreenImg != null) g.drawImage(offscreenImg,0,0,xdim,ydim,this);
      if( zoomMode ) {
	Color transLucent = new Color(64, 128, 255, 49);
	// Paint a rectangle with a translucent color
	g.setColor( transLucent );
	int xi = Math.min( sxinit, sxfin );
	int yi = Math.min( syinit, syfin );
	int xs = Math.abs( sxfin - sxinit );
	int ys = Math.abs( syfin - syinit );
	g.fillRect( xi, yi, xs, ys );
	// Paint a double rectangular outline
	g.setColor( Color.WHITE );
	g.drawRect( xi, yi, xs, ys );
	g.setColor( Color.BLACK );
	g.drawRect( xi+1, yi+1, xs-2, ys-2 );
      }
   }

   public void resizePlot(int xdim, int ydim) {
      this.xdim = xdim;
      this.ydim = ydim;
      repaint();
   }

   public void addxyFrame(XYFrame xyFrame) {
      this.xyFrame = xyFrame;
   }

   public void removexyFrame() {
      this.xyFrame = null;
   }

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
      if (ufp != null && ufp.getClass().getName().indexOf("GatorPlot") != -1) {
	GatorPlot gp = (GatorPlot)ufp;
	gp.xMinField.setText(""+x1);
	gp.xMaxField.setText(""+x2);
	gp.yMinField.setText(""+y1);
	gp.yMaxField.setText(""+y2);
	gp.plotButton.doClick();
      }
    }

   public void actionPerformed(ActionEvent ev) {
      PageAttributes page = new PageAttributes();
      page.setPrinterResolution(84);
      page.setOrigin(PageAttributes.OriginType.PRINTABLE);
      PrintJob pjob = getToolkit().getPrintJob(new Frame(), "Java Plot", null, page);
      if (pjob != null) {
         Graphics pg = pjob.getGraphics();
         if (pg != null) {
           this.printAll(pg);
           pg.dispose();
         }
         pjob.end();
      }
   }

}
