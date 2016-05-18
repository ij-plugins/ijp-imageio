/*
 * Image/J Plugins
 * Copyright (C) 2002-2016 Jarek Sacha
 * Author's email: jpsacha at gmail.com
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
import net.sf.ij_plugins.imageio.IJImageIO;
import net.sf.ij_plugins.imageio.IJImageIOException;
import net.sf.ij_plugins.imageio.TiffMetaDataFactory;
import net.sf.ij_plugins.imageio.impl.EncoderParamDialog;
import net.sf.ij_plugins.imageio.impl.ImageFileChooserFactory;
import net.sf.ij_plugins.imageio.impl.ImageIOWriterFileFilter;
import net.sf.ij_plugins.imageio.impl.SwingUtils;

import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static net.sf.ij_plugins.imageio.IJImageOUtils.isBinary;
import static net.sf.ij_plugins.imageio.impl.ImageIOWriter.askForCompressionParams;

/**
 * @author Jarek Sacha
 */
public final class ImageIOSaveAsPlugin implements PlugIn {

    public static final String PNG = "png";
    public static final String PNM = "pnm";
    public static final String TIFF = "tiff";
    public static final String JPEG = "jpeg";
    private static final String TITLE = "IJP-ImageIO Save As ...";
    private static JFileChooser _fileChooser;

    public void run(final String arg) {
        IJ.showStatus("Starting \"" + TITLE + "\" plugins...");

        // Check if there is an image to save
        final ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        try {

            // Ask for file
            final FileChooserResult fcSelection;
            {
                final Optional<FileChooserResult> fcSelectionOpt = askForFile(imp.getTitle());
                if (fcSelectionOpt.isPresent()) {
                    fcSelection = fcSelectionOpt.get();
                } else {
                    return;
                }
            }

            // Create writer
            final File file = fcSelection.file;
            final ImageWriterSpi spi = fcSelection.spi;
            final ImageWriter writer;
            try {
                writer = spi.createWriterInstance();
            } catch (IOException e) {
                throw new IJImageIOException(
                        "Error creating image writer '" + spi.getDescription(Locale.US) + "'. " + e.getMessage());
            }

            // Ask for file options
            final WriterOptions writerOptions;
            {
                final Optional<WriterOptions> writerOptionsOpt = askForWriterOptions(imp, writer);
                if (!writerOptionsOpt.isPresent()) {
                    return;
                } else {
                    writerOptions = writerOptionsOpt.get();
                }
            }

            // Write the image to a file
            IJ.showStatus("Writing image as " + spi.getDescription(Locale.US) + " to " + file.getName());
            try {
                if (IJ.debugMode) {
                    IJ.log("Saving using SPI   : " + spi.getPluginClassName());
                    IJ.log("Saving using SPI   : " + spi.getClass().getCanonicalName());
                    IJ.log("Saving using writer: " + writer.getClass().getCanonicalName());
                }
                IJImageIO.write(imp, file, writer, writerOptions.metadata, writerOptions.param,
                        writerOptions.useOneBitCompression);
            } catch (final IJImageIOException e) {
                throw new IJImageIOException(
                        "Error writing file: " + file.getAbsolutePath() + ".\n\n" + Objects.toString(e.getMessage()),
                        e);
            }
        } catch (IJImageIOException ex) {
            ex.printStackTrace();
            IJ.showMessage(TITLE, ex.getMessage());
        } finally {
            IJ.showStatus("");
        }
    }

    private Optional<FileChooserResult> askForFile(final String imageTitle) {

        // Get fileName and codecName showing save dialog
        final String dirName = OpenDialog.getDefaultDirectory();
        fileChooser().setCurrentDirectory(new File(dirName != null ? dirName : "."));

        File file;
        if (imageTitle != null) {
            file = new File(imageTitle);
            fileChooser().setSelectedFile(file);
        } else {
            IJ.error("Image title cannot be null");
            return Optional.empty();
        }

        if (fileChooser().showSaveDialog(IJ.getInstance()) != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        OpenDialog.setDefaultDirectory(fileChooser().getSelectedFile().getParentFile().getAbsolutePath());

        final ImageWriterSpi spi;
        final FileFilter fileFilter = fileChooser().getFileFilter();
        if (fileFilter instanceof ImageIOWriterFileFilter) {
            final ImageIOWriterFileFilter imageFileFilter = (ImageIOWriterFileFilter) fileFilter;
            spi = imageFileFilter.getSPI();
        } else {
            IJ.log(TITLE + " - Internal error unexpected FileChooser filter: " + fileFilter);
            spi = null;
        }

        if (spi == null) {
            IJ.showMessage(TITLE, "File format not selected. File not saved.");
            return Optional.empty();
        }

        file = fileChooser().getSelectedFile();

        return Optional.of(new FileChooserResult(file, spi));
    }

    private JFileChooser fileChooser() {
        if (_fileChooser == null) {
            _fileChooser = ImageFileChooserFactory.createJAISaveChooser();
            final String dirName = OpenDialog.getDefaultDirectory();
            _fileChooser.setCurrentDirectory(new File(dirName != null ? dirName : "."));
        }
        return _fileChooser;
    }

    private Optional<WriterOptions> askForWriterOptions(final ImagePlus imp, final ImageWriter writer) {

        final String spiPluginClassName = writer.getOriginatingProvider().getPluginClassName();
        final ImageWriteParam writerParam;
        final boolean useOneBitCompression;
        final IIOMetadata metadata;
        if (spiPluginClassName != null && spiPluginClassName.endsWith(".TIFFImageWriter")) {

            // Detect if image is binary and give an option to save as 1bit compressed image
            useOneBitCompression = isBinary(imp) &&
                    IJ.showMessageWithCancel(
                            "Save as TIFF",
                            "Image seems to be two level binary. Do you want to save it using 1 bit per pixel?");

            final EncoderParamDialog paramDialog = new EncoderParamDialog(useOneBitCompression);
            SwingUtils.centerOnScreen(paramDialog, false);
            paramDialog.setVisible(true);
            if (!paramDialog.isAccepted()) {
                Macro.abort();
                IJ.showMessage(TITLE, "Option dialog cancelled, image not saved.");
                return Optional.empty();
            }

            writerParam = paramDialog.getImageWriteParam(useOneBitCompression);
            metadata = TiffMetaDataFactory.createFrom(imp);
        } else {
            final Optional<ImageWriteParam> writerParamOpt = askForCompressionParams(writer, TITLE, null);
            if (!writerParamOpt.isPresent()) {
                return Optional.empty();
            }
            writerParam = writerParamOpt.get();
            useOneBitCompression = false;
            metadata = null;
        }

        return Optional.of(new WriterOptions(writerParam, useOneBitCompression, metadata));
    }

    private class FileChooserResult {
        final public File file;
        final public ImageWriterSpi spi;

        FileChooserResult(final File file, final ImageWriterSpi spi) {
            this.file = file;
            this.spi = spi;
        }
    }

    private class WriterOptions {
        final ImageWriteParam param;
        final boolean useOneBitCompression;
        final IIOMetadata metadata;

        WriterOptions(final ImageWriteParam param, final boolean useOneBitCompression, final IIOMetadata metadata) {
            this.param = param;
            this.useOneBitCompression = useOneBitCompression;
            this.metadata = metadata;
        }
    }
}
