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
import ij.WindowManager;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import net.sf.ij.imageio.JAIWriter;
import net.sf.ij.swing.ImageFileChooserFactory;
import net.sf.ij.swing.JAIFileFilter;

/**
 *  Opens file chooser dialog and open the image using JAI codec.
 *
 * @author     Jarek Sacha
 * @created    February 10, 2002
 * @version    $Revision: 1.1 $
 */

public class JAI_Writer implements PlugInFilter {

  private static JFileChooser jaiChooser;


  /**
   *  Description of the Method
   *
   * @param  arg  Description of Parameter
   * @param  imp  Description of Parameter
   * @return      Description of the Returned Value
   */
  public int setup(String arg, ImagePlus imp) {
    return DOES_ALL | NO_CHANGES;
  }


  /**
   *  Main processing method for the JAIReaderPlugin object
   *
   * @param  ip  Description of Parameter
   */
  public void run(ImageProcessor ip) {
    File file = null;
    ImagePlus imp = WindowManager.getCurrentImage();
    if (imp == null) {
      IJ.showMessage("JAI Writer", "No images are open.");
      return;
    }

    try {
      if (jaiChooser == null) {
        jaiChooser = ImageFileChooserFactory.createJAIImageChooser();
        jaiChooser.setCurrentDirectory(new File(OpenDialog.getDefaultDirectory()));
        jaiChooser.setMultiSelectionEnabled(false);
        jaiChooser.setAccessory(null);
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
    catch(Exception ex) {
      ex.printStackTrace();
      String msg = "Error writing file: " + file.getName() + ".\n\n";
      msg += (ex.getMessage() == null) ? ex.toString() : ex.getMessage();
      IJ.showMessage("JAI Writer", msg);
    }
  }
}
