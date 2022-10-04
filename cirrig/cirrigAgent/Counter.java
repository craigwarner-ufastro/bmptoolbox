package CirrigPlc;
/**
 * Title:        Counter
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Class representing a Counter 
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;

import javaUFProtocol.*;
import javaUFLib.*;
import javaMMTLib.*;

public class Counter {
    String _mainClass = getClass().getName();
    protected int counterNum, uid;

    public Counter(int num) { 
      this.counterNum = num;
    }

    public int getCounterNumber() { return counterNum; }
}
