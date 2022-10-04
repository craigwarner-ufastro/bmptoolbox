package BMPToolbox;
/**
 * Title:        Irrigator
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  interface for irrigators such as BMPPLCs to implement
 */


import java.util.*;

public interface Irrigator { 
    public void verbose(boolean verbose);

    //Return irrigator type
    public String getType();

    //Return host IP
    public String getHost();

    //Return UID associated with irrgator
    public int getUid();

    //Power on the device.  Can be a no-op
    public boolean powerOn();

    //Power off the device.  Can be a no-op
    public boolean powerOff();

    //Start a timer
    public boolean startTimer();

    //Pause the timer
    public boolean pauseTimer();

    //Resume a paused timer
    public boolean resumeTimer();

    //Set a timer value
    public boolean setTimer(float seconds);

    //returns true if an error
    public boolean getErrorStatus();

    //returns an error message
    public String getErrorMessage();

    //true = on, false = off
    public boolean getPowerStatus();

    //get timer set value
    public float getTimer();

    //get current timer value
    public float getTimerValue();

    //get the timer status
    public int getTimerStatus();

    //connect to the device
    public boolean connect();

    //check the connection
    public boolean isConnected();

    //close the connection to device
    public void closeConnection();

    //check if irrigator is busy
    public boolean busy();
}
