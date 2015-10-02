/*
 * Image/J Plugins
 * Copyright (C) 2002-2015 Jarek Sacha
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
package net.sf.ij_plugins.imageio;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import net.sf.ij.jaiio.BufferedImageCreator;
import net.sf.ij.jaiio.ImagePlusCreator;
import net.sf.ij.jaiio.UnsupportedImageModelException;

import javax.imageio.*;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Helper class that for reading images using {@code javax.imageio} into ImageJ representation.
 *
 * @author Jarek Sacha
 */
public class IJImageIO {

    /**
     * Default constructor intentionally made private to prevent instantiation of the class.
     */
    private IJImageIO() {
    }


    /**
     * Read image from file using using {@code javax.imageio} and convert it to ImageJ representation. All
     * images contained in the file ill be read.
     *
     * @param file          input image file.
     * @param combineStacks if {@code true} series of images of the same type and size will be combined into stacks (single ImagePlus).
     * @return Array of images read from the file. If images are of the same type and size they will
     * be combined into a stack and the returned ImagePlus array will have a single element
     * with stack size equal to the number of images in the input file.
     * @throws IJImageIOException when images cannot be read or represented as ImagePlus.
     */
    public static ImagePlus[] read(final File file, final boolean combineStacks) throws IJImageIOException {
        return read(file, combineStacks, null);
    }

    /**
     * Read image from file using using {@code javax.imageio} and convert it to ImageJ representation. All
     * images contained in the file ill be read.
     *
     * @param file          input image file.
     * @param combineStacks if {@code true} series of images of the same type and size will be combined into stacks (single ImagePlus).
     * @param pageIndex     index of pages to read from the file. if {@code null} all pages will be read.
     * @return Array of images read from the file. If images are of the same type and size they will
     * be combined into a stack and the returned ImagePlus array will have a single element
     * with stack size equal to the number of images in the input file.
     * @throws IJImageIOException when images cannot be read or represented as ImagePlus.
     */
    public static ImagePlus[] read(final File file, final boolean combineStacks, final int[] pageIndex) throws IJImageIOException {

        // Load images
        final List<BufferedImage> bufferedImages = readAsBufferedImages(file, pageIndex);

        // Convert to ImageJ representation
        final List<ImagePlus> images = new ArrayList<ImagePlus>();
        for (final BufferedImage bi : bufferedImages) {
            final ImagePlus imp;
            try {
                imp = ImagePlusCreator.create(file.getName(), bi);
            } catch (final UnsupportedImageModelException e) {
                throw new IJImageIOException("Unable to convert loaded image to ImagePlus. " + e.getMessage(), e);
            }
            // Add converted to the list
            images.add(imp);
        }

        return combineStacks
                ? attemptToCombineStacks(images)
                : images.toArray(new ImagePlus[images.size()]);
    }


    /**
     * Read image from file using using javax.imageio and convert it to ImageJ representation. All
     * images contained in the file will be read, and stacks combined.
     * Convenience call to read(file, true) ({@link #read(java.io.File, boolean)} ).
     *
     * @param file input image file.
     * @return Array of images read from the file. If images are of the same type and size they will
     * be combined into a stack and the returned ImagePlus array will have a single element
     * with stack size equal to the number of images in the input file.
     * @throws IJImageIOException when images cannot be read or represented as ImagePlus.
     * @see #read(java.io.File, boolean)
     */
    public static ImagePlus[] read(final File file) throws IJImageIOException {
        return read(file, true);
    }


    /**
     * Read image from file using using {@code javax.imageio} and convert it to ImageJ representation. All
     * images contained in the file ill be read.
     *
     * @param file input image file.
     * @return Array of images read from the file. If images are of the same type and size they will
     * be combined into a stack and the returned ImagePlus array will have a single element
     * with stack size equal to the number of images in the input file.
     * @throws IJImageIOException when I/O error occurs.
     */
    public static List<BufferedImage> readAsBufferedImages(final File file) throws IJImageIOException {
        return readAsBufferedImages(file, null);
    }

