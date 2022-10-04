package javaMMTLib;
/**
 * Title:        UFMMTLock.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  For synrchronization 
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class UFMMTLock extends Object {

    public static final
        String rcsID = "$Name:  $ $Id: UFMMTLock.java,v 1.2 2010/11/01 23:20:14 warner Exp $";

    protected String _name = "UFMMTLock";
    protected static boolean _verbose = false;
    private boolean _locked = false;
    private String _owner = null;
    private String _creator = null;

    public UFMMTLock(String creator) {
        /* Create empty object */
	_locked = false;
	_owner = null;
	_creator = creator;
    }

    public synchronized boolean getLock(String clientName) {
      synchronized(this) {
	if (_locked) return false;
	_locked = true;
	_owner = clientName;
	return true; 
      }
    }

    public synchronized String getOwner() {
      synchronized(this) {
	return _owner;
      }
    }

    public synchronized boolean hasLock(String clientName) {
      synchronized(this) {
	if (!_locked) return false;
	if (_owner == null) return false;
	if (_owner.equals(clientName)) return true;
	return false;
      }
    }

    public synchronized boolean releaseLock(String clientName) {
      synchronized(this) {
        if (!_locked) return true;
        if (_owner == null) return false;
	if (_owner.equals(clientName)) {
	  _locked = false;
	  _owner = null;
	  return true;
	}
	return false;
      }
    }

    public synchronized boolean breakLock(String clientName) {
      synchronized(this) {
        if (!_locked) return true;
        if (_owner == null) return false;
	if (_creator == null) return false;
	if (_creator.equals(clientName)) {
	  _locked = false;
	  _owner = null;
	  return true;
	}
	return false;
      }
    }
}
