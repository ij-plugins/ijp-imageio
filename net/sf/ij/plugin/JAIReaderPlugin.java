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
package net.sf.ij.plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

import java.io.File;
import javax.swing.JFileChooser;

import net.sf.ij.imageio.JAIReader;
import net.sf.ij.swing.ImageFileChooserFactory;

/**
 *  Opens file chooser dialog and open the image using JAI codec.
 *
 * @author     Jarek Sacha
 * @created    February 10, 2002
 * @version    $Revision: 1.1 $
 */

public class JAIReaderPlugin implements PlugIn {

  /**
   *  Argument passed to <code>run</code> method to use standard Image/J open
   *  dialog.
   */
  public final static String ARG_SIMPLE = "simple";
  /**
   *  Argument passed to <code>run</code> method to use open dialog with an
   *  image preview.
   */
  public final static String ARG_IMAGE_PREVIEW = "image preview";

  private static JFileChooser jaiChooser;


  /**
   *  Main processing method for the JAIReaderPlugin object
   *
   * @param  arg  Description of Parameter
   */
  public void run(String arg) {
    String type = (arg == null) ? ARG_SIMPLE : arg.trim().toLowerCase();

    File[] files = null;
    if (type.equals(ARG_IMAGE_PREVIEW)) {
      files = getFileImagePreview();
    }
    else {
      files = getFileSimple();
    }

    if (files != null) {
      for (int i = 0; i < files.length; ++i) {
        open(files[i]);
      }
    }
  }


  /*
   *
   */
  private File[] getFileImagePreview() {
    if (jaiChooser == null) {
      jaiChooser = ImageFileChooserFactory.createJAIImageChooser();
      jaiChooser.setCurrentDirectory(new File(OpenDialog.getDefaultDirectory()));
      jaiChooser.setMultiSelectionEnabled(true);
    }

    if (jaiChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      return jaiChooser.getSelectedFiles();
    }
    else {
      return null;
    }
  }


  /*
   *
   */
  private File[] getFileSimple() {
    OpenDialog openDialog = new OpenDialog("Open...", null);
    if (openDialog.getFileName() == null) {
      // No selection
      return null;
    }

    File[] files = new File[1];
    files[0] = new File(openDialog.getDirectory(), openDialog.getFileName());

    return files;
  }


  /*
   *
   */
  private void open(File file) {
    IJ.showStatus("Opening: " + file.getName());

    try {
      JAIReader jaiReader = new JAIReader();
      ImagePlus[] images = jaiReader.read(file);
      if (images != null) {
        for (int i = 0; i < images.length; ++i) {
          images[i].show();
        }
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      IJ.showMessage("JAI Reader", "Error opening file: "
           + file.getName() + ".\n\n" + ex.getMessage());
    }
  }
}