    /**
     * Read image from file using using {@code javax.imageio} and convert it to ImageJ representation. All
     * images contained in the file ill be read.
     *
     * @param file      input image file.
     * @param pageIndex index of pages to read from the file. if {@code null} all pages will be read.
     * @return Array of images read from the file. If images are of the same type and size they will
     * be combined into a stack and the returned ImagePlus array will have a single element
     * with stack size equal to the number of images in the input file.
     * @throws IJImageIOException when I/O error occurs.
     */
    public static List<BufferedImage> readAsBufferedImages(final File file, final int[] pageIndex) throws IJImageIOException {

        if (file == null) {
            throw new IllegalArgumentException("Argument 'file' cannot be null.");
        }

        final ImageInputStream iis = createImageInputStream(file);

        try {
            // Locate all available readers
            final List<ImageReader> readerList = getImageReaderList(iis);

            // Try available readers till one of them reads images with no errors
            final StringBuilder errorBuffer = new StringBuilder();
            List<BufferedImage> bufferedImages = null;
            for (int i = 0; bufferedImages == null && i < readerList.size(); i++) {
                final ImageReader reader = readerList.get(i);
                IJImageIO.logDebug("Using reader: " + reader.getClass().getName());
                try {
                    bufferedImages = read(reader, iis, pageIndex);
                } catch (final Exception ex) {
                    errorBuffer.append(reader.getClass().getName()).append(": ").append(ex.getMessage()).append("\n");
                }
            }

            if (bufferedImages != null) {
                return bufferedImages;
            } else {
                throw new IJImageIOException("Unable to read images from file: " + file.getAbsoluteFile() + ". " + errorBuffer.toString());
            }
        } finally {
            try {
                iis.close();
            } catch (final IOException e) {
                final String message = "Failed to close image input stream. " + e.getMessage();
                e.printStackTrace();
                logDebug(message);
            }
        }
    }

    private static List<ImageReader> getImageReaderList(ImageInputStream iis) throws IJImageIOException {
        final Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
        final List<ImageReader> readerList = new ArrayList<ImageReader>();
        while (readers.hasNext()) {
            final ImageReader reader = readers.next();
            readerList.add(reader);
        }

        // Verify that there is at least one reader available.
        if (readerList.isEmpty()) {
            throw new IJImageIOException("Input file format not supported: Cannot find proper image reader.");
        }
        return readerList;
    }

    /**
     * IJImageIOException
     * Read only the first image in the <code>file</code>.
     *
     * @param file Image file.
     * @return ImageInfo object.
     * @throws IJImageIOException In case of I/O error.
     */
    public static ImageInfo readPreviewAndInfo(final File file) throws IJImageIOException {


        if (file == null) {
            throw new IllegalArgumentException("Argument 'file' cannot be null.");
        }

        final ImageInputStream iis = createImageInputStream(file);

        try {
            // Locate all available readers
            final List<ImageReader> readerList = getImageReaderList(iis);

            // Try available readers till one of them reads images with no errors
            final StringBuffer errorBuffer = new StringBuffer();
            List<BufferedImage> bufferedImages = null;
            ImageInfo imageInfo = null;
            for (int i = 0; bufferedImages == null && i < readerList.size(); i++) {
                final ImageReader reader = readerList.get(i);
                IJImageIO.logDebug("Using reader: " + reader.getClass().getName());
                try {
                    imageInfo = readInfo(reader, iis);
                } catch (final Exception ex) {
                    errorBuffer.append(reader.getClass().getName()).append(": ").append(ex.getMessage()).append("\n");
                }
            }

            if (imageInfo != null) {
                return imageInfo;
            } else {
                throw new IJImageIOException("Unable to read images from file: " + file.getAbsoluteFile() + ". " + errorBuffer.toString());
            }
        } finally {
            try {
                iis.close();
            } catch (final IOException e) {
                final String message = "Failed to close image input stream. " + e.getMessage();
                e.printStackTrace();
                logDebug(message);
            }
        }

    }

