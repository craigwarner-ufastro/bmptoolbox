package cjec;

/**
 * Title:        Cirrig Java Engineering Console (CJEC)
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Engineering console GUI for MMTPol
 */

import java.awt.*;
import javax.swing.*;

public class cjec {
    
    public static final
	String rcsID = "$Name:  $ $Id: cjec.java,v 1.3 2010/09/01 20:08:24 warner Exp $";

    public static final int MODE_REGULAR = 0;
    public static final int MODE_STATUS = 1;
    public static boolean _isConnected = false;
    public static int cirrigPort = 57016;
    public static int mode = MODE_REGULAR;

    public cjec(String hostname, String isHostname, String[] args) { 
	// Initializing cjec components... 
	CjecFrame cjecFrame = new CjecFrame(hostname, isHostname, args);
	cjecFrame.validate();
	cjecFrame.setVisible(true);
    }
    
    public static void main(String[] args)
    {
        String hostname = "localhost";
	String isHostname = "localhost";
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-host")) {
                hostname = args[++i];
	    } else if (args[i].equals("-bmpport")) {
                cirrigPort = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-status")) {
		mode = MODE_STATUS;
	    }
        }

        new cjec(hostname, isHostname, args);
    }
}

