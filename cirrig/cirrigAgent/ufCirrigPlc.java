package CirrigPlc; 
/**
 * Title:        ufCirrigPlc.java
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Main program to start MMTPol agent 
 */

import java.util.*;

public class ufCirrigPlc {

    public static final String rcsID = "$Name:  $ $Id: ufmmtPol.java,v 1.1 2010/04/29 21:19:07 warner Exp $";

    public ufCirrigPlc(String[] args)
    {
	int serverPort = 57016;
	CirrigPlcAgent cirrigPlcAgent = new CirrigPlcAgent( serverPort, args );

	cirrigPlcAgent.exec();
    }

    public static void main(String[] args) {
	System.out.println("Creating new CirrigPlc agent...");
	new ufCirrigPlc(args);
    }
}
