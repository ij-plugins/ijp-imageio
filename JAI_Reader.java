/*
 * Image/J Plugins
 * Copyright (C) 2002 Jarek Sacha
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */

import ij.IJ;
import ij.ImagePlus;
import ij.Menus;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

import java.io.File;
import java.lang.reflect.Method;

import net.sf.ij.imageio.JAIReader;

/**
 *  Read image files using JAI image I/O codec
 *  (http://developer.java.sun.com/developer/sampsource/jai/).
 *
 * @author     Jarek Sacha
 * @created    January 22, 2002
 * @version    $Revision: 1.4 $
 */
public class JAI_Reader implements PlugIn {

  private final static String caption = "JAI Reader";
  private final static String requirementMsg = "This plugin requires Java 1.2 or better.";
  private final static String jaiReaderJarName = "JAIReader.jar";
  private final static String jaiReaderClassName = "net.sf.ij.imageio.JAIReader";

  private static Object jaiReader = null;


  /**
   *  Main processing method for the JAI_Reader object
   *
   * @param  arg  (not used)
   */
  public void run(String arg) {

    if (jaiReader == null) {
      // Verify Java version
      IJ.showStatus("Verifying Java version.");
      String javaVersion = System.getProperty("java.version");
      if (javaVersion == null) {
        IJ.showMessage(caption, "Unable to verify Java version.\n"
             + requirementMsg);
        return;
      }
      if (javaVersion.compareTo("1.2") < 0) {
        IJ.showMessage(caption, "Detected Java version " + javaVersion + ".\n"
             + requirementMsg);
        return;
      }

      // Load JAIReader class
      IJ.showStatus("Loading JAI Reader classes..");
      Class jaiReaderClass = null;
      try {
        JarClassLoader jarClassLoader = new JarClassLoader(
            Menus.getPlugInsPath() + jaiReaderJarName);
        jaiReaderClass = jarClassLoader.loadClass(jaiReaderClassName);
      }
      catch (Exception ex) {
        IJ.showMessage("JAI Reader", "This plugin requires "
             + jaiReaderJarName + " available from "
             + "\"http://sourceforge.net/projects/ij-plugins/\".\n\n"
             + ex.getMessage());
        return;
      }

      //  Create instance of JAIReader
      try {
        jaiReader = jaiReaderClass.newInstance();
      }
      catch (Exception ex) {
        IJ.showMessage("JAI Reader",
            "Failed to instantiate class JAIReader.\n\n" + ex.toString());
      }
      IJ.showStatus("");
    }

    // Run JAIReader as plugin
    try {
      ((PlugIn) jaiReader).run(null);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      IJ.showMessage("JAI Reader", ex.toString());
    }
  }
}
