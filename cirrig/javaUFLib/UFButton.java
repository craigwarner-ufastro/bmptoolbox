package javaUFLib;

//Title:        UFButton.java
//Copyright:    (c) 2006
//Author:       Frank Varosi and David Rashkin
//Company:      University of Florida
//Description:  Extension of JButton class for simple defaults and options.

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Creates a JButton with defaults and options.
 */

public class UFButton extends JButton implements MouseListener
{
    public static final
	String rcsID = "$Name:  $ $Id: UFButton.java,v 1.23 2019/02/28 08:29:08 varosi Exp $";

    protected Color _colorBackg = Color.BLUE;
    protected Color _colorForeg = Color.BLACK;
    protected String _name;
    protected Color _highLight = new Color(202,202,202);
    protected Color _shadow = new Color(33,33,33);

    protected Color _topColor;
    protected Color _midColor;
    protected Color _botColor;
    protected Color _flatColor;
    protected boolean _useGradient = false;
    protected boolean _pressed = false;
    protected boolean _mouseListenerAdded = false;
//-------------------------------------------------------------------------------
    /**
     * Default Constructor
     */
    public UFButton() { }
//-------------------------------------------------------------------------------
    /**
     * Basic Constructor
     *@param name String: Text to call button
     */
    public UFButton(String name) {
	super(name);
	_name = name;
    }
//-------------------------------------------------------------------------------
    /**
     * Basic Constructor
     *@param name String: Text to call button
     */
    public UFButton(String name, boolean raised) {
	super(name);
	_name = name;
	if( raised ) setBorder(new BevelBorder( BevelBorder.RAISED ));
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param  name        String: Text to call button
     *@param  background  Color:  color object to use for button background
     */
    public UFButton(String name, Color background) {
	super(name);
	_name = name;
	setColorGradient( background );
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param  name        String: Text to call button
     *@param  background  Color:  color object to use for button background
     */
    public UFButton(String name, Color background, boolean raised) {
	super(name);
	_name = name;
	setColorGradient( background );
	if( raised ) setBorder(new BevelBorder( BevelBorder.RAISED, _highLight, _shadow ));
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param  name        String: Text to call button
     *@param  background  Color:  color object to use for button background
     *@param  foreground  Color:  color object to use for button foreground
     */
    public UFButton(String name, Color background, Color foreground) {
	super(name);
	_name = name;
	setColorGradient( background );
	setForeground( foreground );
	_colorForeg = foreground;
    }
//-------------------------------------------------------------------------------
    /**
     * Constructor
     *@param  name        String: Text to call button
     *@param  background  Color:  color object to use for button background
     *@param  foreground  Color:  color object to use for button foreground
     */
    public UFButton(String name, Color background, Color foreground, boolean raised) {
	super(name);
	_name = name;
	setColorGradient( background );
	setForeground( foreground );
	_colorForeg = foreground;
	if( raised ) setBorder(new BevelBorder( BevelBorder.RAISED, _highLight, _shadow ));
    }
//-------------------------------------------------------------------------------

    public void useGradient( boolean use ) { _useGradient = use; }

    public void setBackground(Color bg) {
	if( _useGradient )
	    setColorGradient( bg );
	else
	    super.setBackground(bg);
    }

    public void setForeground(Color color) {
	super.setForeground( color );
	_colorForeg = color;
    }

    public void setText(String text) {
	super.setText(text);
	_name = text;
    }

    public void setColorGradient(Color bcolor) {
	_colorBackg = bcolor;
	if(!_mouseListenerAdded) {
	    addMouseListener(this);
	    _mouseListenerAdded = true;
	}

        if (bcolor == Color.RED) {
            setColorGradient(new Color(255, 144, 144),
			     new Color(244,  66,  66),
			     new Color(225,  55,  68));
        }
	else if (bcolor == Color.GREEN) {
            setColorGradient(new Color(155, 240, 155),
                             new Color( 44, 222,  44),
                             new Color( 55, 211, 68));
        }
	else if (bcolor == Color.BLUE) {
            setColorGradient(new Color(177, 177, 255),
                             new Color(111, 111, 255),
                             new Color(100, 255, 255));
        }
	else if (bcolor == Color.YELLOW) {
            setColorGradient(new Color(255, 255, 111),
                             new Color(233, 233,   0),
                             new Color(225, 225,  66));
        }
	else if (bcolor == Color.ORANGE) {
            setColorGradient(new Color(255, 188, 144),
                             new Color(255, 155,  33),
                             new Color(215, 130,  40));
	}
	else if (bcolor == Color.BLACK) {
	    setColorGradient(new Color( 50, 50, 50),
			     new Color(  0,  0,  0),
			     new Color(128,128,128),
			     new Color( 50, 50, 50));
	}
	else if (bcolor == Color.WHITE) {
            setColorGradient(new Color(244,244,244),
                             new Color(220,220,220),
                             new Color(215,215,215));
	}
	else if (bcolor == Color.LIGHT_GRAY) {
            setColorGradient(new Color(198,198,198),
                             new Color(170,170,170),
                             new Color(160,160,160));
        }
	else if (bcolor == Color.GRAY) {
            setColorGradient(new Color(123,123,123),
                             new Color(100,100,100),
                             new Color( 90, 90, 90));
	}
	else if (bcolor == Color.DARK_GRAY) {
            setColorGradient(new Color(55,55,55),
                             new Color( 0, 0, 0),
                             new Color(50,50,50));
        }
	else if (bcolor == Color.CYAN) {
            setColorGradient(new Color(66,211,211),
                             new Color( 0,255,255),
                             new Color(68,225,225));
        }
	else if (bcolor == Color.MAGENTA) {
            setColorGradient(new Color(218,77,218),
                             new Color(255, 0,255),
                             new Color(225,68,225));
	} else {
	    setColorGradient( bcolor, bcolor, Color.white, bcolor.darker() );
	}
    }

    public void setColorGradient(Color top, Color bottom, Color flat) {
	setColorGradient( top, bottom, Color.white, flat );
    }

    public void setColorGradient(Color topColor, Color bottomColor, Color middleColor, Color flatColor) {
	_useGradient = true;
	_topColor = topColor;
	_midColor = middleColor;	
	_botColor = bottomColor;
	_flatColor = flatColor;
    }

    public void updateColorGradient(Color bcolor) {
        setColorGradient( bcolor );
        repaint();
    }
//-------------------------------------------------------------------------------

    public void mousePressed(MouseEvent me) { _pressed = true; repaint(); }
    public void mouseClicked(MouseEvent me) { }
    public void mouseReleased(MouseEvent me) { _pressed = false; repaint(); }
    public void mouseEntered(MouseEvent me) { _pressed = false; }
    public void mouseExited(MouseEvent me) { _pressed = false; }
//-------------------------------------------------------------------------------

    public synchronized void paintComponent(Graphics g) {

	super.paintComponent(g);
	if( !_useGradient ) return;

	Graphics2D g2d = (Graphics2D)g;
	Color nameColor = _colorForeg;
	int wdi = getWidth();
	int hti = getHeight();
	float wdf = (float)wdi;
	float htf = (float)hti;
      
	if( _pressed ) {

	    g2d.setPaint( _flatColor );
	    g2d.fillRect( 0, 0, wdi, hti );
	}
	else if( !this.isEnabled() ) {
	  
	    g2d.setPaint( _flatColor.brighter() );
	    g2d.fillRect( 0, 0, wdi, hti );
	    nameColor = Color.BLACK;
	}
	else {
	    float mpf = htf * 0.3f;
	    GradientPaint gp1 = new GradientPaint(1.0f,  htf, _botColor, 1.0f, mpf, _midColor);
	    GradientPaint gp2 = new GradientPaint(1.0f, 0.0f, _topColor, 1.0f, mpf, _midColor);
	    Rectangle2D.Float rf1 = new Rectangle2D.Float(0.0f,  mpf, wdf, htf);
	    Rectangle2D.Float rf2 = new Rectangle2D.Float(0.0f, 0.0f, wdf, mpf);
	    g2d.setPaint(gp1);
	    g2d.fill(rf1);
	    g2d.setPaint(gp2);
	    g2d.fill(rf2);
	}

	g2d.setPaint( Color.GRAY );
	g2d.drawRect( 0, 0, --wdi, --hti );
	//g2d.setFont( new Font("default", Font.BOLD, 14));  //default font is wider than normal
	g2d.setFont( g2d.getFont().deriveFont( Font.BOLD )); //so use bold version of font copy
	Rectangle2D r2d = getFont().getStringBounds( _name, g2d.getFontRenderContext() );
	g2d.setPaint( nameColor );
	g2d.drawString( _name,
			(int)( 0.4*(wdf - r2d.getWidth()) ),
			(int)( htf/3 + r2d.getHeight()/2 ) );
    }
}
