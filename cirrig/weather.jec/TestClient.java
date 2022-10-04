import java.net.*;
import java.io.*;
import java.lang.*;


public class TestClient {
  public int _serverPort = 57004;
  public String _className = "TestClient";

  public TestClient() {
    try {
      System.out.println(_className + ".run> server port = "+_serverPort);
      Socket socket = new Socket("localhost", _serverPort);
      System.out.println(_className + ".run> accepting new connection...");
      DataInputStream inpStrm = new DataInputStream(socket.getInputStream());
      for (int j = 0; j < 5; j++) {
	System.out.println(inpStrm.readInt());
      }
      socket.close();
      for (int j = 0; j < 5; j++) {
	Thread.sleep(1000);
      }
      System.out.println("End client");
    } catch (Exception ex) {
      System.err.println(_className + ".run> "+ex.toString());
    }
  }

  public static void main(String[] args) {
    TestClient ts = new TestClient();
  }

}
