package BMPToolbox;
/**
 * Title:        ufbmpPlc.java
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Main program to start MMTPol agent 
 */

import java.util.*;

public class CirrigPLCConfig { 
    private int nOutlets; 
    private Vector <CirrigPLCOutlet> outlets;
    public int powerRegister, timerRegister, timerFlagRegister, outputRegister, timerStatRegister, timerValRegister, outletNumber; 

    public CirrigPLCConfig(int nOutlets, int firstPowerRegister, int stride)  {
      this.nOutlets = nOutlets;
      outlets = new Vector(nOutlets);
      int powerRegister;
      for (int j = 0; j < nOutlets; j++) {
	powerRegister = firstPowerRegister+j*stride;
	outlets.add(new CirrigPLCOutlet(powerRegister, (j+1)));
      }
    }

    public CirrigPLCOutlet[] getOutlets() {
      return outlets.toArray(new CirrigPLCOutlet[outlets.size()]);
    }
}
