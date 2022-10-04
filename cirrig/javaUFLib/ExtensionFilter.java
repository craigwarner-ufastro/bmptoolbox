package javaUFLib;

import javax.swing.filechooser.FileFilter;
import java.io.*;


//===============================================================================
/**
 * Filters the extensions of files that jei handles
 */
public class ExtensionFilter extends FileFilter {
  public static final String rcsID = "$Name:  $ $Id: ExtensionFilter.java,v 1.2 2004/03/30 04:42:26 varosi Exp $";
  private String extensions[];
  private String description;


//-------------------------------------------------------------------------------
  /**
   *Constructor
   *@param description Description of ??
   *@param extension Extension of ??
   */
  public ExtensionFilter (String description, String extension) {
    this (description, new String[] {extension});
  } //end of ExtensionFilter


//-------------------------------------------------------------------------------
  /**
   *Constructor
   *@param description Description of ??
   *@param extensions Extension of ??
   */
  public ExtensionFilter (String description, String extensions[]) {
    this.description = description;
    this.extensions = (String[])extensions.clone();
  } //end of ExtensionFilter


//-------------------------------------------------------------------------------
  /**
   * Returns the class-field: description
   */
  public String getDescription() {
    return description;
  } //end of getDescription


//-------------------------------------------------------------------------------
  /**
   * Accepts the file
   *@param file File that is going through the filter
   */
  public boolean accept (File file) {
    if (file.isDirectory()) {
      return true;
    }
    int count = extensions.length;
    String path = file.getAbsolutePath();
    for (int i = 0; i < count ;i++) {
      String ext = extensions[i];
      if (path.endsWith(ext) && (path.charAt(path.length()-ext.length()) == '.')) {
        return true;
      }
    }
    return false;
  } //end of accept

} //end of class ExtensionFilter

