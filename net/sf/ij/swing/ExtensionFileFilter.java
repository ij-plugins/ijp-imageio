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
package net.sf.ij.swing;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *  Description of the Class
 *
 * @author     Jarek Sacha
 * @created    November 6, 2000
 */

public class ExtensionFileFilter extends FileFilter {
  private String fileDescription = null;
  private String fileExtension = null;


  /**
   *  Constructor for the ExtensionFileFilter object
   *
   * @param  extension    Description of Parameter
   * @param  description  Description of Parameter
   */
  public ExtensionFileFilter(String extension, String description) {
    fileExtension = "." + extension.toLowerCase();
    fileDescription = description + " (*" + fileExtension + ")";
  }


  /**
   *  Gets the Description attribute of the ExtensionFileFilter object
   *
   * @return    The Description value
   */
  public String getDescription() {
    return fileDescription;
  }


  /**
   *  Description of the Method
   *
   * @param  f  Description of Parameter
   * @return    Description of the Returned Value
   */
  public boolean accept(File f) {
    if (f == null) {
      return false;
    }
    if (f.isDirectory()) {
      return true;
    }
    return f.getName().toLowerCase().endsWith(fileExtension);
  }
}

