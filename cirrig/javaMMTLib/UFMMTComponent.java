package javaMMTLib;
/**
 * Title:        UFMMTComponent.java
 * Version:      (see rcsID)
 * Copyright:    Copyright (c) 2010
 * Author:       Craig Warner
 * Company:      University of Florida
 * Description:  Interface implemented by all GUI components for MJEC 
 */

import java.util.*;

public interface UFMMTComponent { 
    /* Changed from getValue to getVal to avoid overriding JProgressBar's int getValue() */
    public String getVal();
    public void setValue(String val);
    public void registerComponent(UFGUIRecord guiRec, boolean isInput);
    public void registerComponent(LinkedHashMap <String, UFGUIRecord> database, boolean isInput);
    public boolean isMarked();
    public void mark();
    public void clear();
    public void apply();
    public String getCommandValue();
    public void setCommand(String command);
    public void clearShadows();
    public void addShadow(UFMMTComponent component);
    public void setEnabled(boolean enabled);
    public void registerClearRec(LinkedHashMap <String, UFGUIRecord> database, String clearName);
    public void setHealthAndMess(String health, String mess);
}
