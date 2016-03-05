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
import ij.gui.GenericDialog;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import net.sf.ij_plugins.imageio.BufferedImageFactory;
import net.sf.ij_plugins.imageio.IJImageIO;
import net.sf.ij_plugins.imageio.IJImageIOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;


/**
 * Opens file chooser dialog and reads images using {@link net.sf.ij_plugins.imageio.IJImageIO}.
 *
 * @author Jarek Sacha
 */
public class ImageIOWriterPlugin implements PlugIn {

    private static final String TITLE = "Image IO Save As";

    /**
     * Main processing method for the ImageIOWriterPlugin object.
     */
    public void run(final String codecName) {

        if ("PNG".equalsIgnoreCase(codecName)) {
            saveAs("Save As PNG", "PNG", ".png", null);
        } else if ("PNM".equalsIgnoreCase(codecName)) {
            saveAs("Save As PNM", "PNM", ".pnm", null);
        } else if ("TIFF".equalsIgnoreCase(codecName)) {
            saveAs("Save As TIFF", "TIFF", ".tif", "LZW");
        } else if ("JPEG2000".equalsIgnoreCase(codecName)) {
            saveAs("Save As JPEG 2000", "JPEG2000", ".jp2", "JPEG2000");
        } else {
            saveAs(codecName);
        }
    }

    private static void saveAs(final String codecName) {
        IJ.showStatus("Starting \"" + TITLE + "\" plugins...");

        final ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        final File file = askForFile("Save As " + codecName + "...", imp.getTitle(), "." + codecName);
        if (file == null) {
            return;
        }

        try {
            // TODO support preference for binary images
//            final boolean ok = IJImageIO.write(imp, codecName, file, true);
//            if (!ok) {
//                throw new IJImageIOException("Writer for format '" + codecName + "' not available.");
//            }
            IJImageIO.write(imp, file, codecName);
        } catch (final IJImageIOException e) {
            IJ.error(TITLE, e.getMessage());
        }
    }

    private static void saveAs(final String title,
                               final String formatName, final String formatExtension,
                               final String defaultCompression) {

        IJ.showStatus("Starting \"" + title + "\" plugins...");

        final ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }


        final ImageWriter writer = findWriter(formatName);
        if (writer == null) {
            IJ.error("No " + formatName + " writers available");
            return;
        }

        if (!writer.canWriteSequence() && imp.getNSlices() > 1) {
            IJ.error(title, formatName + " can save only images with a single slice.");
            return;
        }

        boolean useOneBitCompression = false;

        final File file = askForFile(title, imp.getTitle(), formatExtension);
        if (file == null) {
            return;
        }


        final ImageWriteParam writerParam = writer.getDefaultWriteParam();
        if (writerParam != null && writerParam.canWriteCompressed()) {
            writerParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            final String[] compressionTypes = writerParam.getCompressionTypes();
            if (compressionTypes != null && compressionTypes.length > 1) {
                GenericDialog dialog = new GenericDialog(title);
                dialog.addChoice("Compression type", compressionTypes, defaultCompression);
                dialog.showDialog();

                if (dialog.wasCanceled()) {
                    return;
                }

                writerParam.setCompressionType(dialog.getNextChoice());
            }
        }


        if (imp.getNSlices() <= 1) {
            writeSingle(imp, writer, file, writerParam, useOneBitCompression, formatName, title);
        } else {
            writeSequence(imp, writer, file, writerParam, null, useOneBitCompression, title);
        }


    }

    private static void writeSingle(final ImagePlus imp,
                                    final ImageWriter writer,
                                    final File file,
                                    final ImageWriteParam parameters,
                                    final boolean useOneBitCompression,
                                    final String formatName,
                                    final String title) {
        final BufferedImage bi = BufferedImageFactory.createFrom(imp, 0, useOneBitCompression);

        //
        // Now ready to write the image to a file
        //
        try {
            IJ.showStatus("Writing image as " + formatName + " to " + file.getAbsolutePath());
//            write(imp, file, "PNG", null, useOneBitCompression);
            IJImageIO.write(new BufferedImage[]{bi}, file, writer, null, parameters);
        } catch (final IJImageIOException e) {
            e.printStackTrace();
            Macro.abort();
            String msg = "Error writing file: " + file.getAbsolutePath() + ".\n\n";
            msg += (e.getMessage() == null) ? e.toString() : e.getMessage();
            IJ.showMessage(title, msg);
        }

    }

    private static void writeSequence(final ImagePlus imp,
                                      final ImageWriter writer,
                                      final File file,
                                      final ImageWriteParam parameters,
                                      final IIOMetadata metadata,
                                      final boolean useOneBitCompression,
                                      final String title) {
        try {
            final ImageOutputStream outputStream = new FileImageOutputStream(file);
            try {
                writer.setOutput(outputStream);
                writer.prepareWriteSequence(metadata);
                for (int i = 0; i < imp.getNSlices(); i++) {
                    final BufferedImage bi = BufferedImageFactory.createFrom(imp, i, useOneBitCompression);

                    final IIOImage iioImage = new IIOImage(bi, null, metadata);

                    // Write image
                    writer.writeToSequence(iioImage, parameters);
                }
                writer.endWriteSequence();
            } finally {
                outputStream.close();
            }
        } catch (final IOException e) {
            e.printStackTrace();
            Macro.abort();
            String msg = "Error writing file: " + file.getAbsolutePath() + ".\n\n";
            msg += (e.getMessage() == null) ? e.toString() : e.getMessage();
            IJ.showMessage(title, msg);
        }

    }

    private static File askForFile(final String title, final String defaultName, final String extension) {
        final SaveDialog saveDialog = new SaveDialog(title, defaultName, extension);

        // Make only single call to saveDialog.getFileName(). When recording a macro,
        // each call records path in a macro (ImageJ 1.33k)
        final String saveDialogFileName = saveDialog.getFileName();
        if (saveDialogFileName == null) {
            return null;
        }
        final File file;
        final String directory = saveDialog.getDirectory();
        if (directory != null) {
            file = new File(directory, saveDialogFileName);
        } else {
            file = new File(saveDialogFileName);
        }

        return file;
    }

    private static ImageWriter findWriter(final String codecName) {

        {
            final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(codecName);
            while (writers.hasNext()) {
                IJ.log("Writer: " + writers.next());

            }
        }
        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(codecName);
        return writers.hasNext() ? writers.next() : null;

    }


}
