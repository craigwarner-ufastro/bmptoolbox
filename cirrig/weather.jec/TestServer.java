import java.net.*;
import java.io.*;
import java.lang.*;


public class TestServer {
  public int _serverPort = 57004;
  public String _className = "TestServer";

  public TestServer() {
    try {
      System.out.println(_className + ".run> server port = "+_serverPort);
      ServerSocket ss = new ServerSocket(_serverPort);
      Socket clsoc = ss.accept();
      System.out.println(_className + ".run> accepting new connection...");
      DataOutputStream outpStrm = new DataOutputStream(clsoc.getOutputStream());
      for (int j = 0; j < 10; j++) {
	Thread.sleep(500);
        outpStrm.writeInt(j);
      }
    } catch (Exception ex) {
      System.err.println(_className + ".run> "+ex.toString());
    }
  }

  public static void main(String[] args) {
    TestServer ts = new TestServer();
  }

}
