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
 * @version    $Revision: 1.3 $
 */
public abstract class JarPluginProxy implements PlugIn {

  private final static String requirementMsg
       = "This plugin requires Java 1.2 or better.";
  private final static String jarName = "ij-jai-imageio.jar";

  private Object pluginObject = null;


  /**
   *  Gets the PluginClassName attribute of the JarPluginProxy object
   *
   * @return    The PluginClassName value
   */
  protected abstract String getPluginClassName();


  /**
   *  Override this method to return argument that needs to be passed to the
   *  plugin. Default implementation assumes that there is no argument and
   *  returns <code>null</code>.
   *
   * @return    Value of argument that needs to be passed to the plugin.
   * @see       ij.plugin.PlugIn.run(String arg)
   */
  protected String getPluginArg() {
    return null;
  }


  /**
   *  Main processing method for the JAI_Reader object
   *
   * @param  arg  (not used)
   */
  public final void run(String arg) {

    if (pluginObject == null) {
      // Verify Java version
      IJ.showStatus("Verifying Java version.");
      String javaVersion = System.getProperty("java.version");
      if (javaVersion == null) {
        IJ.showMessage(getPluginName(), "Unable to verify Java version.\n"
             + requirementMsg);
        return;
      }
      if (javaVersion.compareTo("1.2") < 0) {
        IJ.showMessage(getPluginName(), "Detected Java version "
             + javaVersion + ".\n" + requirementMsg);
        return;
      }

      // Load plugin class
      IJ.showStatus("Loading " + getPluginName() + " classes..");
      Class pluginClass = null;
      try {
        JarClassLoader jarClassLoader = new JarClassLoader(
            Menus.getPlugInsPath() + jarName);
        pluginClass = jarClassLoader.loadClass(getPluginClassName());
      }
      catch (Exception ex) {
        IJ.showMessage(getPluginName(), "This plugin requires "
             + jarName + " available from "
             + "\"http://sourceforge.net/projects/ij-plugins/\".\n\n"
             + ex.getMessage());
        return;
      }

      //  Create instance of the plugin
      try {
        pluginObject = pluginClass.newInstance();
      }
      catch (Exception ex) {
        IJ.showMessage(getPluginName(),
            "Failed to instantiate class " + getPluginClassName() + ".\n\n"
             + ex.toString());
      }
      IJ.showStatus("");
    }

    // Run the plugin
    try {
      ((PlugIn) pluginObject).run(getPluginArg());
    }
    catch (Exception ex) {
      ex.printStackTrace();
      IJ.showMessage(getPluginName(), ex.toString());
    }
  }


  /*
   *
   */
  private String getPluginName() {
    String pluginName = "<plugin>";
    try {
      String pluginClassName = this.getClass().getName();
      if (pluginClassName != null) {
        int index = pluginClassName.lastIndexOf(".");
        if (index != -1) {
          pluginClassName = pluginClassName.substring(index);
        }
      }
      pluginName = pluginClassName.replace('_', ' ');
    }
    catch (Throwable t) {
      t.printStackTrace();
    }

    return pluginName;
  }
}
