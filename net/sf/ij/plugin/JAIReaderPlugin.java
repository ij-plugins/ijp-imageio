/***
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
import net.sf.ij.jaiio.ImageFileChooser;

import net.sf.ij.jaiio.JAIFileChooserFactory;
import net.sf.ij.jaiio.JAIReader;

/**
 *  Opens file chooser dialog and open the image using JAI codec.
 *
 * @author     Jarek Sacha
 * @created    February 10, 2002
 * @version    $Revision: 1.6 $
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

  private static ImageFileChooser jaiChooser;
  private int[] pageIndex;
  private File[] files;


  /*
   *
   */
  private static void open(File file, int[] pageIndex) {
    IJ.showStatus("Opening: " + file.getName());

    try {
      ImagePlus[] images = JAIReader.read(file, pageIndex);
      if (images != null) {
        for (int i = 0; i < images.length; ++i) {
          images[i].show();
        }
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      String msg = "Error opening file: " + file.getName() + ".\n\n";
      msg += (ex.getMessage() == null) ? ex.toString() : ex.getMessage();
      IJ.showMessage("JAI Reader", msg);
    }
  }


  /**
   *  Main processing method for the JAIReaderPlugin object. Type of the file
   *  dialog is determined by value of <code>arg</code>. If it is equal <code>ARG_IMAGE_PREVIEW</code>
   *  then file chooser with image preview will be used. By default standard
   *  Image/J's open dialog is used.
   *
   * @param  arg  Can be user to specify type of the open dialog.
   */
  public void run(String arg) {
    String type = (arg == null) ? ARG_SIMPLE : arg.trim().toLowerCase();

    files = null;
    pageIndex = null;
    if (type.equals(ARG_IMAGE_PREVIEW)) {
      selectFilesWithImagePreview();
    }
    else {
      selectFiles();
    }

    if (files != null) {
      for (int i = 0; i < files.length; ++i) {
        open(files[i], pageIndex);
      }
    }
  }


  /*
   *
   */
  private void selectFilesWithImagePreview() {
    if (jaiChooser == null) {
      jaiChooser = JAIFileChooserFactory.createJAIOpenChooser();
      jaiChooser.setCurrentDirectory(new File(OpenDialog.getDefaultDirectory()));
      jaiChooser.setMultiSelectionEnabled(true);
    }

    if (jaiChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      files = jaiChooser.getSelectedFiles();
      pageIndex = jaiChooser.getPageIndex();
    }
    else {
      files = null;
      pageIndex = null;
    }
  }


  /*
   *
   */
  private void selectFiles() {
    pageIndex = null;

    OpenDialog openDialog = new OpenDialog("Open...", null);
    if (openDialog.getFileName() == null) {
      // No selection
      files = null;
    }

    if (files == null || files.length != 1) {
      files = new File[1];
    }
    files[0] = new File(openDialog.getDirectory(), openDialog.getFileName());
  }
}
