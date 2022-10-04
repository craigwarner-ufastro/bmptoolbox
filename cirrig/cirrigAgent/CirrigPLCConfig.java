package CirrigPlc;
/**
 * Title:        ufbmpPlc.java
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Main program to start MMTPol agent 
 */

import java.util.*;

public class CirrigPLCConfig { 
    private int nOutlets, stride, firstPowerRegister, nCounters; 
    private Vector <CirrigPLCOutlet> outlets;

    public CirrigPLCConfig(int nOutlets, int firstPowerRegister, int stride, int nCounters)  {
      this.nOutlets = nOutlets;
      this.stride = stride;
      this.firstPowerRegister = firstPowerRegister;
      this.nCounters = nCounters;
      outlets = new Vector(nOutlets);
      int powerRegister;
      for (int j = 0; j < nOutlets; j++) {
	powerRegister = firstPowerRegister+j*stride;
	outlets.add(new CirrigPLCOutlet(powerRegister, (j+1)));
      }
    }

    public CirrigPLCConfig(int nOutlets, int firstPowerRegister, int stride)  {
      this(nOutlets, firstPowerRegister, stride, 0);
    }

    public CirrigPLCOutlet[] getOutlets() {
      return outlets.toArray(new CirrigPLCOutlet[outlets.size()]);
    }

    public int getNOutlets() { return nOutlets; }

    public int getFirstRegister() { return firstPowerRegister; }

    public int getStride() { return stride; }

    public int getNCounters() { return nCounters; }

    public String toString() {
      return "nOutlets = "+nOutlets+"; firstPowerRegister = "+outlets.elementAt(0).powerRegister+"; stride = "+stride+"; nCounters = "+nCounters;
    }
}
