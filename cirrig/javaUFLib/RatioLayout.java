package javaUFLib;

import java.awt.*;
import java.util.*;

/** RatioLayout.java -- Layout manager for Java containers
 * 
 *  This layout manager allows you to specify ratios of x,y,width,height
 *  characteristics of the components.  For example,
 *
 *      setLayout(new RatioLayout());
 *      add("0,0;.45,.5", new Button("OK")); // upper left corner, 45% x 50%
 *      add(".75,.75", new Button("QUIT"));  // 75%,75% and use preferred size
 *
 * @author Terence Parr
 * <a href=http://www.MageLang.com>MageLang Institute</a>
 */
public class RatioLayout implements LayoutManager {

    // track the ratios for each object of form "xratio,yratio;wratio,hratio"
    Vector ratios = new Vector(50);
    // track the components also so we can remove associated modifier
    // if necessary.
    Vector components = new Vector(50);

    public void addLayoutComponent(String r, Component comp) {
        ratios.addElement(r);
        components.addElement(comp);
    }

    public void removeLayoutComponent(Component comp) {
        int i = components.indexOf(comp);
        if ( i!=-1 ) {
            ratios.removeElementAt(i);
            components.removeElementAt(i);
        }
    }

    public Dimension preferredLayoutSize(Container target) {
        return target.getSize();
    }

    public Dimension minimumLayoutSize(Container target) {
        return target.getSize();
    }
    
    public String getComponentRatio(Component comp) {
    	int i = components.indexOf(comp);
    	if( i != -1 ) 
    		return (String)ratios.elementAt(i);
    	return null;    	
    }

    public void layoutContainer(Container target) {
        Insets insets = target.getInsets();
        int ncomponents = target.getComponentCount();
        Dimension d = target.getSize();
        d.width -=  insets.left+insets.right;
        d.height -= insets.top+insets.bottom;
        for (int i = 0 ; i < ncomponents ; i++) {
            Component comp = target.getComponent(i);
            StringTokenizer st =
                new StringTokenizer((String)ratios.elementAt(i), ", \t;");
            float rx = Float.valueOf(st.nextToken()).floatValue();
            float ry = Float.valueOf(st.nextToken()).floatValue();
            float rw=0;
            float rh=0;
            int w,h;
            if ( st.hasMoreElements() ) {// get width, height if they exist
                rw = Float.valueOf(st.nextToken()).floatValue();
                rh = Float.valueOf(st.nextToken()).floatValue();
                w = (int)(d.width*rw);
                h = (int)(d.height*rh);
            }
            else {
                Dimension compDim = comp.getPreferredSize();
                w = compDim.width;
                h = compDim.height;
            }
            int x = (int)(d.width*rx);
            int y = (int)(d.height*ry);
            comp.setBounds(x+insets.left,y+insets.top,w,h);
        }
    }
    
    public String toString() {
        return getClass().getName();
    }
}
