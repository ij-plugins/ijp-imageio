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
import ij.Macro;
import ij.WindowManager;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import net.sf.ij.jaiio.*;
import non_com.media.jai.codec.ImageEncodeParam;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Saves an image using JAI codecs. (http://developer.java.sun.com/developer/sampsource/jai/).
 * 
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */

public class ImageIOSaveAsPlugin implements PlugIn {

    private static final String PNG = "png";
    private static final String PNM = "pnm";
    private static final String TIFF = "tiff";
    private static final String JPEG = "jpeg";

    private static final String TITLE = "ImageIO Save As";

    private static final String MACRO_OPTION_FILENAME = "ImageIOSaveAs.fileName";
    private static final String MACRO_OPTION_CODECNAME = "ImageIOSaveAs.codecName";

    private static JFileChooser jaiChooser;
    private EncoderParamDialog paramDialog;


    /**
     * Main processing method for the ImageIOSaveAsPlugin object
     * 
     * @param arg Not used.
     */
    public void run(String arg) {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.showMessage(TITLE, "No images are open.");
            return;
        }

        String fileName = null;
        String codecName = null;
        ImageEncodeParam encodeParam = null;
        

        // Check with ImageJ if macro options are present
        String macroOptions = Macro.getOptions();
        if (macroOptions != null) {
            fileName = Macro.getValue(macroOptions, MACRO_OPTION_FILENAME, fileName);
            codecName = Macro.getValue(macroOptions, MACRO_OPTION_CODECNAME, codecName);

            // Sanity checks
            if (fileName != null && codecName == null) {
                IJ.showMessage(TITLE,
                        "Macro option '" + MACRO_OPTION_CODECNAME + "' is missing");
                Macro.abort();
                return;
            } else if (fileName == null && codecName != null) {
                IJ.showMessage(TITLE,
                        "Macro option '" + MACRO_OPTION_FILENAME + "' is missing");
                Macro.abort();
                return;
            }
        } else {
            if (arg.equalsIgnoreCase(JPEG)) {
                codecName = JPEG;
            } else if (arg.equalsIgnoreCase(PNG)) {
                codecName = PNG;
            } else if (arg.equalsIgnoreCase(PNM)) {
                codecName = PNM;
            } else if (arg.equalsIgnoreCase(TIFF)) {
                codecName = TIFF;
            }

            if (codecName != null) {
                Recorder.recordOption(MACRO_OPTION_CODECNAME, codecName);

                SaveDialog saveDialog = new SaveDialog("Save As " + codecName + "...", imp.getTitle(),
                        "." + getFileExtension(codecName));
                fileName = saveDialog.getDirectory() + File.separator
                        + saveDialog.getFileName();
                if (fileName == null) {
                    Macro.abort();
                    return;
                }
//                Recorder.recordOption(MACRO_OPTION_FILENAME, fileName);
            }
        }


        if (fileName == null && codecName == null) {
            // Get fileName and codecName showing save dialog
            if (jaiChooser == null) {
                jaiChooser = JAIFileChooserFactory.createJAISaveChooser();
                jaiChooser.setCurrentDirectory(new File(OpenDialog.getDefaultDirectory()));
            }

            File file = new File(imp.getTitle());
            jaiChooser.setSelectedFile(file);

            if (jaiChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
                Macro.abort();
                return;
            }

            FileFilter fileFilter = jaiChooser.getFileFilter();
            if (fileFilter instanceof JAIFileFilter) {
                JAIFileFilter jaiFileFilter = (JAIFileFilter) fileFilter;
                codecName = jaiFileFilter.getCodecName();
                Recorder.recordOption(MACRO_OPTION_CODECNAME, codecName);
            }

            if (codecName == null) {
                IJ.showMessage(TITLE, "File format not selected. File not saved.");
                Macro.abort();
                return;
            }

            file = jaiChooser.getSelectedFile();
            if (file.getName().indexOf(".") < 0) {
                file = new File(file.getParent(),
                        file.getName() + "." + getFileExtension(codecName));
            }
            fileName = file.getAbsolutePath();
            Recorder.recordOption(MACRO_OPTION_FILENAME, fileName);
        }

        // Ask for file options
        if (codecName.equalsIgnoreCase(TIFF)) {
            // TODO: detect if image is binary and give an option to save as 1bit compressed image
            if (paramDialog == null) {
                paramDialog = new EncoderParamDialog();
            }
            JaiioUtil.centerOnScreen(paramDialog, false);
            paramDialog.show();
            if (!paramDialog.isAccepted()) {
                Macro.abort();
                IJ.showMessage(TITLE, "Option dialog cancelled, image not saved.");
                return;
            }

            encodeParam = paramDialog.getImageEncodeParam(JaiioUtil.isBinary(imp.getProcessor()));
        }

        //
        // Now ready to write the image to a file
        //
        try {
            write(imp, fileName, codecName, encodeParam);
        } catch (IOException e) {
            e.printStackTrace();
            Macro.abort();
            String msg = "Error writing file: " + fileName + ".\n\n";
            msg += (e.getMessage() == null) ? e.toString() : e.getMessage();
            IJ.showMessage(TITLE, msg);
        }
    }


    /**
     * @param imp       
     * @param fileName  
     * @param codecName 
     * @throws IOException           
     * @throws FileNotFoundException 
     */
    private static void write(ImagePlus imp,
                              String fileName,
                              String codecName,
                              ImageEncodeParam encodeParam)
            throws IOException, FileNotFoundException {
        JAIWriter jaiWriter = new JAIWriter();
        jaiWriter.setFormatName(codecName);
        jaiWriter.setImageEncodeParam(encodeParam);
        jaiWriter.write(fileName, imp);
    }


    /*
     *  Return typically used extension for given codec name.
     */
    private String getFileExtension(String codecName) {
        if (codecName.compareToIgnoreCase(TIFF) == 0) {
            return "tif";
        } else if (codecName.compareToIgnoreCase(JPEG) == 0) {
            return "jpg";
        } else {
            return codecName.toLowerCase();
        }
    }

}
