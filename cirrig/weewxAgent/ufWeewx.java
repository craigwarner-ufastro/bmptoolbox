package Weewx;
/**
 * Title:        ufWeewx.java
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Main program to start Weewx agent 
 */

import java.util.*;

public class ufWeewx {

    public static final String rcsID = "$Name:  $ $Id: ufWeewx.java,v 1.1 2010/04/29 21:19:07 warner Exp $";

    public ufWeewx(String[] args)
    {
	int serverPort = 57006;
	WeewxAgent wxAgent = new WeewxAgent( serverPort, args );

	wxAgent.exec();
    }

    public static void main(String[] args) {
	System.out.println("Creating new Weewx agent...");
	new ufWeewx(args);
    }
}
