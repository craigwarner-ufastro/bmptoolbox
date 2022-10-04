package javaMMTLib;
/**
 * Title:        UFMMTDitherPattern.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  MMT DitherPattern class 
 */

import java.io.*;
import java.lang.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class UFMMTDitherPattern {
  protected String name;
  protected int numPos;
  protected double[] ra, dec;
  protected double raOrigin, decOrigin;
  protected int currPos;
  protected boolean increment;

  public UFMMTDitherPattern(String filename) {
      name = "none";
      numPos = 0;
      raOrigin = 0;
      decOrigin = 0;
      currPos = 0;
      increment = false;
      Document doc = null;

      try {
        File file = new File(filename);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        doc = db.parse(file);
      } catch(Exception e) {
	System.out.println(e.toString());
      }
      doc.getDocumentElement().normalize();
      Element root = doc.getDocumentElement();
      if (root.hasAttribute("name")) {
	name = root.getAttribute("name").trim();
      }
      NodeList nlist;
      Element elem;
      nlist = root.getElementsByTagName("position");
      numPos = nlist.getLength();
      ra = new double[numPos];
      dec = new double[numPos];

      for (int j = 0; j < numPos; j++) {
	Node posNode = nlist.item(j);
        if (posNode.getNodeType() == Node.ELEMENT_NODE) {
          Element posElmnt = (Element)posNode;
          NodeList raList = posElmnt.getElementsByTagName("ra");
          elem = (Element)raList.item(0);
	  try {
	    ra[j] = Double.parseDouble(elem.getFirstChild().getNodeValue().trim());
	  } catch(NumberFormatException nfe) { ra[j] = 0; }
          NodeList decList = posElmnt.getElementsByTagName("dec");
          elem = (Element)decList.item(0);
	  try {
	    dec[j] = Double.parseDouble(elem.getFirstChild().getNodeValue().trim());
	  } catch(NumberFormatException nfe) { dec[j] = 0; }
	  if (j == 0) {
	    raOrigin = ra[j];
	    decOrigin = dec[j];
	  }
	}
      }
  }
 
  public String getName() {
    return name;
  }

  public int getNumPos() {
    return numPos; 
  }

  public double[] getRA() {
    return ra;
  }

  public double[] getDec() {
    return dec; 
  }

  public boolean nextPos() {
    if (currPos < numPos-1) {
      if (increment) currPos++; else increment = true;
      return true;
    }
    return false;
  }

  public void reset() {
    currPos = 0;
    increment = false;
  }

  public double getCurrRA() {
    return ra[currPos];
  }

  public double getCurrDec() {
    return dec[currPos];
  }

  public int getCurrPos() {
    return currPos;
  }

  public String toString() {
    String s = "DitherPattern name: "+name+"; positions: "+numPos;
    for (int j = 0; j < numPos; j++) {
      s+="\n\tPosition "+j+": RA = "+ra[j]+"; Dec = "+dec[j];
    }
    return s;
  }

  public static void main(String[] args) {
    UFMMTDitherPattern dither = new UFMMTDitherPattern("/share/home/warner/MMTPolRCS/etc/ditherPatterns/raDec5point.xml");
    System.out.println(dither.toString());
  }
}
