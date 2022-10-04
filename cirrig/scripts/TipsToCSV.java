import java.io.*;
import java.util.*;

public class TipsToCSV {
  public static void main(String[] args) {
    if (args.length < 2 || args[0].equals("-h")) {
      System.out.println("Usage: javaw.exe TipsToCSV /path/to/SDCard output.csv");
      System.exit(0);
    }
    String dirname = args[0];
    String[] fnames;
    File f = new File(dirname);
    if (!f.exists() || !f.isDirectory()) {
      System.out.println("Directory "+dirname+" does not exist or is not a directory!");
      System.exit(0);
    }
    fnames = f.list();
    ArrayList<String> output = new ArrayList();
    output.add("Date,\tTip1,\tTip2,\tTip3,\tTip4");
    for (int j = 0; j < fnames.length; j++) {
      //System.out.println(fnames[j]);
      if (!fnames[j].startsWith("C") || !fnames[j].endsWith("0.TXT")) continue;
      String currLine = "20"+fnames[j].substring(1,3)+"-"+fnames[j].substring(3,5)+"-"+fnames[j].substring(5,7);
      boolean success = true;
      for (int i = 0; i < 4; i++) {
	String currFilename = fnames[j].substring(0,7)+i+".TXT";
	try {
          BufferedReader br = new BufferedReader(new FileReader(dirname+"/"+currFilename));
	  currLine += ",\t"+br.readLine();
	  br.close();
	} catch(IOException ioe) {
	  System.out.println(ioe.toString());
	  success = false;
	  break;
	}
      }
      if (success) output.add(currLine);
    }
    try {
      PrintWriter pw = new PrintWriter(new FileOutputStream(args[1]));
      for (int j = 0; j < output.size(); j++) {
	pw.println(output.get(j));
      }
      pw.close();
    } catch(IOException ioe) {
      System.out.println(ioe.toString());
    }
  }
}
