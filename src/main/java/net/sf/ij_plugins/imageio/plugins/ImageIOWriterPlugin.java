/*
 *  IJ Plugins
 *  Copyright (C) 2002-2020 Jarek Sacha
 *  Author's email: jpsacha at gmail.com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Latest release available at https://github.com/ij-plugins/ijp-imageio
 */

package net.sf.ij_plugins.imageio.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import net.sf.ij_plugins.imageio.IJImageIO;
import net.sf.ij_plugins.imageio.IJImageIOException;
import net.sf.ij_plugins.imageio.IJImageOUtils;
import net.sf.ij_plugins.imageio.TiffMetaDataFactory;

import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import java.io.File;
import java.util.List;
import java.util.Optional;

import static net.sf.ij_plugins.imageio.IJImageOUtils.isBinary;
import static net.sf.ij_plugins.imageio.impl.ImageIOWriter.askForCompressionParams;


/**
 * Opens file chooser dialog and reads images using {@link net.sf.ij_plugins.imageio.IJImageIO}.
 *
 * @author Jarek Sacha
 */
public class ImageIOWriterPlugin implements PlugIn {

    private static final String TITLE = "IJP-ImageIO Save As";

    /**
     * Main processing method for the ImageIOWriterPlugin object.
     */
    public void run(final String codecName) {

        final ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        switch (codecName.toUpperCase()) {
            case "PNG":
                saveAs(imp, "Save As PNG", "PNG", ".png", null, null);
                break;
            case "PNM":
                saveAs(imp, "Save As PNM", "PNM", ".pnm", null, null);
                break;
            case "TIFF":
                saveAs(imp, "Save As TIFF", "TIFF", ".tif", "ZLib", TiffMetaDataFactory.createFrom(imp));
                break;
            case "JPEG2000":
                saveAs(imp, "Save As JPEG 2000", "JPEG2000", ".jp2", "JPEG2000", null);
                break;
            default:
                saveAs(codecName);
        }
    }

    private static void saveAs(final String codecName) {
        IJ.showStatus("Starting \"" + TITLE + "\" plugins...");

        // Check if there is an image to save
        final ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        final File file;
        {
            Optional<File> fileOpt = askForFile("Save As " + codecName + "...", imp.getTitle(), "." + codecName);
            if (fileOpt.isPresent()) {
                file = fileOpt.get();
            } else {
                return;
            }
        }

        try {
            IJImageIO.write(imp, file, codecName);
            imp.setTitle(file.getName());
        } catch (final IJImageIOException e) {
            IJ.error(TITLE, e.getMessage());
        }
    }

    private static void saveAs(final ImagePlus imp,
                               final String title,
                               final String formatName, final String formatExtension,
                               final String defaultCompression,
                               final IIOMetadata metadata) {

        IJ.showStatus("Starting \"" + title + "\" plugins...");


        final List<ImageWriter> writers = IJImageOUtils.getImageWritersByFormatName(formatName);
        if (writers.isEmpty()) {
            IJ.error("No " + formatName + " writers available");
            return;
        }

        final ImageWriter writer = writers.get(0);

        if (!writer.canWriteSequence() && imp.getNSlices() > 1) {
            IJ.error(title, formatName + " can save only images with a single slice.");
            return;
        }

        final File file;
        {
            final Optional<File> fileOpt = askForFile(title, imp.getTitle(), formatExtension);
            if (fileOpt.isPresent()) {
                file = fileOpt.get();
            } else {
                return;
            }
        }

        final boolean useOneBitCompression;
        useOneBitCompression = "TIFF".equalsIgnoreCase(formatName)
                && isBinary(imp)
                && IJ.showMessageWithCancel("Save as TIFF",
                "Image seems to be two level binary. Do you want to save it using 1 bit per pixel?");

        final Optional<ImageWriteParam> writerParamOpt = askForCompressionParams(writer, title, defaultCompression);
        if (writerParamOpt.isEmpty()) {
            return;
        }
        final ImageWriteParam writerParam = writerParamOpt.get();

        // Write the image to a file
        IJ.showStatus("Writing image as " + formatName + " to " + file.getAbsolutePath());
        try {
            IJImageIO.write(imp, file, writer, metadata, writerParam, useOneBitCompression);
            imp.setTitle(file.getName());
        } catch (final IJImageIOException e) {
            e.printStackTrace();
            Macro.abort();
            String msg = "Error writing file: " + file.getAbsolutePath() + ".\n\n";
            msg += (e.getMessage() == null) ? e.toString() : e.getMessage();
            IJ.showMessage(title, msg);
        }

        IJ.showStatus("Done writing image as " + formatName + " to " + file.getAbsolutePath());
    }

    private static Optional<File> askForFile(final String title, final String defaultName, final String extension) {
        final SaveDialog saveDialog = new SaveDialog(title, defaultName, extension);

        // Make only single call to saveDialog.getFileName(). When recording a macro,
        // each call records path in a macro (ImageJ 1.33k)
        final String saveDialogFileName = saveDialog.getFileName();
        if (saveDialogFileName == null) {
            return Optional.empty();
        }
        final File file;
        final String directory = saveDialog.getDirectory();
        if (directory != null) {
            file = new File(directory, saveDialogFileName);
        } else {
            file = new File(saveDialogFileName);
        }

        return Optional.of(file);
    }


}
