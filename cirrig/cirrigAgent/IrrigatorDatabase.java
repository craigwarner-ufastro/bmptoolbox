package CirrigPlc; 
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
    private LinkedHashMap <String, CirrigIrrigator> _irrigators;

    public IrrigatorDatabase() { 
      _irrigators = new LinkedHashMap(10);
    }

    public void addIrrigator(CirrigIrrigator irr) {
      String key = irr.getHost()+"::"+irr.getUid();
      _irrigators.put(key, irr);
    }

    public LinkedHashMap <String, CirrigIrrigator> getIrrigators() {
      return _irrigators;
    }

    public CirrigIrrigator getIrrigator(String key) {
      return _irrigators.get(key);
    }

    public LinkedHashMap <String, CirrigIrrigator> getIrrigatorsWithUid(String uid) {
      LinkedHashMap <String, CirrigIrrigator> irrs = new LinkedHashMap(10);
      for (Iterator i = _irrigators.keySet().iterator(); i.hasNext(); ) {
        String key = (String)i.next();
        String[] temp = key.split("::");
        if (temp[1].equals(uid)) irrs.put(key, (CirrigIrrigator)_irrigators.get(key));
      }
      return irrs;
    }

    public String getIrrigatorType(String key) {
      return _irrigators.get(key).getType();
    }

    public ArrayList <String> getIrrigatorInfo(String uid) {
      //return ArrayList with not only Irrigators but ZoneGroups and ZoneOutlets
      ArrayList <String> list = new ArrayList();
      for (Iterator i = _irrigators.keySet().iterator(); i.hasNext(); ) {
	String key = (String)i.next();
	String[] temp = key.split("::");
	CirrigIrrigator irr = (CirrigIrrigator)_irrigators.get(key);
	if (temp[1].equals(uid)) {
	  list.add("Irrigator::"+irr.getType()+"::"+temp[0]+"::"+irr.getNOutlets()+"::"+irr.getAllOutletNames()+"::"+irr.getNCounters());
	  for (Iterator zi = irr.getZoneGroups().iterator(); zi.hasNext(); ) {
            ZoneGroup zg = (ZoneGroup)zi.next();
	    list.addAll(zg.getGroupInfo());
	  }
	}
      } 
      return list;
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
