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
package net.sf.ij_plugins.imageio;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import net.sf.ij.jaiio.BufferedImageCreator;
import net.sf.ij.jaiio.ImagePlusCreator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class that for reading images using javax.imageio into ImageJ representation.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.6 $
 */
public class IJImageIO {
    /**
     * Default constructor intentionally made private to prevent instantiation of the class.
     */
    private IJImageIO() {
    }

    /**
     * Read image from file using using javax.imageio and convert it to ImageJ representation. All
     * images contained in the file ill be read.
     *
     * @param file input image file.
     * @return Array of images read from the file. If images are of the same type and size they will
     *         be combined into a stack and the returned ImagePlus array will have a single element
     *         with stack size equal to the number of images in the input file.
     * @throws IOException
     * @throws IJImageIOException
     */
    public static ImagePlus[] read(final File file)
            throws IOException, IJImageIOException {

        if (file == null) {
            throw new IllegalArgumentException("Argument 'file' cannot be null.");
        }

        final ImageInputStream iis = ImageIO.createImageInputStream(file);

        // Locate all available readers
        final Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
        final List<ImageReader> readerList = new ArrayList<ImageReader>();
        while (readers.hasNext()) {
            final ImageReader reader = readers.next();
            readerList.add(reader);
        }

        // Verify that at least one reader available.
        if (readerList.isEmpty()) {
            throw new IJImageIOException("Input file format not supported: Cannot find proper image reader.");
        }

        final StringBuffer errorBuffer = new StringBuffer();
        for (final ImageReader reader : readerList) {
            IJImageIO.logDebug("Using reader: " + reader.getClass().getName());
            try {
                //                iis.reset();
                iis.seek(0);
                reader.setInput(iis, false, false);

                // How many images are in the file and what is the first image index
                final int numImages = reader.getNumImages(true);
                final int minIndex = reader.getMinIndex();

                // Read each image and add it to list 'images'
                final List<ImagePlus> images = new ArrayList<ImagePlus>(numImages);
                for (int j = minIndex; j < numImages + minIndex; j++) {
                    // Read using javax.imageio
                    final BufferedImage bi = reader.read(j);
                    // Convert to ImageJ representation
                    final ImagePlus imp = ImagePlusCreator.create(file.getName(), bi.getRaster(), bi.getColorModel());
                    // Add converted to the list
                    images.add(imp);
                }

                // If images on the list are of the same type and size combine them into a stack.
                final ImagePlus imp = attemptToCombineImages(images);

                // Prepare output image array 'imps'.
                return imp != null
                        ? new ImagePlus[]{imp}
                        : images.toArray(new ImagePlus[numImages]);
            } catch (Exception ex) {
                errorBuffer.append(reader.getClass().getName()).append(": ").append(ex.getMessage()).append("\n");
            }
        }

        throw new IJImageIOException("Input file format not supported: Cannot find proper image reader.\n"
                + errorBuffer.toString());

    }

    public static boolean write(final ImagePlus imp, final String formatName, final File file, boolean prefferBinary) throws IJImageIOException {

        final BufferedImage bi = BufferedImageCreator.create(imp, 0, prefferBinary);
        try {
            return ImageIO.write(bi, formatName, file);
        } catch (IOException e) {
            throw new IJImageIOException("Unable to write image file :" + file.getAbsolutePath()
                    + "\n" + e.getMessage(), e);
        }
    }


    /**
     * Attempts to combine images on the list into a stack. If successful return the combined image,
     * otherwise return null. Images cannot be combined if they are of different types,  different
     * sizes, or have more then single slice. Can return <code>null</code>.
     *
     * @param imageList List of images to combine into a stack.
     * @return Combined image if successful, otherwise <code>null</code>.
     */
    private static ImagePlus attemptToCombineImages(final List<ImagePlus> imageList) {
        if (imageList == null || imageList.size() < 1)
            return null;

        if (imageList.size() == 1) {
            return imageList.get(0);
        }

        final ImagePlus firstImage = imageList.get(0);
        if (firstImage.getStackSize() != 1) {
            return null;
        }

        final int fileType = firstImage.getFileInfo().fileType;
        final int w = firstImage.getWidth();
        final int h = firstImage.getHeight();
        final ImageStack stack = firstImage.getStack();
        for (int i = 1; i < imageList.size(); ++i) {
            final ImagePlus im = imageList.get(i);
            if (im.getStackSize() != 1) {
                return null;
            }
            if (fileType == im.getFileInfo().fileType
                    && w == im.getWidth() && h == im.getHeight()) {
                stack.addSlice(im.getTitle(), im.getProcessor().getPixels());
            } else {
                return null;
            }
        }

        firstImage.setStack(firstImage.getTitle(), stack);
        return firstImage;
    }

    /**
     * Helper method to print log message using {@link ij.IJ#log} when {@link ij.IJ#debugMode} is
     * set to <code>true</code>.
     */
    static void logDebug(final String message) {
        if (IJ.debugMode) {
            IJ.log(message);
        }
    }

}
