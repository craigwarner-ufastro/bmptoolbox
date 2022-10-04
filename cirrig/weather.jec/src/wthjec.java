package wthjec;

/**
 * Title:        Weather Java Engineering Console (wthjec)
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2012
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  GUI and client for weather data 
 */

import java.awt.*;
import javax.swing.*;

public class wthjec {
    
    public static final
	String rcsID = "$Name:  $ $Id: wthjec.java,v 1.3 2010/09/01 20:08:24 warner Exp $";

    public static final int MODE_REGULAR = 0;
    public static final int MODE_STATUS = 1;
    public static boolean _isConnected = false;
    public static int wthPort = 57003;
    public static int mode = MODE_REGULAR;

    public wthjec(String hostname, String isHostname, String[] args) { 
	// Initializing wthjec components... 
	WthjecFrame wthjecFrame = new WthjecFrame(hostname, isHostname, args);
	wthjecFrame.validate();
	wthjecFrame.setVisible(true);
    }
    
    public static void main(String[] args)
    {
        String hostname = "localhost";
	String isHostname = "localhost";
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-host")) {
                hostname = args[++i];
	    } else if (args[i].equals("-wthport")) {
                wthPort = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-status")) {
		mode = MODE_STATUS;
	    }
        }

        new wthjec(hostname, isHostname, args);
    }
}

