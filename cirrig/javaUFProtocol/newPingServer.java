import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import javaUFProtocol.*;

public class newPingServer {

    final static String rcsId ="$Id: newPingServer.java,v 1.2 2004/02/13 00:59:08 dan Exp $";
    public static void main(String []args)
    {
        int portNum = 55555 ;
        if( args.length != 0 )
            {
                try
                    {   
                        portNum = Integer.parseInt(args[0]);
                    }
                catch(NumberFormatException e)
                    {
                        System.out.println("Invalid port number.  Port # must be an integer > 1024 and < 65000");
                        return;
                    }
            }

        System.out.println( "Starting server on port " + portNum ) ;

        try
            {
                ServerSocket echoSocket = new ServerSocket(portNum);

                Socket connectionSocket = null ;
                
                while(true) 
                    {
                        // Wait until someone connects to the socket.
                        System.out.println("Waiting for connections");
                        connectionSocket = echoSocket.accept();
                        System.out.println("Got connection!");
                        while(true)
                            {
                                UFProtocol ufp = UFProtocol.createFrom(connectionSocket);
                                if(ufp==null)
                                    {
                                        System.out.println("create failed");
                                        break;
                                    }
                                String new_name =ufp.name()+"--ALIVE--";
                                ufp._name = new_name;
                                if(ufp.sendTo(connectionSocket)<=0)
                                    { 
                                        System.out.println("Write Error");
                                        System.exit(0);
                                    }
                            }
                        System.out.println("done sending, returning to start");
                                // Clean up so someone else can connect
                                //connectionSocket.close();
                                //System.out.println("Connection closed");
                        
                    } // while()
            } 
        catch(IOException ioe)
            {
                System.out.println("EXceeption in main"+ ioe);
                System.exit( 0 ) ;
            }
    }
}


