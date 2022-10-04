package javaUFLib;

//Title:        UFColor
//Version:      (see rcsID)
//Copyright:    Copyright (c) 2006
//Author:       Frank Varosi
//Company:      University of Florida
//Description:  Extends class Color by adding static member defaultColor that can be changed.

import java.awt.*;

public class UFColor extends Color {

    protected static Color dark_Color =    new Color(211,211,211);
    protected static Color default_Color = new Color(222,222,222);
    protected static Color panel_Color =   new Color(233,233,233);

    protected static Color _greenWhite = new Color(237,244,237);
    protected static Color _redWhite =   new Color(255,249,249);
    protected static Color _redDark =    new Color(240,230,220);

    public UFColor(int r, int g, int b) { super(r,g,b); }

    public UFColor(int r, int g, int b, int a) { super(r,g,b,a); }

    public UFColor(float r, float g, float b) { super(r,g,b); }

    public UFColor(float r, float g, float b, float a) { super(r,g,b,a); }

    public static Color translateColor( String name ) {
	if( name.toLowerCase().equals("red") ) return Color.red;
	if( name.toLowerCase().equals("orange") ) return Color.orange;
	if( name.toLowerCase().equals("yellow") ) return Color.yellow;
	if( name.toLowerCase().equals("green") ) return Color.green;
	if( name.toLowerCase().equals("cyan") ) return Color.cyan;
	if( name.toLowerCase().equals("blue") ) return Color.blue;
	if( name.toLowerCase().equals("magenta") ) return Color.magenta;
	if( name.toLowerCase().equals("pink") ) return Color.pink;
	if( name.toLowerCase().equals("black") ) return Color.black;
	if( name.toLowerCase().equals("gray") ) return Color.gray;
	if( name.toLowerCase().equals("grey") ) return Color.gray;
	else return null;
    }

    public static void setDefaultColor( Color color ) { default_Color = color; }
    public static void setPanelColor( Color color ) { panel_Color = color; }
    public static void setDarkColor( Color color ) { dark_Color = color; }

    public static Color getDefaultColor() { return default_Color; }
    public static Color getPanelColor() { return panel_Color; }
    public static Color getDarkColor() { return dark_Color; }

    public static Color defaultColor() { return default_Color; }
    public static Color panelColor() { return panel_Color; }
    public static Color darkColor() { return dark_Color; }

    public static Color greenWhite() { return _greenWhite; }
    public static Color redWhite() { return _redWhite; }
    public static Color redDark() { return _redDark; }
}
