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
import javax.swing.JFileChooser;

/**
 * @author     Jarek Sacha
 * @created    February 2, 2002
 * @version    $Revision: 1.1 $
 */

public class IJFileChooserFactory {

  /**
   *  Description of the Method
   *
   * @return    Description of the Returned Value
   */
  public static JFileChooser createIJImageChooser() {
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(new File(".").getAbsoluteFile());

    IJImageFilePreviewer previewer = new IJImageFilePreviewer(chooser);
    chooser.setAccessory(previewer);

    return chooser;
  }



  /**
   *  Description of the Method
   *
   * @param  extension    Description of Parameter
   * @param  description  Description of Parameter
   * @return              Description of the Returned Value
   */
  public static JFileChooser createIJImageChooserWithFilter(String extension,
      String description) {

    JFileChooser chooser = createIJImageChooser();
    ExtensionFileFilter fileFilter = new ExtensionFileFilter(extension, description);
    chooser.setFileFilter(fileFilter);

    return chooser;
  }
}