    private static ImageInputStream createImageInputStream(File file) throws IJImageIOException {
        final ImageInputStream iis;
        try {
            iis = ImageIO.createImageInputStream(file);
        } catch (final IOException e) {
            throw new IJImageIOException("Failed to create image input stream for file: " + file.getAbsolutePath() + ". "
                    + e.getMessage(), e);
        }
        if (iis == null) {
            throw new IJImageIOException("Failed to create image input stream for file: " + file.getAbsolutePath() + ".");
        }
        return iis;
    }


    public static boolean write(final ImagePlus imp, final String formatName, final File file, final boolean preferBinary) throws IJImageIOException {

        final BufferedImage bi = BufferedImageCreator.create(imp, 0, preferBinary);
        try {
            return ImageIO.write(bi, formatName, file);
        } catch (final IOException e) {
            throw new IJImageIOException("Unable to write image file :" + file.getAbsolutePath()
                    + "\n" + e.getMessage(), e);
        }
    }


    public static void write(final BufferedImage image,
                             final ImageWriter writer,
                             final File file,
                             final ImageWriteParam parameters,
                             final IIOMetadata metadata) throws IOException {
        final ImageOutputStream outputStream = new FileImageOutputStream(file);
        try {
            writer.setOutput(outputStream);

            final IIOImage iioImage = new IIOImage(image, null, metadata);

            // Write image
            writer.write(null, iioImage, parameters);
        } finally {
            outputStream.close();
        }
    }


    private static List<BufferedImage> read(final ImageReader reader,
                                            final ImageInputStream iis,
                                            int[] pageIndex)
            throws IJImageIOException {

        //                iis.reset();
        try {
            iis.seek(0);
        } catch (final IOException e) {
            throw new IJImageIOException("Unable to reset input stream to position 0. ", e);
        }
        reader.setInput(iis, false, false);

        // How many images are in the file and what is the first image index
        final int numImages;
//        reader.addIIOReadProgressListener(new ProgressListener(numImages));
        try {
            numImages = reader.getNumImages(true);
        } catch (final IOException e) {
            throw new IJImageIOException("Failed to retrieve number of images in the file. ", e);
        }
        final int minIndex = reader.getMinIndex();

        if (pageIndex == null) {
            pageIndex = new int[numImages - minIndex];
            for (int i = minIndex; i < numImages; ++i) {
                pageIndex[i] = i;
            }
        }

        // Read each image and add it to list 'images'
        final List<BufferedImage> images = new ArrayList<BufferedImage>();
        for (int i = 0; i < pageIndex.length; i++) {
            IJ.showProgress(i, pageIndex.length);

            final BufferedImage bi;
            try {
                bi = reader.read(i);
            } catch (final IOException e) {
                throw new IJImageIOException("Error reading image with internal index " + i
                        + ". Min internal index is " + minIndex + ". ", e);
            }

//            // Read metadata for this image
//            final ImageReadParam imageReadParam = reader.getDefaultReadParam();
//            final IIOImage a = reader.readAll(j, imageReadParam);
//            final IIOMetadata metadata = a.getMetadata();

            images.add(bi);
            IJ.showProgress(i + 1, pageIndex.length);
        }

        return images;
    }

    private static ImageInfo readInfo(final ImageReader reader,
                                      final ImageInputStream iis)
            throws IJImageIOException {

        //                iis.reset();
        try {
            iis.seek(0);
        } catch (final IOException e) {
            throw new IJImageIOException("Unable to reset input stream to position 0. ", e);
        }
        reader.setInput(iis, false, false);

        final ImageInfo imageInfo = new ImageInfo();
        try {
            imageInfo.numberOfPages = reader.getNumImages(true);
            imageInfo.codecName = reader.getFormatName();
            if (reader.hasThumbnails(0)) {
                imageInfo.previewImage = reader.readThumbnail(0, 0);
            } else {
                imageInfo.previewImage = reader.read(0);
            }
        } catch (final IOException ex) {
            throw new IJImageIOException(ex);
        }

        return imageInfo;
    }


