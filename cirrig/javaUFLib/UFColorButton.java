package javaUFLib;

//Title:        UFColorButton.java
//Copyright:    (c) 2006
//Author:       Craig Warner and David Rashkin
//Company:      University of Florida
//Description:  Extension of JButton class for extra colors 

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

public class UFColorButton extends JButton {
    public static final
        String rcsID = "$Name:  $ $Id: UFColorButton.java,v 1.1 2010/03/10 21:33:40 warner Exp $";

    public static final int COLOR_SCHEME_BLUE = 0;
    public static final int COLOR_SCHEME_RED = 1;
    public static final int COLOR_SCHEME_GREEN = 2;
    public static final int COLOR_SCHEME_YELLOW = 3;
    public static final int COLOR_SCHEME_BLACK = 4;
    public static final int COLOR_SCHEME_ORANGE = 5;
    public static final int COLOR_SCHEME_WHITE = 6;
    public static final int COLOR_SCHEME_LIGHT_GRAY = 7;
    public static final int COLOR_SCHEME_GRAY = 8;
    public static final int COLOR_SCHEME_DARK_GRAY = 9;
    public static final int COLOR_SCHEME_CYAN = 10;
    public static final int COLOR_SCHEME_MAGENTA = 11;
    public static final int COLOR_SCHEME_ROYAL = 12;
    public static final int COLOR_SCHEME_AQUA = 13;

    protected Color gradientColor1;
    protected Color gradientColor2;
    protected Color gradientColor3;
    protected Color flatColor;
    protected boolean useGradient;

    GradientPaint gp1, gp2;
    Rectangle2D.Float rf1, rf2;
    Rectangle2D r2d;

    public Color foregroundColor = Color.BLACK;

    boolean pressed = false;

    public UFColorButton(String title) {this(title,0);}
    public UFColorButton(String title, int colorScheme) {
	super(title);
	setColorGradient(colorScheme);
	pressed = false;
	addMouseListener(new MouseListener() {
		public void mousePressed(MouseEvent me) {pressed = true; repaint();}
		public void mouseExited(MouseEvent me) {}
		public void mouseEntered(MouseEvent me) {}
		public void mouseClicked(MouseEvent me) {}
		public void mouseReleased(MouseEvent me) {pressed = false; repaint();}
	    });
	
    }

    public void setBackground(Color bg) {
	super.setBackground(bg);
	useGradient = false;
    }

    public void setColorGradient(int colorScheme) {
	useGradient = true;
        if (colorScheme == COLOR_SCHEME_RED) {
            setColorGradient(new Color(238, 69, 84),
                             new Color(255, 81, 97),
                             new Color(225, 55, 68));
        } else if (colorScheme == COLOR_SCHEME_GREEN) {
            setColorGradient(new Color(69, 238, 84),
                             new Color(20, 255, 30),
                             new Color(55, 225, 68));
        } else if (colorScheme == COLOR_SCHEME_BLUE) {
            setBackground(new JButton().getBackground());
        } else if (colorScheme == COLOR_SCHEME_YELLOW) {
            setColorGradient(new Color(238, 238, 69),
                             new Color(255, 255, 0),
                             new Color(225, 225, 68));
        } else if (colorScheme == COLOR_SCHEME_ORANGE) {
            setColorGradient(new Color(238, 180, 119),
                             new Color(255, 145, 40),
                             new Color(215, 130, 40));
	} else if (colorScheme == COLOR_SCHEME_BLACK) {
	    setColorGradient(new Color(0,0,0),
			     new Color(50,50,50),
			     new Color(128,128,128),
			     new Color(50,50,50));
	} else if (colorScheme == COLOR_SCHEME_WHITE) {
            setColorGradient(new Color(220,220,220),
                             new Color(240,240,240),
                             new Color(215,215,215));
	} else if (colorScheme == COLOR_SCHEME_LIGHT_GRAY) {
            setColorGradient(new Color(170,170,170),
                             new Color(190,190,190),
                             new Color(160,160,160));
        } else if (colorScheme == COLOR_SCHEME_GRAY) {
            setColorGradient(new Color(100,100,100),
                             new Color(120,120,120),
                             new Color(90,90,90));
	} else if (colorScheme == COLOR_SCHEME_DARK_GRAY) {
            setColorGradient(new Color(50,50,50),
                             new Color(0,0,0),
                             new Color(50,50,50));
        } else if (colorScheme == COLOR_SCHEME_CYAN) {
            setColorGradient(new Color(69,218,218),
                             new Color(0,255,255),
                             new Color(68,225,225));
        } else if (colorScheme == COLOR_SCHEME_MAGENTA) {
            setColorGradient(new Color(218,69,218),
                             new Color(255,0,255),
                             new Color(225,68,225));
        } else if (colorScheme == COLOR_SCHEME_ROYAL) {
            setColorGradient(new Color(39, 54, 208),
                             new Color(0, 10, 225),
			     new Color(200, 200, 255), 
                             new Color(25, 38, 195));
        } else if (colorScheme == COLOR_SCHEME_AQUA) {
            setColorGradient(new Color(100, 100, 255),
                             new Color(100, 255, 100),
                             new Color(100, 255, 255),
                             new Color(100, 255, 255));
	}

    }

