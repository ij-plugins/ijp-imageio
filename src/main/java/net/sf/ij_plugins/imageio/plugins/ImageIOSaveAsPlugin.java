/*
 * Image/J Plugins
 * Copyright (C) 2002-2016 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
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

package net.sf.ij_plugins.imageio.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import net.sf.ij_plugins.imageio.BufferedImageFactory;
import net.sf.ij_plugins.imageio.IJImageIO;
import net.sf.ij_plugins.imageio.IJImageIOException;

import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * @author Jarek Sacha
 */
public final class ImageIOSaveAsPlugin implements PlugIn {

    // TODO: add support for use in macros. Arecord/restore macro options as GenericDialog would do.

    public static final String PNG = "png";
    public static final String PNM = "pnm";
    public static final String TIFF = "tiff";
    public static final String JPEG = "jpeg";

    public static final String MACRO_OPTION_FILENAME = "ImageIOSaveAs.fileName";
    public static final String MACRO_OPTION_CODECNAME = "ImageIOSaveAs.codecName";

    private static final String TITLE = "ImageIO Save As ...";

    private static JFileChooser fileChooser;
    private EncoderParamDialog paramDialog;

    public void run(final String arg) {
        IJ.showStatus("Starting \"" + TITLE + "\" plugins...");

        boolean useOneBitCompression = false;
        try {
            final ImagePlus imp = WindowManager.getCurrentImage();
            if (imp == null) {
                IJ.noImage();
                return;
            }

            String fileName = null;
            ImageWriteParam writerParam = null;

//            // Check with ImageJ if macro options are present
//            final String macroOptions = Macro.getOptions();
//            if (macroOptions != null) {
//                // Running from macro so try to extract macro options
//                codecName = Macro.getValue(macroOptions, MACRO_OPTION_CODECNAME, null);
//                fileName = Macro.getValue(macroOptions, MACRO_OPTION_FILENAME, null);
//
//                // Sanity checks
//                if (codecName == null) {
//                    IJ.showMessage(TITLE,
//                            "Macro option '" + MACRO_OPTION_CODECNAME + "' is missing");
//                    Macro.abort();
//                    return;
//                }
//                if (fileName == null) {
//                    IJ.showMessage(TITLE,
//                            "Macro option '" + MACRO_OPTION_FILENAME + "' is missing");
//                    Macro.abort();
//                    return;
//                }
//            } else {

            // Get fileName and codecName showing save dialog
            if (fileChooser == null) {
                fileChooser = ImageFileChooserFactory.createJAISaveChooser();
                final String dirName = OpenDialog.getDefaultDirectory();
                fileChooser.setCurrentDirectory(new File(dirName != null ? dirName : "."));
            }
            final String dirName = OpenDialog.getDefaultDirectory();
            fileChooser.setCurrentDirectory(new File(dirName != null ? dirName : "."));

            String imageTitle = imp.getTitle();
            File file;
            if (imageTitle != null) {
                file = new File(imp.getTitle());
                fileChooser.setSelectedFile(file);
            } else {
                IJ.error("Image title cannot be null");
                Macro.abort();
                return;
            }

            // FIXME: use ImageJ window as parent
            if (fileChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
                Macro.abort();
                return;
            }

            OpenDialog.setDefaultDirectory(fileChooser.getSelectedFile().getParentFile().getAbsolutePath());

            ImageWriterSpi spi = null;
            final FileFilter fileFilter = fileChooser.getFileFilter();
            if (fileFilter instanceof ImageFileFilter) {
                final ImageFileFilter imageFileFilter = (ImageFileFilter) fileFilter;
                spi = imageFileFilter.getSPI();
//                Recorder.recordOption(MACRO_OPTION_CODECNAME, codecName);
            } else {
                IJ.log(TITLE + " - Internal error unexpected FileChooser filter: " + fileFilter);
            }

            if (spi == null) {
                IJ.showMessage(TITLE, "File format not selected. File not saved.");
                Macro.abort();
                return;
            }

            file = fileChooser.getSelectedFile();
//            if (!file.getName().contains(".")) {
//                file = new File(file.getParent(),
//                        file.getName() + "." + getFileExtension(codecName));
//            }
//            fileName = file.getAbsolutePath();

//                Recorder.recordOption(MACRO_OPTION_CODECNAME, codecName);
//                Recorder.recordOption(MACRO_OPTION_FILENAME, fileName);
//
            // Ask for file options
//                if (codecName.equalsIgnoreCase(TIFF)) {
//                    // TODO: detect if image is binary and give an option to save as 1bit compressed image
//                    if (paramDialog == null) {
//                        paramDialog = new EncoderParamDialog();
//                    }
//                    SwingUtils.centerOnScreen(paramDialog, false);
//                    paramDialog.setVisible(true);
//                    if (!paramDialog.isAccepted()) {
//                        Macro.abort();
//                        IJ.showMessage(TITLE, "Option dialog cancelled, image not saved.");
//                        return;
//                    }
//
//                    final boolean isBinary = imp.getType() != ImagePlus.COLOR_256
//                            && JaiioUtil.isBinary(imp.getProcessor());
//
//                    if (isBinary) {
//                        useOneBitCompression = IJ.showMessageWithCancel("Save as TIFF",
//                                "Image seems to be two level binary. Do you want to save it using 1 bit per pixel?");
//                    }
//
//                    encodeParam = paramDialog.getImageEncodeParam(useOneBitCompression);
//                }
//
//            }
            if ("com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriter".equals(spi.getPluginClassName())) {
                IJ.log("TODO: show TIFF options dialog");
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

                final boolean isBinary = imp.getType() != ImagePlus.COLOR_256 && imp.getProcessor().isBinary();

                if (isBinary) {
                    useOneBitCompression = IJ.showMessageWithCancel("Save as TIFF",
                            "Image seems to be two level binary. Do you want to save it using 1 bit per pixel?");
                }

//                com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi
//                com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriter

                writerParam = paramDialog.getImageWriteParam(useOneBitCompression);
//                spi.createWriterInstance().setOutput();
            }


            //
            // Now ready to write the image to a file
            //
            try {
                IJ.showStatus("Writing image as " + spi.getDescription(Locale.US) + " to " + file.getName());
                IJ.log("Saving using SPI: " + spi.getPluginClassName());
                IJ.log("Saving using SPI: " + spi.getClass().getCanonicalName());

                ImageWriter writer = null;
                try {
                    writer = spi.createWriterInstance();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final BufferedImage[] bis = new BufferedImage[imp.getNSlices()];
                for (int s = 0; s < imp.getNSlices(); s++) {
                    bis[s] = BufferedImageFactory.createFrom(imp, s, useOneBitCompression);
                }

                IJImageIO.write(bis, file.getAbsoluteFile(), writer, null, writerParam);


            } catch (final IJImageIOException e) {
                e.printStackTrace();
                Macro.abort();
                String msg = "Error writing file: " + file.getAbsolutePath() + ".\n\n";
                msg += (e.getMessage() == null) ? e.toString() : e.getMessage();
                IJ.showMessage(TITLE, msg);
                IJ.log(e.toString());
            }


        } finally {
            IJ.showStatus("");
        }
    }

//    /**
//     */
//    private static void write(final ImagePlus imp,
//                              final String fileName,
//                              final ImageWriterSpi spi,
//                              final ImageEncodeParam encodeParam,
//                              final boolean useOneBitCompression)
//            throws IOException {
//
//        final ImageIOWriter imageioWriter = new ImageIOWriter();
//        imageioWriter.setFormatName(codecName);
//        imageioWriter.setImageEncodeParam(encodeParam);
//        imageioWriter.write(fileName, imp, useOneBitCompression);
//    }

    /*
     *  Return typically used extension for given codec name.
     */
    private String getFileExtension(final String codecName) {
        if (codecName.compareToIgnoreCase(TIFF) == 0) {
            return "tif";
        } else if (codecName.compareToIgnoreCase(JPEG) == 0) {
            return "jpg";
        } else {
            return codecName.toLowerCase();
        }
    }
}
