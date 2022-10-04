package CCROP;
/**
 * Title:        ufCCrop.java
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Main program to start MMTPol agent 
 */

import java.util.*;

public class ufCCrop {

    public static final String rcsID = "$Name:  $ $Id: ufmmtPol.java,v 1.1 2010/04/29 21:19:07 warner Exp $";

    public ufCCrop(String[] args)
    {
	int serverPort = 57002;
	CCropAgent ccropAgent = new CCropAgent( serverPort, args );

	ccropAgent.exec();
    }

    public static void main(String[] args) {
	System.out.println("Creating new CCrop agent...");
	new ufCCrop(args);
    }
}
