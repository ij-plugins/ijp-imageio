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
import ij.WindowManager;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import net.sf.ij.jaiio.JAIFileFilter;
import net.sf.ij.jaiio.JAIWriter;
import net.sf.ij.jaiio.JAIFileChooserFactory;

/**
 *  Saves an image using JAI codecs. (http://developer.java.sun.com/developer/sampsource/jai/).
 *
 * @author     Jarek Sacha
 * @created    March 3, 2002
 * @version    $Revision: 1.2 $
 */

public class JAIWriterPlugin implements PlugIn {

  private static JFileChooser jaiChooser;


  /**
   *  Main processing method for the JAIReaderPlugin object
   *
   * @param  arg  Argument.
   */
  public void run(String arg) {
    File file = null;
    ImagePlus imp = WindowManager.getCurrentImage();
    if (imp == null) {
      IJ.showMessage("JAI Writer", "No images are open.");
      return;
    }

    try {
      if (jaiChooser == null) {
        jaiChooser = JAIFileChooserFactory.createJAISaveChooser();
        jaiChooser.setCurrentDirectory(new File(OpenDialog.getDefaultDirectory()));
      }

      if (jaiChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
        FileFilter fileFilter = jaiChooser.getFileFilter();
        String codecName = null;
        if (fileFilter instanceof JAIFileFilter) {
          JAIFileFilter jaiFileFilter = (JAIFileFilter) fileFilter;
          codecName = jaiFileFilter.getCodecName();
        }

        if (codecName == null) {
          IJ.showMessage("JAI Writer", "File format not selected. File not saved.");
          return;
        }

        JAIWriter jaiWriter = new JAIWriter();
        jaiWriter.setFormatName(codecName);
        file = jaiChooser.getSelectedFile();
        jaiWriter.write(file.getAbsolutePath(), imp);
      }
    }
//    catch(FileNotFoundException ex) {
//    }
//    catch(IOException ex) {
//    }
    catch (Exception ex) {
      ex.printStackTrace();
      String msg = "Error writing file: " + file.getName() + ".\n\n";
      msg += (ex.getMessage() == null) ? ex.toString() : ex.getMessage();
      IJ.showMessage("JAI Writer", msg);
    }
  }
}
