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
package net.sf.ij_plugins.imageio;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import javax.imageio.*;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.*;


/**
 * Helper class that for easy reading of images using {@code javax.imageio} into ImageJ representation.
 * <p>
 * For example:
 * <pre>
 *     ImagePlus[] imps = IJImageIO.read(file);
 * </pre>
 *
 * @author Jarek Sacha
 */
public class IJImageIO {

    private final static boolean useOneBitCompressionDefault = BufferedImageFactory.useOneBitCompressionDefault;

    public static final String PREFERRED_SPI_VENDOR = "github.com/jai-imageio";

    public static class ImageAndMetadata {
        public final BufferedImage image;
        public final IIOMetadata metadata;

        public ImageAndMetadata(BufferedImage bi, IIOMetadata md) {
            this.image = bi;
            this.metadata = md;
        }
    }

    // TODO: Simplify API of this class, there are too many very similar looking methods for 'write'

    static {
        // Try to register all available ImageIO SPIs
        IIORegistry.getDefaultInstance().registerApplicationClasspathSpis();
    }

    /**
     * Default constructor intentionally made private to prevent instantiation of the class.
     */
    private IJImageIO() {
    }

    /**
     * Return array of strings representing all supported image file extension that this data set can read.
     *
     * @return array of supported file extension.
     */
    public static String[] supportedImageReaderExtensions() {
        final String[] formatNames = ImageIO.getReaderFormatNames();
        final Set<String> extensions = new TreeSet<>();
        for (final String formatName : formatNames) {
            final Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(formatName);
            while (readers.hasNext()) {
                final ImageReader reader = readers.next();
                final String[] suffixes = reader.getOriginatingProvider().getFileSuffixes();
                if (suffixes != null) {
                    for (final String suffix : suffixes) {
                        if (suffix != null && suffix.trim().length() > 0) {
                            extensions.add(suffix);
                        }
                    }
                }
            }
        }

        return extensions.toArray(new String[0]);
    }


