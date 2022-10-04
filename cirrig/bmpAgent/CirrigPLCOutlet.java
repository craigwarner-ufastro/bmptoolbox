package BMPToolbox;
/**
 * Title:        ufbmpPlc.java
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Main program to start MMTPol agent 
 */

import java.util.*;

public class CirrigPLCOutlet { 
    public int powerRegister, timerRegister, timerFlagRegister, timerValRegister, outletNumber; 

    public CirrigPLCOutlet(int powerRegister, int outletNumber)  {
      this.powerRegister = powerRegister;
      this.timerRegister = powerRegister+1;
      this.timerFlagRegister = powerRegister+3; 
      this.timerValRegister = (outletNumber-1)*2; 
      this.outletNumber = outletNumber;
    }
}
