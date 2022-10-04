package javaUFLib;

import java.io.*;
//===============================================================================
/**
 * Opens a file for IN, OUT or APPEND in constructor. Methods include:
 * ok, readLine, println, and close.
 */
public class TextFile {
  protected String file_name;
  private BufferedReader _inFile ;
  private PrintWriter _outFile ;
  private int _mode;
  private int _numLine = 0;
  private boolean status_is_ok;
  public static int IN = 1;
  public static int OUT = 2;
  public static int APPEND = 3;
  private String _className = this.getClass().getName();
//-------------------------------------------------------------------------------
  /**
   * Construcs the TextFile object
   * @param file_name The name of the file to be read from or written to
   * @param mode should be TextFile.IN, TextFile.OUT or TextFile.APPEND (default is IN).
   */
  public TextFile(String file_name, int mode) {
    this.file_name = file_name;
    this._mode = mode;
    try {
	if (_mode == OUT) {
	    System.out.println(_className + "> Opening new to Write: " + file_name);
	    _outFile = new PrintWriter( new FileWriter(file_name), true /*autoflush*/);
	    status_is_ok = true;
	}
	else if (_mode == APPEND) {
	    System.out.println(_className + "> Appending to: " + file_name);
	    _outFile = new PrintWriter( new FileWriter(file_name, true /*append*/), true /*autoflush*/);
	    status_is_ok = true;
	}
	else {
	    System.out.println(_className + "> Opening for Read: " + file_name);
	    _mode = IN;
	    _inFile = new BufferedReader(new FileReader(file_name));
	    status_is_ok = true;
	}
    }
    catch (IOException ioe) {
        System.err.println(ioe.toString());
        status_is_ok = false;
    }
  }
//-------------------------------------------------------------------------------
  /**
   *Returns the status boolean
   */
  public boolean ok() { return status_is_ok; }

  public boolean status() { return status_is_ok; }

  public int LineNumber() { return _numLine; }

//-------------------------------------------------------------------------------
  /**
   *Gets the next line from the TextFile in IN mode and returns it
   *It shows errors if not in IN mode and if the status is bad
   */
  public String readLine() {
    String line = "";
    if( _mode != IN ) {
	System.err.println("this file is NOT open for Reading");
	status_is_ok = false;
    }
    else {
      try {
	_numLine++;
	line = _inFile.readLine();
      }
      catch (IOException ioe) {
        System.err.println(_className + ".readLine> " + file_name + " at line # = " + _numLine +
			   " - " + ioe.toString());
        status_is_ok = false;
      }
    }

    return line;
  }
//-------------------------------------------------------------------------------
  /**
   * Returns the first string from the file that
   * doesn't have a semicolon at the front
   */
  public String nextUncommentedLine()
    {
       String line = ";";

       while( line.charAt(0) == ';' )
       {
          line = readLine();
	  if (!status_is_ok) return "";
	  if (line == null) break;
          if (line.trim().equals("")) break;
       }

       return line;
    }
//-------------------------------------------------------------------------------
  /**
   *Checks to see if its in OUT or APPEND mode
   *if not, it shows an error and the status flag turns false
   *otherwise it trys to write
   *then shows error if status is bad
   */
  public boolean println(String line) {
    if( _mode != OUT && _mode != APPEND ) {
	System.err.println(_className + ".println> this file is NOT open for Write or Append.");
	status_is_ok = false;
    }
    else {
	_outFile.println(line);
	_numLine++;
	if( _outFile.checkError() ) {
	    System.err.println(_className + ".println> ERROR writing \"" + line + "\" to " + file_name +
			       " at line # " + _numLine );
	    status_is_ok = false;
	}
	else status_is_ok = true;
    }

    return status_is_ok;
  }
//-------------------------------------------------------------------------------
  /**
   *Prints the string passed at the cursor point in the file
   *shows errors if not in OUT or APPEND mode and if status is bad
   *returns the status
   *@param line String: line to be written to the file
   */
  public boolean print(String line) {
    if( _mode != OUT && _mode != APPEND ) {
	System.err.println(_className + ".print> this file is NOT open for Write or Append.");
	status_is_ok = false;
    }
    else {
	_outFile.print(line);
	if( _outFile.checkError() ) {
	    System.err.println(_className + ".print> ERROR writing \"" + line + "\" to " + file_name +
			       " at line # " + (_numLine + 1) );
	    status_is_ok = false;
	}
    }

    return status_is_ok;
  }
//-------------------------------------------------------------------------------
  /**
   *Closes the file
   */
  public void close() {
    System.out.println(_className + "> Closing: " + file_name);
    if (_mode == IN) {
      try {
        _inFile.close();
      }
      catch (IOException ioe) {
        System.err.println(_className + ".close> " + file_name + " - " + ioe.toString());
        status_is_ok = false;
      }
    }
    else if( (_mode == OUT) || (_mode == APPEND) ) {
      _outFile.close();
      if( _outFile.checkError() ) {
	  System.err.println(_className + ".close> " + file_name);
	  status_is_ok = false;
      }
    }
  }
} //end of class TextFile
