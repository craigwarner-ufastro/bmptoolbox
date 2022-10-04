package Weather;
/**
 * Title:        ufWeather.java
 * Version:      (see rcsID)
 * Authors:      Craig Warner
 * Company:      University of Florida
 * Description:  Main program to start Weather agent 
 */

import java.util.*;

public class ufWeather {

    public static final String rcsID = "$Name:  $ $Id: ufWeather.java,v 1.1 2010/04/29 21:19:07 warner Exp $";

    public ufWeather(String[] args)
    {
	int serverPort = 57003;
	WeatherAgent wthAgent = new WeatherAgent( serverPort, args );

	wthAgent.exec();
    }

    public static void main(String[] args) {
	System.out.println("Creating new Weather agent...");
	new ufWeather(args);
    }
}