    public void setColorGradient(Color grad1, Color grad2, Color flat) {
	setColorGradient(grad1, grad2, Color.white, flat);
    }

    public void setColorGradient(Color topColor, Color bottomColor, Color middleColor, Color flatColor) {
	useGradient = true;
	gradientColor1 = topColor;
	gradientColor2 = bottomColor;
	gradientColor3 = middleColor;	
	this.flatColor = flatColor;
    }

    public void updateColorGradient(int colorScheme) {
        setColorGradient(colorScheme);
        repaint();
    }

    public void paintComponent(Graphics g){
      super.paintComponent(g);
      if (!useGradient) return;
      String text = getText();
      Graphics2D g2 = (Graphics2D)g;
      
      if (!isEnabled()){
	  
	  g2.setPaint(getParent().getBackground());
	  g2.fillRect(0, 0, getWidth(), getHeight());
	  
	  g2.setPaint(Color.gray);
	  
      } else if ( ! pressed){
        gp1 = new GradientPaint(1.0f, (float)getHeight(),gradientColor1, 1.0f,
				(float)getHeight() * 0.3f, gradientColor3);
        gp2 = new GradientPaint(1.0f, 0.0f, gradientColor2,
				1.0f, (float)getHeight() * 0.3f, gradientColor3);
	//gp2 = new GradientPaint(1.0f, 0.0f, new Color(255, 181, 197),
        rf1 = new Rectangle2D.Float(0.0f, (float)getHeight() * 0.3f,
            (float)getWidth(), (float)getHeight());
        rf2 = new Rectangle2D.Float(0.0f, 0.0f, (float)getWidth(),
            (float)getHeight() * 0.3f);
        g2.setPaint(gp1);
        g2.fill(rf1);
        g2.setPaint(gp2);
        g2.fill(rf2);

        g2.setPaint(Color.black);

      }
      else{
        g2.setPaint(flatColor);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setPaint(Color.black);

      } // else{
          g2.setPaint(foregroundColor);
	  r2d = getFont().getStringBounds(text, g2.getFontRenderContext());
	  
	  if (text.toLowerCase().startsWith("<html>")) {
	      text = text.substring(6);
	      if (text.toLowerCase().endsWith("</html>")) 
		  text = text.substring(0,text.toLowerCase().lastIndexOf("</html>"));
	      String [] strs = text.split("<br>");
	      for (int i=0; i<strs.length; i++) {
		  r2d = getFont().getStringBounds(strs[i], g2.getFontRenderContext());		  
		  g2.drawString(strs[i], (int)((getWidth() - r2d.getWidth()) / 2),
				(int)(getHeight()/2 + r2d.getHeight()/2*strs.length - (r2d.getHeight())*(strs.length-1-i) ));
	      }
	  } else {
	      g2.drawString
		  (text, (int)((getWidth() - r2d.getWidth()) / 2),
		   (int)(getHeight()/2 +  r2d.getHeight()/2));	  
	  }
    } // paintComponent()

    public void setForeground(Color color) {
	this.foregroundColor = color;
    }

    public static void main(String [] args) {
	JFrame j = new JFrame("UFColorButton Test");
	j.setDefaultCloseOperation(3);
	j.setSize(400,400);
	JPanel pan = new JPanel();
	final UFColorButton ufcb = new UFColorButton("<html>Test<br>testhtml</html>",COLOR_SCHEME_YELLOW);
	
	ufcb.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		    ufcb.setColorGradient(COLOR_SCHEME_BLACK);
		    ufcb.repaint();
		}
	    });
	pan.add(ufcb);
	j.getContentPane().add(pan);
	j.setVisible(true);
    }

}
