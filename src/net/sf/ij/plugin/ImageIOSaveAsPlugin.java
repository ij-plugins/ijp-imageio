/*
 * Image/J Plugins
 * Copyright (C) 2002-2008 Jarek Sacha
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
 *
 */
package net.sf.ij.plugin;

import com.sun.media.jai.codec.ImageEncodeParam;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import net.sf.ij.jaiio.*;
import net.sf.ij.swing.SwingUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Saves an image using JAI codecs. (http://developer.java.sun.com/developer/sampsource/jai/).
 *
 * @author Jarek Sacha
 * @version $Revision: 1.9 $
 */
public class ImageIOSaveAsPlugin implements PlugIn {
    public static final String PNG = "png";
    public static final String PNM = "pnm";
    public static final String TIFF = "tiff";
    public static final String JPEG = "jpeg";

    public static final String MACRO_OPTION_FILENAME = "ImageIOSaveAs.fileName";
    public static final String MACRO_OPTION_CODECNAME = "ImageIOSaveAs.codecName";

    private static final String TITLE = "ImageIO Save As";

    private static JFileChooser jaiChooser;
    private EncoderParamDialog paramDialog;


    /**
     * When running a macro, both macro options {@link #MACRO_OPTION_FILENAME} and {@link
     * #MACRO_OPTION_FILENAME} must be specified.
     *
     * @param arg Format in which to save image, possible values: "JPEG", "PNG", "PNM", "TIFF", or
     *            <code>null</code>. If <code>null</code> custom save dialog will be shown.
     */
    public void run(final String arg) {

        IJ.showStatus("Starting \"" + TITLE + "\" plugin...");

        boolean useOneBitCompression = false;
        try {
            final ImagePlus imp = WindowManager.getCurrentImage();
            if (imp == null) {
                IJ.showMessage(TITLE, "No images are open.");
                return;
            }

            String fileName = null;
            String codecName;
            ImageEncodeParam encodeParam = null;

            // Check with ImageJ if macro options are present
            String macroOptions = Macro.getOptions();
            if (macroOptions != null) {
                // Running from macro so try to extract macro options
                codecName = Macro.getValue(macroOptions, MACRO_OPTION_CODECNAME, null);
                fileName = Macro.getValue(macroOptions, MACRO_OPTION_FILENAME, null);

                // Sanity checks
                if (codecName == null) {
                    IJ.showMessage(TITLE,
                            "Macro option '" + MACRO_OPTION_CODECNAME + "' is missing");
                    Macro.abort();
                    return;
                }
                if (fileName == null) {
                    IJ.showMessage(TITLE,
                            "Macro option '" + MACRO_OPTION_FILENAME + "' is missing");
                    Macro.abort();
                    return;
                }
            } else {
                // Check if file type was specified as an argument, ignore unknown format names
                if (arg.equalsIgnoreCase(JPEG)) {
                    codecName = JPEG;
                } else if (arg.equalsIgnoreCase(PNG)) {
                    codecName = PNG;
                } else if (arg.equalsIgnoreCase(PNM)) {
                    codecName = PNM;
                } else if (arg.equalsIgnoreCase(TIFF)) {
                    codecName = TIFF;
                } else {
                    codecName = null;
                }

                if (codecName == null) {
                    // Get fileName and codecName showing save dialog
                    if (jaiChooser == null) {
                        jaiChooser = JAIFileChooserFactory.createJAISaveChooser();
                        final String dirName = OpenDialog.getDefaultDirectory();
                        jaiChooser.setCurrentDirectory(new File(dirName != null ? dirName : "."));
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
                } else if (fileName == null) {
                    SaveDialog saveDialog = new SaveDialog("Save As " + codecName + "...", imp.getTitle(),
                            "." + getFileExtension(codecName));
                    // Make only single call to saveDialog.getFileName(). When recording a macro,
                    // each call records path in a macro (ImageJ 1.33k)
                    final String saveDialogFileName = saveDialog.getFileName();
                    if (saveDialogFileName == null) {
                        Macro.abort();
                        return;
                    }
                    if (saveDialog.getDirectory() != null) {
                        fileName = saveDialog.getDirectory() + File.separator + saveDialogFileName;
                    } else {
                        fileName = saveDialogFileName;
                    }
                }

                Recorder.recordOption(MACRO_OPTION_CODECNAME, codecName);
                Recorder.recordOption(MACRO_OPTION_FILENAME, fileName);

                // Ask for file options
                if (codecName.equalsIgnoreCase(TIFF)) {
                    // TODO: detect if image is binary and give an option to save as 1bit compressed image
                    if (paramDialog == null) {
                        paramDialog = new EncoderParamDialog();
                    }
                    SwingUtils.centerOnScreen(paramDialog, false);
                    paramDialog.setVisible(true);
                    if (!paramDialog.isAccepted()) {
                        Macro.abort();
                        IJ.showMessage(TITLE, "Option dialog cancelled, image not saved.");
                        return;
                    }

                    boolean isBinary = imp.getType() != ImagePlus.COLOR_256
                            && JaiioUtil.isBinary(imp.getProcessor());

                    if (isBinary) {
                        useOneBitCompression = IJ.showMessageWithCancel("Save as TIFF",
                                "Image seems to be two level binary. Do you want to save it using 1 bit per pixel?");
                    }

                    encodeParam = paramDialog.getImageEncodeParam(useOneBitCompression);
                }

            }

            //
            // Now ready to write the image to a file
            //
            try {
                IJ.showStatus("Writing image as " + codecName.toUpperCase() + " to " + fileName);
                write(imp, fileName, codecName, encodeParam, useOneBitCompression);
            } catch (IOException e) {
                e.printStackTrace();
                Macro.abort();
                String msg = "Error writing file: " + fileName + ".\n\n";
                msg += (e.getMessage() == null) ? e.toString() : e.getMessage();
                IJ.showMessage(TITLE, msg);
            }
        } finally {
            IJ.showStatus("");
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
                              ImageEncodeParam encodeParam,
                              boolean useOneBitCompression)
            throws IOException {
        JAIWriter jaiWriter = new JAIWriter();
        jaiWriter.setFormatName(codecName);
        jaiWriter.setImageEncodeParam(encodeParam);
        jaiWriter.write(fileName, imp, useOneBitCompression);
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
