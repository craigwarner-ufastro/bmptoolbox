package BMPToolbox; 
/**
 * Title:        IrrigatorDatabase 
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Class for a database of Irrigators 
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;

import javaUFProtocol.*;
import javaUFLib.*;
import javaMMTLib.*;

public class IrrigatorDatabase { 
    String _mainClass = getClass().getName();
    private LinkedHashMap <String, Irrigator> _irrigators;

    public IrrigatorDatabase() { 
      _irrigators = new LinkedHashMap(10);
    }

    public void addIrrigator(Irrigator irr) {
      String key = irr.getHost()+"::"+irr.getUid();
      _irrigators.put(key, irr);
    }

    public LinkedHashMap <String, Irrigator> getIrrigators() {
      return _irrigators;
    }

    public Irrigator getIrrigator(String key) {
      return _irrigators.get(key);
    }

    public String getIrrigatorType(String key) {
      return _irrigators.get(key).getType();
    }

    public Vector <String> getIrrigatorIPs(String uid) {
      Vector <String> v = new Vector();
      for (Iterator i = _irrigators.keySet().iterator(); i.hasNext(); ) {
	String key = (String)i.next();
	String[] temp = key.split("::");
	Irrigator irr = (Irrigator)_irrigators.get(key);
	if (temp[1].equals(uid)) v.add("Irrigator::"+irr.getType()+"::"+temp[0]);
      } 
      return v;
    }

    public boolean hasIrrigator(String key) {
      return _irrigators.containsKey(key);
    }

    public boolean hasIrrigatorWithUid(String uid) {
      for (Iterator i = _irrigators.keySet().iterator(); i.hasNext(); ) {
        String key = (String)i.next();
        String[] temp = key.split("::");
        if (temp[1].equals(uid)) return true; 
      }
      return false;
    }

    public void removeIrrigator(String key) {
      _irrigators.remove(key);
    }
}