    public static String[] supportedImageWriterExtensions() {
        final String[] formatNames = ImageIO.getWriterFormatNames();
        final Set<String> extensions = new TreeSet<>();
        for (final String formatName : formatNames) {
            final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
            while (writers.hasNext()) {
                final ImageWriter writer = writers.next();
                final String[] suffixes = writer.getOriginatingProvider().getFileSuffixes();
                if (suffixes != null) {
                    for (final String suffix : suffixes) {
                        if (suffix != null && suffix.trim().length() > 0) {
                            extensions.add(suffix);
                        }
                    }
                }
            }
        }

        return extensions.toArray(new String[0]);
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
    public static ImagePlus[] read(final File file,
                                   final boolean combineStacks) throws IJImageIOException {
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
    public static ImagePlus[] read(final File file,
                                   final boolean combineStacks,
                                   final int[] pageIndex) throws IJImageIOException {

        // FIXME: for TIFF images read description and decode stored information, like calibration, etc.

        // Load images
        final List<ImageAndMetadata> ims = readAsBufferedImages(file, pageIndex);

        // Convert to ImageJ representation
        final List<ImagePlus> images = new ArrayList<>();
        for (final ImageAndMetadata im : ims) {
            final ImagePlus imp;
            try {
                imp = ImagePlusFactory.create(file.getName(), im);
            } catch (final IJImageIOException e) {
                throw new IJImageIOException("Unable to convert loaded image to ImagePlus. " + e.getMessage(), e);
            }
            // Add converted to the list
            images.add(imp);
        }

        return combineStacks
                ? attemptToCombineStacks(images)
                : images.toArray(new ImagePlus[0]);
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
    public static List<ImageAndMetadata> readAsBufferedImages(final File file) throws IJImageIOException {
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
    public static List<ImageAndMetadata> readAsBufferedImages(final File file,
                                                              final int[] pageIndex) throws IJImageIOException {

        if (file == null) {
            throw new IllegalArgumentException("Argument 'file' cannot be null.");
        }

        final ImageInputStream iis = createImageInputStream(file);

        try {
            // Locate all available readers
            final List<ImageReader> readerList = getImageReaders(iis);

            // Try available readers till one of them reads images with no errors
            final StringBuilder errorBuffer = new StringBuilder();
            List<ImageAndMetadata> bufferedImages = null;
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
            final List<ImageReader> readerList = getImageReaders(iis);

            // Try available readers till one of them reads images with no errors
            final StringBuilder errorBuffer = new StringBuilder();
            ImageInfo imageInfo = null;
            for (final ImageReader reader : readerList) {
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


    public static void write(final ImagePlus imp,
                             final File file,
                             final ImageWriterSpi imageWriterSpi) throws IJImageIOException {

        write(imp, file, imageWriterSpi, useOneBitCompressionDefault);
    }

    public static void write(final ImagePlus imp,
                             final File file,
                             final ImageWriterSpi imageWriterSpi,
                             final boolean useOneBitCompression) throws IJImageIOException {


        final BufferedImage[] images = BufferedImageFactory.createFrom(imp, useOneBitCompression);
        write(images, file, imageWriterSpi, null);
    }

    public static void write(final ImagePlus imp,
                             final File file,
                             final ImageWriter writer,
                             final IIOMetadata metadata,
                             final ImageWriteParam parameters) throws IJImageIOException {


        final BufferedImage[] images = BufferedImageFactory.createFrom(imp);
        write(images, file, writer, metadata, parameters);
    }

    public static void write(final ImagePlus imp,
                             final File file,
                             final ImageWriter writer,
                             final IIOMetadata metadata,
                             final ImageWriteParam parameters,
                             final boolean useOneBitCompression) throws IJImageIOException {

        final BufferedImage[] bis = BufferedImageFactory.createFrom(imp, useOneBitCompression);
        write(bis, file, writer, metadata, parameters);
    }

    public static void write(ImagePlus imp,
                             File file,
                             String format) throws IJImageIOException {
        write(imp, file, format, useOneBitCompressionDefault);
    }

    public static void write(ImagePlus imp,
                             File file,
                             String format,
                             final boolean useOneBitCompression) throws IJImageIOException {
        List<ImageWriterSpi> spis = IJImageOUtils.writerSpiByFormatName(format);
        write(imp, file, spis.get(0), useOneBitCompression);
    }


    /**
     * Write image to a file using specified format.
     * Supported formats can be obtained calling {@link #supportedImageWriterExtensions()}.
     *
     * @param image  image to be saved.
     * @param file   file where to save the image.
     * @param format image format (extension)
     * @throws IJImageIOException writing fails or file format is not supported.
     */
    public static void write(final BufferedImage image,
                             final File file,
                             final String format) throws IJImageIOException {
        write(image, file, format, null);
    }


    /**
     * Write image to a file using specified format and also save the metadata if provided.
     * Supported formats can be obtained calling {@link #supportedImageWriterExtensions()}.
     *
     * @param images   images to be saved.
     * @param file     file where to save the image.
     * @param format   image format (extension)
     * @param metadata image meta data
     * @throws IJImageIOException writing fails or file format is not supported.
     */
    public static void write(final BufferedImage[] images,
                             final File file,
                             final String format,
                             final IIOMetadata metadata)
            throws IJImageIOException {

        Validate.notEmpty(images, "Argument 'image' cannot be null");
        Validate.notNull(file, "Argument 'file' cannot be null");
        Validate.notNull(format, "Argument 'format' cannot be null");

        List<ImageWriterSpi> spis = IJImageOUtils.writerSpiByFormatName(format);
        if (spis.isEmpty()) {
            throw new IJImageIOException("Cannot find writer for format: '" + format + "'.");
        }

        write(images, file, spis.get(0), metadata);
    }

    public static void write(final BufferedImage[] images,
                             final File file,
                             final ImageWriterSpi imageWriterSpi,
                             final IIOMetadata metadata)
            throws IJImageIOException {
        Validate.notEmpty(images, "Argument 'image' cannot be null");
        Validate.notNull(file, "Argument 'file' cannot be null");
        Validate.notNull(imageWriterSpi, "Argument 'format' cannot be null");


        final ImageWriter imageWriter;
        try {
            imageWriter = imageWriterSpi.createWriterInstance();
        } catch (IOException e) {
            throw new IJImageIOException("Failed to create image writer. " + e.getMessage(), e);
        }

        final ImageWriteParam parameters = imageWriter.getDefaultWriteParam();

        write(images, file, imageWriter, metadata, parameters);

    }

    public static void write(final BufferedImage[] images,
                             final File file,
                             final ImageWriter writer,
                             final IIOMetadata metadata,
                             final ImageWriteParam parameters)
            throws IJImageIOException {
        Validate.notEmpty(images, "Argument 'image' cannot be null");
        Validate.notNull(file, "Argument 'file' cannot be null");
        Validate.notNull(writer, "Argument 'format' cannot be null");


        try (ImageOutputStream outputStream = new FileImageOutputStream(file)) {

            writer.setOutput(outputStream);

            if (images.length <= 0) {
                throw new IllegalArgumentException("There are no input images to write");
            }


            if (images.length == 1) {
                final IIOImage iioImage = new IIOImage(images[0], null, metadata);
                writer.write(null, iioImage, parameters);
            } else {

                writer.prepareWriteSequence(metadata);
                for (BufferedImage image : images) {
                    final IIOImage iioImage = new IIOImage(image, null, metadata);

                    // Write image
                    writer.writeToSequence(iioImage, parameters);
                }
                writer.endWriteSequence();
            }

        } catch (final FileNotFoundException ex) {
            throw new IJImageIOException("Error creating file output stream '" + file.getAbsolutePath() + ". "
                    + ex.getMessage(), ex);

        } catch (final IOException ex) {
            throw new IJImageIOException("Error writing image to file '" + file.getAbsolutePath() + ". "
                    + ex.getMessage(), ex);
        }

    }

    /**
     * Write image to a file using specified format and also save the metadata if provided.
     * Supported formats can be obtained calling {@link #supportedImageWriterExtensions()}.
     *
     * @param image    image to be saved.
     * @param file     file where to save the image.
     * @param format   image format (extension)
     * @param metadata image meta data
     * @throws IJImageIOException writing fails or file format is not supported.
     */
    public static void write(final BufferedImage image,
                             final File file,
                             final String format,
                             final IIOMetadata metadata)
            throws IJImageIOException {
        write(new BufferedImage[]{image}, file, format, metadata);
    }


    /**
     * Write image in TIFF format with ZLib compression using ImageIO
     *
     * @param file  File to save to.
     * @param image Image to save.
     * @throws IJImageIOException writing fails or file format is not supported.
     */
    public static void writeAsTiff(final ImagePlus image,
                                   final File file) throws IJImageIOException {

        writeAsTiff(image, file, "ZLib");
    }

    /**
     * Write image in TIFF format using ImageIO
     *
     * @param file        File to save to.
     * @param image       Image to save.
     * @param compression TIFF compression type, for instance, "ZLib", "LZW".
     *                    If empty or {@code none}, no compression is used
     * @throws IJImageIOException writing fails or file format is not supported.
     * @see IJImageIO#getTIFFCompressionTypes()
     */
    public static void writeAsTiff(final ImagePlus image,
                                   final File file,
                                   final String compression) throws IJImageIOException {

        final BufferedImage[] images = BufferedImageFactory.createFrom(image);
        final IIOMetadata metadata = TiffMetaDataFactory.createFrom(image);
        writeAsTiff(images, file, compression, metadata);
    }

    /**
     * Write image in TIFF format using ImageIO
     *
     * @param file        File to save to.
     * @param images      Images to save.
     * @param compression TIFF compression type, for instance, "ZLib", "LZW".
     *                    If empty or {@code none}, no compression is used
     * @throws IJImageIOException writing fails or file format is not supported.
     * @see IJImageIO#getTIFFCompressionTypes()
     */
    public static void writeAsTiff(final BufferedImage[] images,
                                   final File file,
                                   final String compression,
                                   IIOMetadata metadata
    ) throws IJImageIOException {

        final ImageWriter imageWriter = getTIFFWriter();

        // Set compression parameters
        final ImageWriteParam writerParam = imageWriter.getDefaultWriteParam();
        if (compression != null && !compression.isEmpty()) {
            if (writerParam.canWriteCompressed()) {
                writerParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                final String[] compressionTypes = writerParam.getCompressionTypes();
                if (!Arrays.asList(compressionTypes).contains(compression)) {
                    throw new IllegalArgumentException("Unsupported TIFF compression type: \"" + compression + "\", " +
                            "valid types: " + Arrays.toString(compressionTypes));
                }
                writerParam.setCompressionType(compression);
            }
        }
        write(images, file, imageWriter, metadata, writerParam);
    }


    /**
     * Write image in TIFF format with ZLib compression using ImageIO
     *
     * @param file File to save to.
     * @param ip   Image to save.
     * @throws IJImageIOException writing fails or file format is not supported.
     */
    public static void writeAsTiff(final ImageProcessor ip, final File file) throws IJImageIOException {
        writeAsTiff(new ImagePlus("", ip), file);
    }

    public static ImageWriter getTIFFWriter() throws IJImageIOException {
        final String format = "tif";
        List<ImageWriterSpi> spis = IJImageOUtils.writerSpiByFormatName(format);
        if (spis.isEmpty()) {
            throw new IJImageIOException("Cannot find writer for format: '" + format + "'.");
        }
        final ImageWriterSpi imageWriterSpi = spis.get(0);
        final ImageWriter imageWriter;
        try {
            imageWriter = imageWriterSpi.createWriterInstance();
        } catch (IOException e) {
            throw new IJImageIOException("Failed to create image writer. " + e.getMessage(), e);
        }
        return imageWriter;
    }

    public static String[] getTIFFCompressionTypes() throws IJImageIOException {
        return getTIFFWriter().getDefaultWriteParam().getCompressionTypes();
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

    /**
     * Return list of all currently registered readers that  that claim to be able to decode the supplied ImageInputStream.
     * Preferred readers are returned at the beginning on=f the list.
     *
     * @param iis input stream.
     * @return list of readers claiming to be able to decode the input stream.
     * @throws IJImageIOException if no readers are found.
     */
    public static List<ImageReader> getImageReaders(ImageInputStream iis) throws IJImageIOException {
        final Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
        final List<ImageReader> preferredReaders = new ArrayList<>();
        final List<ImageReader> otherReaders = new ArrayList<>();
        while (readers.hasNext()) {
            final ImageReader reader = readers.next();
            ImageReaderSpi spi = reader.getOriginatingProvider();
            if (spi != null && spi.getVendorName().toLowerCase().contains(PREFERRED_SPI_VENDOR)) {
                preferredReaders.add(reader);
            } else {
                otherReaders.add(reader);
            }
        }

        preferredReaders.addAll(otherReaders);

        // Verify that there is at least one reader available.
        if (preferredReaders.isEmpty()) {
            throw new IJImageIOException("Input file format not supported: Cannot find proper image reader.");
        }
        return preferredReaders;
    }

    private static List<ImageAndMetadata> read(final ImageReader reader,
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
        final List<ImageAndMetadata> images = new ArrayList<>();
        for (int i = 0; i < pageIndex.length; i++) {
            IJ.showProgress(i, pageIndex.length);

            final BufferedImage bi;
            final IIOMetadata md;
            try {
                bi = reader.read(i);
                md = reader.getImageMetadata(i);
            } catch (final IOException e) {
                throw new IJImageIOException("Error reading image with internal index " + i
                        + ". Min internal index is " + minIndex + ". " + e.getMessage(), e);
            }

//            // Read metadata for this image
//            final ImageReadParam imageReadParam = reader.getDefaultReadParam();
//            final IIOImage a = reader.readAll(j, imageReadParam);
//            final IIOMetadata metadata = a.getMetadata();

            md.getController();
            images.add(new ImageAndMetadata(bi, md));
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

        final List<ImagePlus> result = new ArrayList<>();
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

        return result.toArray(new ImagePlus[0]);
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
