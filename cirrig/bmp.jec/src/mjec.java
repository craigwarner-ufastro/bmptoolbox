package bmpjec;

/**
 * Title:        MMTPol Java Engineering Console (MJEC)
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Engineering console GUI for MMTPol
 */

import java.awt.*;
import javax.swing.*;

public class mjec {
    
    public static final
	String rcsID = "$Name:  $ $Id: mjec.java,v 1.3 2010/09/01 20:08:24 warner Exp $";

    public static final int MODE_REGULAR = 0;
    public static final int MODE_STATUS = 1;
    public static boolean _isConnected = false;
    public static int bmpPort = 57001;
    public static int mode = MODE_REGULAR;

    public mjec(String hostname, String isHostname, String[] args) { 
	// Initializing mjec components... 
	MjecFrame mjecFrame = new MjecFrame(hostname, isHostname, args);
	mjecFrame.validate();
	mjecFrame.setVisible(true);
    }
    
    public static void main(String[] args)
    {
        String hostname = "localhost";
	String isHostname = "localhost";
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-host")) {
                hostname = args[++i];
	    } else if (args[i].equals("-bmpport")) {
                bmpPort = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-status")) {
		mode = MODE_STATUS;
	    }
        }

        new mjec(hostname, isHostname, args);
    }
}

