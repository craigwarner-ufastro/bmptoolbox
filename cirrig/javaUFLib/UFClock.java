package javaUFLib;

// An analog clock -- Using Timer and Calendar.
// Uses a BufferedImage and antialiasing.
// The second hand creep is from the Timer, which seems to take
// a little longer than the requested time to call the listener.
//  -- Fred Swartz, 1 May 1999, 2002-02-21 from applet to application
//Title:        UFClock.java
//Copyright:    (c) 2006
//Author:       Craig Warner and David Hon
//Company:      University of Florida
//Description:  Extension of Clock class by Fred Swartz 

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class UFClock extends JPanel {
    public static final
        String rcsID = "$Name:  $ $Id: UFClock.java,v 1.1 2010/03/10 21:33:28 warner Exp $";

    private float seconds = 0;
    private int delay = 0;

    private boolean showClock = false;

    private static final int   spacing = 10;
    private static final float twoPi = (float)(2.0 * Math.PI);
    private static final float threePi = (float)(3.0 * Math.PI);
    // Angles for the trigonometric functions are measured in radians.
    // The following in the number of radians per sec or min.
    private static final float radPerSecMin = (float)(Math.PI / 30.0);
    private static final float degPerSecMin = (float)(radPerSecMin*180/Math.PI);

    private int size;       // height and width of clock face
    private int centerX;    // x coord of middle of clock
    private int centerY;    // y coord of middle of clock
    private BufferedImage clockImage;
    private javax.swing.Timer t;

    //==================================================== Clock constructor
    public UFClock() {
        this.setPreferredSize(new Dimension(300,300));
        this.setBackground(Color.white);
        this.setForeground(Color.black);
    }//end constructor

    public UFClock(int w, int h) {
	this.setPreferredSize(new Dimension(w,h));
        this.setBackground(Color.white);
        this.setForeground(Color.black);
    }

    public void setClock(int sec) {
	seconds = sec;
	showClock = true;
	delay = (int)(seconds*1000/60.0);
	seconds+= delay/1000.0f;
        t = new javax.swing.Timer(delay,
              new ActionListener() {
                  public void actionPerformed(ActionEvent e) {
                      update();
                  }
              });
    }

    public void resetClock() {
	if (t != null) stop();
        showClock = false;
        repaint();
    }

    //=============================================================== update
        // Replace the default update so that the plain background
        // doesn't get drawn.
    public void update() {
        // Hmmm, is this the right way to do this? Or do we just
        // call paintComponent?  Does this coalesce calls, not that
        // this updates often enuf for that to be important.
        this.repaint();
    }//end update

    //================================================================ start
    public void start() {
        t.start();  // start the timer
    }//end start

    //================================================================= stop
    public void stop() {
        t.stop();  // start the timer
    }//end stop

    //======================================================= paintComponent

    public void paintComponent(Graphics g) {
        super.paintComponent(g);  // paint background, borders
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // The panel may have been resized, get current dimensions
        int w = getWidth();
        int h = getHeight();
        size = ((w<h) ? w : h) - 2*spacing;
        centerX = size/2 + spacing;
        centerY = size/2 + spacing;

        // Create the clock face background image if this is the first time,
        // or if the size of the panel has changed
        if (clockImage == null
                || clockImage.getWidth() != w
                || clockImage.getHeight() != h) {

            clockImage = (BufferedImage)(this.createImage(w, h));
            // now get a graphics context from this image
            Graphics2D gc = clockImage.createGraphics();
            gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            drawClockFace(gc);
        }

	seconds-=delay/1000.0f;

        // Draw the clock face from the precomputed image
        g2.drawImage(clockImage, null, 0, 0);

        if (! showClock) return;

        // Draw the clock hands
        drawClockHands(g);
	if (seconds < 0) resetClock();
    }//end paintComponent


    //======================================================= drawClockHands
    private void drawClockHands(Graphics g) {
        int secondRadius = size/2;
        int minuteRadius = secondRadius * 3/4;
        int hourRadius   = secondRadius/2;

        String strTime = ""+(int)(seconds/60);
        if (strTime.length() == 1) strTime = "0"+strTime;
        String strSec = ""+((int)(seconds+0.5) % 60);
        if (strSec.length() == 1) strSec = "0"+strSec;
        strTime += ":"+strSec;
	g.setFont(new Font("Dialog", Font.BOLD, 16));
	if (seconds < 0) strTime = "00:00";
        g.drawString(strTime, centerX-23, centerY+65);

        // second hand
        float fseconds = 60-seconds*1000.0f/delay;
	int secondAngle = (int)(-0.5-degPerSecMin*fseconds);
	float radSecondAngle = (float)(secondAngle*Math.PI/180); 
        drawRadius(g, centerX, centerY, radSecondAngle+Math.PI, 0, secondRadius);
	Color transLucent = new Color(0,0,255,160);
	g.setColor(transLucent);
	g.fillArc(centerX-secondRadius, centerY-secondRadius, 2*secondRadius, 2*secondRadius, 90, secondAngle);

    }//end drawClockHands

    //======================================================== drawClockFace
    private void drawClockFace(Graphics g) {
        // clock face
        g.setColor(Color.WHITE);
        g.fillOval(spacing, spacing, size, size);
        g.setColor(Color.black);
        g.drawOval(spacing, spacing, size, size);

        // tic marks
        for (int sec = 0; sec<60; sec++) {
            int ticStart;
            if (sec%5 == 0) {
                ticStart = size/2-10;
            } else {
                ticStart = size/2-5;
            }
            drawRadius(g, centerX, centerY, radPerSecMin*sec, ticStart , size/2);
        }
        g.drawString("Estimated Time Remaining", centerX-70, centerY+40);
    }//endmethod drawClockFace

    //=========================================================== drawRadius
    private void drawRadius(Graphics g, int x, int y, double angle,
                    int minRadius, int maxRadius) {
        float sine   = (float)Math.sin(angle);
        float cosine = (float)Math.cos(angle);

        int dxmin = (int)(minRadius * sine);
        int dymin = (int)(minRadius * cosine);

        int dxmax = (int)(maxRadius * sine);
        int dymax = (int)(maxRadius * cosine);
        g.drawLine(x+dxmin, y+dymin, x+dxmax, y+dymax);
    }//end drawRadius

}//endclass Clock
