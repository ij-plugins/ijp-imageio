/*
 * Image/J Plugins
 * Copyright (C) 2002-2004 Jarek Sacha
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
import ij.WindowManager;
import ij.Macro;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import net.sf.ij.jaiio.JAIFileChooserFactory;
import net.sf.ij.jaiio.JAIFileFilter;
import net.sf.ij.jaiio.JAIWriter;
import net.sf.ij.jaiio.SaveImageFileDialog;

/**
 *  Saves an image using JAI codecs. (http://developer.java.sun.com/developer/sampsource/jai/).
 *
 * @author     Jarek Sacha
 * @created    March 3, 2002
 * @version    $Revision: 1.1 $
 */

public class JAIWriterWithOptionsPlugin implements PlugIn {

  private static SaveImageFileDialog fileDialog;


  /**
   *  Main processing method for the JAIWriterPlugin object
   *
   * @param  arg  Not used.
   */
  public void run(String arg) {
    ImagePlus imp = WindowManager.getCurrentImage();
    if (imp == null) {
      IJ.showMessage("JAI Writer", "No images are open.");
      return;
    }

    String fileName = null;
    String codecName = null;


    if (fileName == null && codecName == null) {
      // Get fileName and codecName showing save dialog
//        try {
      if (fileDialog == null) {
        fileDialog = new SaveImageFileDialog(new File(OpenDialog.getDefaultDirectory()));
      }

      File file = new File(imp.getTitle());
      fileDialog.setSelectedFile(file);

      fileDialog.show();

//      if (fileDialog.showDialog() != JFileChooser.APPROVE_OPTION) {
//        Macro.abort();
//        return;
//      }
//
//      FileFilter fileFilter = jaiChooser.getFileFilter();
//      if (fileFilter instanceof JAIFileFilter) {
//        JAIFileFilter jaiFileFilter = (JAIFileFilter) fileFilter;
//        codecName = jaiFileFilter.getCodecName();
//        Recorder.recordOption("JAIWriter.codecName", codecName);
//      }
//
//      if (codecName == null) {
//        IJ.showMessage("JAI Writer", "File format not selected. File not saved.");
//        Macro.abort();
//        return;
//      }
//
//      file = jaiChooser.getSelectedFile();
//      if (file.getName().indexOf(".") < 0) {
//        file = new File(file.getParent(),
//            file.getName() + "." + getFileExtension(codecName));
//      }
//      fileName = file.getAbsolutePath();
//      Recorder.recordOption("JAIWriter.fileName", fileName);
////    } catch (Throwable t) {
    }
//
//    //
//    // Now ready to write the image to a file
//    //
//    try {
//      write(imp, fileName, codecName);
//    } catch (IOException e) {
//      e.printStackTrace();
//      Macro.abort();
//      String msg = "Error writing file: " + fileName + ".\n\n";
//      msg += (e.getMessage() == null) ? e.toString() : e.getMessage();
//      IJ.showMessage("JAI Writer", msg);
//    }
  }


  /**
   *
   * @param imp
   * @param fileName
   * @param codecName
   * @throws IOException
   * @throws FileNotFoundException
   */
  private static void write(ImagePlus imp, String fileName, String codecName)
      throws IOException, FileNotFoundException {
    JAIWriter jaiWriter = new JAIWriter();
    jaiWriter.setFormatName(codecName);
    jaiWriter.write(fileName, imp);
  }


  /*
   *  Return typically used extension for given codec name.
   */
  private String getFileExtension(String codecName) {
    if (codecName.compareToIgnoreCase("TIFF") == 0) {
      return "tif";
    } else if (codecName.compareToIgnoreCase("JPEG") == 0) {
      return "jpg";
    } else {
      return codecName.toLowerCase();
    }
  }

}
