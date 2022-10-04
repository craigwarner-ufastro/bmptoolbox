package BMPToolbox;
/**
 * Title:        ufbmpPlc.java
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Main program to start MMTPol agent 
 */

import java.util.*;

public class BMPMultiPLCConfig { 
    public int powerRegister, timerRegister, timerFlagRegister, outputRegister, timerStatRegister, timerValRegister, outletNumber; 

    public BMPMultiPLCConfig(int powerRegister, int outletNumber)  {
      this.powerRegister = powerRegister;
      this.timerRegister = powerRegister+1;
      this.timerFlagRegister = powerRegister+3; 
      this.outputRegister = 16704; 
      this.timerStatRegister = 16960;//+outletNumber-1;
      this.timerValRegister = (outletNumber-1)*2; 
      this.outletNumber = outletNumber;
    }
}