    /**
     * Attempts to combine images on the list into a stack.
     * Images cannot be combined if they are of different types or different sizes.
     *
     * @param imageList List of images to combine into a stack.
     * @return Combined image stacks.
     */
    private static ImagePlus[] attemptToCombineStacks(final List<ImagePlus> imageList) {

        final List<ImagePlus> result = new ArrayList<ImagePlus>();
        int sourceIndex = 0;
        while (sourceIndex < imageList.size()) {
            // Test how many images can be combined
            final int chainLength = stackableChain(imageList, sourceIndex);

            // Combine
            final ImagePlus imp = imageList.get(sourceIndex);
            if (chainLength > 1) {
                final ImageStack stack = imp.getStack();
                for (int i = sourceIndex + 1; i < sourceIndex + chainLength; ++i) {
                    final ImageStack s2 = imageList.get(i).getStack();
                    for (int s2i = 1; s2i <= s2.getSize(); s2i++) {
                        stack.addSlice(s2.getSliceLabel(s2i), s2.getProcessor(s2i));
                    }
                }
                imp.setStack(imp.getTitle(), stack);
            }

            // Add to output
            result.add(imp);
            sourceIndex += chainLength;
        }

        return result.toArray(new ImagePlus[result.size()]);
    }


    private static int stackableChain(final List<ImagePlus> imageList, final int startIndex) {
        if (imageList.size() <= startIndex || startIndex < 0) {
            return 0;
        }

        if (imageList.size() == startIndex + 1) {
            return 1;
        }

        final ImagePlus firstImage = imageList.get(startIndex);
        final int fileType = firstImage.getFileInfo().fileType;
        final int w = firstImage.getWidth();
        final int h = firstImage.getHeight();
        int count = 1;
        for (int i = startIndex + 1; i < imageList.size(); i++) {
            final ImagePlus imp = imageList.get(i);
            if (fileType == imp.getFileInfo().fileType && w == imp.getWidth() && h == imp.getHeight()) {
                count++;
            } else {
                break;
            }

        }

        return count;
    }


    /**
     * Helper method to print log message using {@link ij.IJ#log} when {@link ij.IJ#debugMode} is
     * set to <code>true</code>.
     *
     * @param message log message
     */
    private static void logDebug(final String message) {
        if (IJ.debugMode) {
            IJ.log(message);
        }
    }


    private static final class ProgressListener implements IIOReadProgressListener {

        final private int numberOfImages;


        private ProgressListener(int numberOfImages) {
            this.numberOfImages = numberOfImages;
        }


        @Override
        public void sequenceStarted(ImageReader source, int minIndex) {
            System.out.println("IJImageIO.sequenceStarted");
        }


        @Override
        public void sequenceComplete(ImageReader source) {
            System.out.println("IJImageIO.sequenceComplete");
        }


        @Override
        public void imageStarted(ImageReader source, int imageIndex) {
            System.out.println("IJImageIO.imageStarted: " + imageIndex);
        }


        @Override
        public void imageProgress(ImageReader source, float percentageDone) {
            System.out.println("IJImageIO.imageProgress: " + percentageDone);
        }


        @Override
        public void imageComplete(ImageReader source) {
            System.out.println("IJImageIO.imageComplete");
        }


        @Override
        public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {
            System.out.println("IJImageIO.thumbnailStarted");
        }


        @Override
        public void thumbnailProgress(ImageReader source, float percentageDone) {
            System.out.println("IJImageIO.thumbnailProgress: " + percentageDone);
        }


        @Override
        public void thumbnailComplete(ImageReader source) {
            System.out.println("IJImageIO.thumbnailComplete");
        }


        @Override
        public void readAborted(ImageReader source) {
            System.out.println("IJImageIO.readAborted");
        }
    }

    /*
     *  Basic image information including first image in the file.
     */
    public static class ImageInfo {
        public Image previewImage;
        public int numberOfPages;
        public String codecName;
    }


}
