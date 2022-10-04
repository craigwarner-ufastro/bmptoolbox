
import java.io.*;
import java.util.*;
import java.net.*;
import javaUFProtocol.*;

public class newPingClient
{

    protected int counter = 0 ;
    protected Random rand = new Random() ;

    public static void main(String[] args)
    { 
        new newPingClient( args ) ;
    }

    public newPingClient( String[] args )
    {
	if(args.length!=2)
	    {
		System.out.println("Please input two arguments: hostname,port");
		return;  
	    }

	String host = args[0];
        int port = 0;
	try 
	    {
		port = Integer.parseInt(args[1]);
	    }
	catch(NumberFormatException e)
	    {
		System.out.println("Invalid port number.");
		return;
	    }
	
        System.out.print("Please enter the number of times you want to loop: ");

        BufferedReader in = null ;
        int num = 0 ;
        try
            {
                in = new BufferedReader(new InputStreamReader(System.in));
                num = Integer.parseInt(in.readLine());
            }
        catch( IOException e )
            {
                System.out.println( "Error obtaining streams" ) ;
                System.exit( 0 ) ;
            }
        catch( NumberFormatException e )
            {
                System.out.println( "Invalid integer" ) ;
                System.exit( 0 ) ;
            }
	
        Socket s = null;
	try {
	s = new Socket(host,port);
	}catch (Exception e) { System.out.println(e.toString()); return;}
	System.out.println("Connection established");
  
	 for(int i=1; i<=num; i++)
            {
                UFProtocol ufp = buildObject();
                System.out.println("Sending " + ufp.description());
                if(ufp.sendTo(s)<=0)
                    {
                        System.out.println("Write Error");
                        System.exit(0);
                    }
    
		System.out.println("Sent " + ufp.description() + " successfully");        
                ufp = UFProtocol.createFrom(s);
                if(null==ufp)
                    {
                        System.out.println("Read Error");
                        System.exit(0);
                    }
                System.out.println("RECEIVED OBJECT NAMED:" + ufp.name());
                System.out.println("THIS IS ITERATION # " + i + " OF THE LOOP!!"); 
		if (ufp instanceof UFStrings) {
		    for (int j=0; j<((UFStrings)ufp).numVals(); j++) 
			System.out.println(((UFStrings)ufp).valData(j));
		} else if (ufp instanceof UFInts) {
		    for (int j=0; j<((UFInts)ufp).numVals(); j++) 
			System.out.println(((UFInts)ufp).valData(j));
		}
                System.out.println("") ;
	    }
	
	/*
	//UFShorts ufs = new UFShorts();
	//UFInts ufs = new UFInts();
	//int [] x = {1,2,3,4};
	//ufs.setNameAndVals("Ints",x);
	UFTimeStamp ufs = new UFTimeStamp("PM");
	if (ufs.sendTo(s)<=0) {
	    System.out.println("Write Error");
	    System.exit(-1);
	}
	System.out.println("Sent TIMESTAMP successfully!");	
	UFProtocol ufp = UFProtocol.createFrom(s);
	if (ufp == null) {
	    System.out.println("Read Error");
	    System.exit(-1);
	}
	System.out.println(ufp.name());
	System.out.println(ufp._timestamp);
	if (ufp instanceof UFStrings) {
	    for (int i=0; i<((UFStrings)ufp).numVals(); i++) 
		System.out.println(((UFStrings)ufp).valData(i));
	} else if (ufp instanceof UFInts) {
	    for (int i=0; i<((UFInts)ufp).numVals(); i++) 
		System.out.println(((UFInts)ufp).valData(i));
	}
	//try{System.in.read();}catch(Exception e) {}
	*/
    }
    
    public UFProtocol buildObject()
    {
        counter++;
        UFProtocol ufp=null;

        String name = "Object: " + counter ;
        int  randomObj = Math.abs(rand.nextInt()%8);
        switch(randomObj){
        case 0:
            ufp = new UFTimeStamp();
            break;
        case 1:
            ufp = new UFStrings("");
            break;
        case 2:
            ufp = new UFBytes();
            break;
	case 3:
	    //default:
	    ufp = new UFShorts();
	    break;
	case 4:
	    ufp = new UFInts();
	    break;
	case 5:
	    //default:
	    ufp = new UFObsConfig();
	    break;
	case 6:
	    //default:
	    ufp = new UFFrameConfig();
	    break;
	case 7:
	    ufp = new UFFloats();
	    break;
        }
	if ( (! (ufp instanceof UFObsConfig))/* && 
	     (! (ufp instanceof UFFrameConfig))*/ )
	    ufp.rename(name);
        return ufp;
    }
}



 
