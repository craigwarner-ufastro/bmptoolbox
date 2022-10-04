package javaMMTLib;
/**
 * Title:        UFMMTMotor.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  MMT Motor class 
 */

import java.io.*;
import java.lang.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class UFMMTMotor {
  protected String motorName, shortName, fitsKey, letter;
  protected int totalSteps, numPos;
  protected String[] posNames;
  protected int[] posSteps;

  public UFMMTMotor(Node fstNode) {
    motorName = "null";
    totalSteps = 0;
    numPos = 0;
    NodeList nlist;
    Element elem;
    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
      Element fstElmnt = (Element) fstNode;
      if (fstElmnt.hasAttribute("name")) {
	motorName = fstElmnt.getAttribute("name").trim();
      }
      if (fstElmnt.hasAttribute("steps")) {
	try {
	  totalSteps = Integer.parseInt(fstElmnt.getAttribute("steps").trim());
        } catch(NumberFormatException nfe) { totalSteps = 0; }
      }
      if (fstElmnt.hasAttribute("short")) {
	shortName = fstElmnt.getAttribute("short").trim();
      } else shortName = motorName;
      if (shortName.indexOf(" ") != -1) shortName = shortName.substring(0,shortName.indexOf(" "));
      if (fstElmnt.hasAttribute("fitskey")) {
	fitsKey = fstElmnt.getAttribute("fitskey").trim();
      } else fitsKey = "";
      if (fstElmnt.hasAttribute("letter")) {
	letter = fstElmnt.getAttribute("letter").trim();
      } else letter = "";
      nlist = fstElmnt.getElementsByTagName("position");
      numPos = nlist.getLength();
      posNames = new String[numPos];
      posSteps = new int[numPos];
      for (int j = 0; j < numPos; j++) {
	Node posNode = nlist.item(j);
        if (posNode.getNodeType() == Node.ELEMENT_NODE) {
          Element posElmnt = (Element)posNode;
          NodeList posnameList = posElmnt.getElementsByTagName("posname");
          elem = (Element)posnameList.item(0);
	  posNames[j] = elem.getFirstChild().getNodeValue().trim();
          NodeList posstepsList = posElmnt.getElementsByTagName("possteps");
          elem = (Element)posstepsList.item(0);
	  try {
	    posSteps[j] = Integer.parseInt(elem.getFirstChild().getNodeValue().trim());
	  } catch(NumberFormatException nfe) { posSteps[j] = 0; }
	}
      }
    }
  }
 
  public static UFMMTMotor[] getMotorsFromXML(String filename) {
    UFMMTMotor[] motors = null;
    try {
      File file = new File(filename);
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(file);
      doc.getDocumentElement().normalize();
      Element root = doc.getDocumentElement();
      NodeList nlist;
      Element elem;
      nlist = root.getElementsByTagName("motor");
      motors = new UFMMTMotor[nlist.getLength()];
      for (int j = 0; j < nlist.getLength(); j++) {
	Node fstNode = nlist.item(j);
	motors[j] = new UFMMTMotor(fstNode);
      }
    } catch (Exception e) { System.out.println(e.toString()); }
    return motors;
  }

  public static UFMMTMotor getMotorFromXML(String filename, String motName) {
    /* Get an individual motor - used by executive agent */
    UFMMTMotor motor = null; 
    try {
      File file = new File(filename);
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(file);
      doc.getDocumentElement().normalize();
      Element root = doc.getDocumentElement();
      NodeList nlist;
      Element elem;
      nlist = root.getElementsByTagName("motor");
      for (int j = 0; j < nlist.getLength(); j++) {
        Node fstNode = nlist.item(j);
	if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
	  Element fstElmnt = (Element) fstNode;
	  if (fstElmnt.hasAttribute("name")) {
	    if (!motName.equals(fstElmnt.getAttribute("name").trim())) continue;
	    motor = new UFMMTMotor(fstNode);
	    break;
	  }
	}
      }
    } catch (Exception e) { System.out.println(e.toString()); }
    return motor; 
  }

  public String getName() {
    return motorName;
  }

  public String getShortName() {
    return shortName;
  }

  public String getFitsKey() {
    return fitsKey;
  }

  public String getLetter() {
    return letter;
  }

  public int getTotalSteps() {
    return totalSteps; 
  }

  public int getNumPos() {
    return numPos; 
  }

  public String[] getPosNames() {
    return posNames;
  }

  public String[] getPosNames(int[] posNums) {
    String[] names = new String[posNums.length];
    for (int j = 0; j < posNums.length; j++) {
      if (posNums[j] >= numPos) {
	names[j] = "null";
      } else {
	names[j] = posNames[posNums[j]];
      }
    }
    return names;
  }

  public int[] getPosSteps() {
    return posSteps;
  }

  public int[] getPosSteps(int[] posNums) {
    int[] steps = new int[posNums.length];
    for (int j = 0; j < posNums.length; j++) {
      if (posNums[j] >= numPos) {
        steps[j] = 0; 
      } else {
        steps[j] = posSteps[posNums[j]];
      }
    }
    return steps;
  }

  public String getPosName(int steps) {
    String posName = "null";
    for (int j = 0; j < numPos; j++) {
      if (steps%totalSteps == posSteps[j]) posName = posNames[j];
    }
    return posName;
  }

  public int getStepCount(String posName) {
    int steps = Integer.MIN_VALUE;
    for (int j = 0; j < numPos; j++) {
      if (posNames[j].toLowerCase().equals(posName.toLowerCase())) {
	steps = posSteps[j];
	break;
      }
    }
    return steps;
  }

  public String toString() {
    String s = "Motor name: "+motorName+"; steps: "+totalSteps+"; positions: "+numPos;
    for (int j = 0; j < numPos; j++) {
      s+="\n\tPosition: "+posNames[j]+"; steps: "+posSteps[j];
    }
    return s;
  }

  public static void main(String[] args) {
    UFMMTMotor[] motors = getMotorsFromXML("/share/home/warner/MMTPolRCS/etc/ufmmtmotors.xml");
    for (int j = 0; j < motors.length; j++) System.out.println(motors[j].toString());
  }
}
