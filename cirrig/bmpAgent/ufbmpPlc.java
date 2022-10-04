package BMPToolbox;
/**
 * Title:        ufbmpPlc.java
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Main program to start MMTPol agent 
 */

import java.util.*;

public class ufbmpPlc {

    public static final String rcsID = "$Name:  $ $Id: ufmmtPol.java,v 1.1 2010/04/29 21:19:07 warner Exp $";

    public ufbmpPlc(String[] args)
    {
	int serverPort = 57001;
	BmpPlcAgent bmpPlcAgent = new BmpPlcAgent( serverPort, args );

	bmpPlcAgent.exec();
    }

    public static void main(String[] args) {
	System.out.println("Creating new BmpPlc agent...");
	new ufbmpPlc(args);
    }
}
