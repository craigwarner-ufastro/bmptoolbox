package javaUFLib;

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class UFPlot extends javax.swing.JFrame {
   UFPlotPanel thePlot;
   int xdim, ydim;
   JPopupMenu menu;
   JMenuItem printItem;
   boolean autoSetVisible = true;

   public UFPlot() {
      this(640, 512, "Java Plot");
   }

   public UFPlot(String title) {
      this(640, 512, title);
   }

   public UFPlot(int xdim, int ydim) {
      this(xdim, ydim, "Java Plot");
   }

   public UFPlot(int xdim, int ydim, String title) {
      super(title);
      this.xdim = xdim;
      this.ydim = ydim;
      setSize(xdim, ydim);
      Container content = getContentPane();
      content.setBackground(Color.black);
      content.setLayout(new BorderLayout());
      addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
           e.getWindow().dispose();
        }
      });
      addComponentListener(new ComponentAdapter() {
	public void componentResized(ComponentEvent ev) {
	   UFPlot ufp = (UFPlot)ev.getSource();
	   ufp.thePlot.resizePlot(ufp.getWidth()-4, ufp.getHeight()-26);
        }
      });
      this.thePlot = new UFPlotPanel(xdim, ydim, this);
      content.add(thePlot, BorderLayout.CENTER);
      pack();
   }

   private void setVisibility() { if( autoSetVisible ) this.setVisible(true); }

   public void autoSetVisible(boolean autoSet) { autoSetVisible = autoSet; }

   public void plot(float[] x, float[] y, String s) {
      this.setVisibility();
      thePlot.plot(x, y, s);
   }

   public void plot(float[] y, String s) {
      this.setVisibility();
      float[] x = new float[y.length];
      for (int j = 0; j < x.length; j++) x[j] = j;
      thePlot.plot(x, y, s);
   }

   public void plot(int[] x, String s) {
      float[] x2 = new float[x.length];
      for (int j = 0; j < x.length; j++) x2[j] = (float)x[j];
      this.plot(x2, s);
   }

   public void plot(int[] x, int[] y, String s) {
      float[] x2 = new float[x.length];
      float[] y2 = new float[y.length];
      for (int j = 0; j < x.length; j++) x2[j] = (float)x[j];
      for (int j = 0; j < y.length; j++) y2[j] = (float)y[j];
      this.plot(x2, y2, s);
   }

   public void plot(String file, int xcol, int ycol, String s) {
      this.setVisibility();
      float[] x, y;
      Vector v = new Vector();
      String currLine = " ";
      String[] temp;
      int n;
      try {
	BufferedReader r = new BufferedReader(new FileReader(file));
	while (currLine != null) {
	   currLine = r.readLine();
	   if (currLine != null) v.add(currLine);
	}
      } catch(IOException e) { 
	System.out.println("Error Reading From File.");
      }
      n = v.size();
      if (n == 0) {
	System.out.println("File is Empty");
	return;
      }
      x = new float[n];
      y = new float[n];
      for (int j = 0; j < n; j++) {
	currLine = (String)v.remove(0);
	currLine = currLine.replaceAll("\t", " ");
	while (currLine.indexOf("  ") != -1)
	   currLine=currLine.replaceAll("  ", " ");
	temp = currLine.split(" ");
	if (xcol != -1) x[j] = Float.parseFloat(temp[xcol]);
	else x[j] = j;
	y[j] = Float.parseFloat(temp[ycol]);
      }
      thePlot.plot(x, y, s);
   }

   public void plot(String file, int ycol, String s) {
      this.plot(file, -1, ycol, s);
   }

   public void usersym(int[] usymxs, int[] usymys) {
      thePlot.usersym(usymxs, usymys);
   }

   public void overplot(float[] x, float[] y, String s) {
      thePlot.overplot(x, y, s);
   }

   public void xyouts(float xc, float yc, String text, String s) {
      thePlot.xyouts(xc, yc, text, s);
   }

   public void multi(int curr, int col, int row) {
      thePlot.multi(curr, col, row);
   }

   public float[] hist(float[] x, String s) {
      this.setVisibility();
      return thePlot.hist(x, s);
   }

   public float[] hist(float[][] x, String s) {
      this.setVisibility();
      float[] y = new float[x.length*x[0].length];
      for (int j = 0; j < x.length; j++) {
	for (int l = 0; l < x[0].length; l++) y[j*x[0].length+l] = x[j][l];
      }
      return thePlot.hist(y, s);
   }

   public float[] hist(int[] x, String s) {
      this.setVisibility();
      float[] y = new float[x.length];
      for (int j = 0; j < x.length; j++) y[j] = (float)x[j];
      return thePlot.hist(y, s);
   }

   public float[] hist(int[][] x, String s) {
      this.setVisibility();
      float[] y = new float[x.length*x[0].length];
      for (int j = 0; j < x.length; j++) {
        for (int l = 0; l < x[0].length; l++)
	   y[j*x[0].length+l] = (float)x[j][l];
      }
      return thePlot.hist(y, s);
   }

   public float getYmax() {
      return thePlot.getYmax();
   }

   public static void main(String[] args) {
      float[] x = {0, 1, 2, 3, 4, 5};
      float[] y = {0, 3, 7, 9, 6, 7};
      int[] ux = {0, 3, 0, -3, 0};
      int[] uy = {8, 0, -8, 0, 8};
      UFPlot p = new UFPlot();
      p.usersym(ux,uy);
      float[] logy = {3, 19, 29, 89, 15, 161};
      float[] logy2 = {8, 27, 25, 95, 25, 125};
      //p.plot(x, logy, "*ylog");
      //p.overplot(x, logy2, "*psym=4");
      p.multi(0, 2, 2);
      p.plot(x, y, "*xrange=[-1,7], *yrange=[0, 12], *xticks=4, *xtickv=[0, 2, 4, 6], *title=A Plot, *xtitle=Wavelength, *ytitle=Flux, *psym=-14, *xminor=4, *ytickinterval= 4");
      p.overplot(y, x, "*color=0,255,0, *psym=-5, *symsize=10");
      p.xyouts(3, 9, "test", "*charsize=20, *color=0,255,255, *data");
      //UFPlot r = new UFPlot();
      p.plot("data.txt", 0, 2, "*psym= -4, *xmargin=[ 15, 12]");
p.hist(y, "*nbins= 3, *xticks=4, *fill");
   }

} 
