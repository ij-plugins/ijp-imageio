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

import FileSeekableStream;
import ImageCodec;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *  Description of the Class
 *
 * @author     Jarek Sacha
 * @created    November 6, 2000
 */

public class JAIFileFilter extends FileFilter {
  private String codecName = null;
  private String decription = "All Supported Images";


  /**
   *  Constructor for the JAIFileFilter object
   */
  public JAIFileFilter() {
  }


  /**
   *  Constructor for the ExtensionFileFilter object
   *
   * @param  codecName  Description of Parameter
   */
  public JAIFileFilter(String codecName) {
    if (codecName != null && codecName.length() > 0) {
      this.codecName = codecName;
      this.decription = codecName.toUpperCase();
    }
  }


  /**
   *  Gets the Description attribute of the ExtensionFileFilter object
   *
   * @return    The Description value
   */
  public String getDescription() {
    return decription;
  }


  /**
   *  Description of the Method
   *
   * @param  file  Description of Parameter
   * @return       Description of the Returned Value
   */
  public boolean accept(File file) {
    if (file == null) {
      return false;
    }
    if (file.isDirectory() || !file.canRead() || file.length() < 4) {
      return true;
    }

    // Find matching decoders
    try {
      FileSeekableStream fss = new FileSeekableStream(file);
      String[] decoders = ImageCodec.getDecoderNames(fss);
      if (decoders == null || decoders.length == 0) {
        return false;
      }

      if (codecName == null) {
        // File is one of the supported image type.
        return true;
      }

      for (int i = 0; i < decoders.length; ++i) {
        if (codecName.equals(decoders[i])) {
          return true;
        }
      }
    }
    catch (Throwable t) {
    }
    return false;
  }
}

