/*
 * Image/J Plugins
 * Copyright (C) 2002-2011 Jarek Sacha
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

package net.sf.ij.jaiio;

import com.sun.media.jai.codec.*;
import com.sun.media.jai.codecimpl.TIFFImage;
import com.sun.media.jai.codecimpl.TIFFImageDecoder;
import com.sun.media.jai.codecimpl.util.FloatDoubleColorModel;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.Opener;
import ij.io.TiffDecoder;
import ij.measure.Calibration;

import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Read image files using JAI image I/O codec
 * (http://developer.java.sun.com/developer/sampsource/jai/) and convert them
 * to Image/J representation.
 *
 * @author Jarek Sacha
 *         \
 */
public class JAIReader {

    private ImageDecoder decoder;
    private String decoderName;
    private File file;


    private JAIReader() {
    }


    /**
     * Read only the first image in the <code>file</code>.
     *
     * @param file Image file.
     * @return ImageInfo object.
     * @throws UnsupportedImageFileFormatException
     *                                        If file is not in a supported image format
     * @throws IOException                    In case of I/O error.
     * @throws UnsupportedImageModelException when conversion failed.
     */
    public static ImageInfo readFirstImageAndInfo(final File file)
            throws
            UnsupportedImageFileFormatException,
            UnsupportedImageModelException,
            IOException {

        // Find matching decoders
        final FileSeekableStream fss = new FileSeekableStream(file);
        final String[] decoders = ImageCodec.getDecoderNames(fss);
        if (decoders == null || decoders.length == 0) {
            throw new UnsupportedImageFileFormatException("Unsupported file format. "
                    + "Cannot find decoder capable of reading: " + file.getName());
        }

        // Create decoder
        final ImageDecoder decoder = ImageCodec.createImageDecoder(decoders[0], fss, null);

        final RenderedImage renderedImage = decoder.decodeAsRenderedImage();

        final ImageInfo imageInfo = new ImageInfo();
        imageInfo.numberOfPages = decoder.getNumPages();
        imageInfo.codecName = decoders[0];

        if (renderedImage instanceof BufferedImage) {
            imageInfo.previewImage = (BufferedImage) renderedImage;
        } else {
            final ColorModel cm = renderedImage.getColorModel();
            if (cm == null || cm instanceof FloatDoubleColorModel) {
                final WritableRaster writableRaster
                        = ImagePlusCreator.forceTileUpdate(renderedImage);
                final ImagePlus imagePlus = ImagePlusCreator.create(file.getName(), writableRaster, null);
                imageInfo.previewImage = (BufferedImage) imagePlus.getImage();
            } else {
                final Raster raster = renderedImage.getData();
                WritableRaster writableRaster;
                if (raster instanceof WritableRaster) {
                    writableRaster = (WritableRaster) raster;
                } else {
                    writableRaster = raster.createCompatibleWritableRaster();
                }

                imageInfo.previewImage = new BufferedImage(cm, writableRaster, false, null);
            }
        }

        return imageInfo;
    }


    /**
     * Open image in the file using registered codecs. A file may contain
     * multiple images. If all images in the file are of the same type and size
     * they will be combines into single stack within ImagesPlus object returned
     * as the first an only element of the image array. If reading from TIFF
     * files, image resolution and Image/J's description string containing
     * calibration information are decoded.
     *
     * @param file File to open image from.
     * @return Array of images contained in the file.
     * @throws Exception when unable to read image from the specified file.
     */
    public static ImagePlus[] read(final File file) throws Exception {
        return read(file, null);
    }


    /**
     * Open image in the file using registered codecs. A file may contain
     * multiple images. If all images in the file are of the same type and size
     * they will be combines into single stack within ImagesPlus object returned
     * as the first an only element of the image array. If reading from TIFF
     * files, image resolution and Image/J's description string containing
     * calibration information are decoded.
     *
     * @param file      File to open image from.
     * @param pageIndex Description of Parameter
     * @return Array of images contained in the file.
     * @throws Exception when unable to read image from the specified file.
     */
    public static ImagePlus[] read(final File file, int[] pageIndex) throws Exception {

        final JAIReader reader = new JAIReader();

        reader.open(file);

        // Get number of sub images
        final int nbPages = reader.getNumPages();
        if (nbPages < 1) {
            throw new Exception("Image decoding problem. "
                    + "Image file has less then 1 page. Nothing to decode.");
        }

        if (pageIndex == null) {
            pageIndex = new int[nbPages];
            for (int i = 0; i < nbPages; ++i) {
                pageIndex[i] = i;
            }
        }

        // Iterate through pages
        IJ.showProgress(0);
        final ArrayList<ImagePlus> imageList = new ArrayList<ImagePlus>();
        for (int i = 0; i < pageIndex.length; ++i) {
            if (pageIndex[i] != 0) {
                IJ.showStatus("Reading page " + pageIndex[i]);
            }

            imageList.add(reader.read(pageIndex[i]));
            IJ.showProgress((double) (i + 1) / pageIndex.length);
        }
        IJ.showProgress(1);

        reader.close();

        ImagePlus[] images = imageList.toArray(new ImagePlus[imageList.size()]);

        if (nbPages == 1) {
            // Do not use page numbers in image name
            images[0].setTitle(file.getName());
        } else {
            // Attempt to combine images into a single stack.
            final ImagePlus im = combineImages(images);
            if (im != null) {
                im.setTitle(file.getName());
                images = new ImagePlus[1];
                images[0] = im;
            }
        }

        return images;
    }


    /**
     * Attempt to combine images into a single stack. Images can be combined into
     * a stack if all of them are single slice images of the same type and
     * dimensions.
     *
     * @param images Array of images.
     * @return Input images combined into a stack. Return null if images
     *         cannot be combined.
     */
    private static ImagePlus combineImages(final ImagePlus[] images) {
        if (images == null || images.length <= 1) {
            return null;
        }

        if (images[0].getStackSize() != 1) {
            return null;
        }

        final int fileType = images[0].getFileInfo().fileType;
        final int w = images[0].getWidth();
        final int h = images[0].getHeight();
        final ImageStack stack = images[0].getStack();
        for (int i = 1; i < images.length; ++i) {
            final ImagePlus im = images[i];
            if (im.getStackSize() != 1) {
                return null;
            }
            if (fileType == im.getFileInfo().fileType
                    && w == im.getWidth() && h == im.getHeight()) {
                stack.addSlice(null, im.getProcessor().getPixels());
            } else {
                return null;
            }
        }

        images[0].setStack(images[0].getTitle(), stack);
        return images[0];
    }


    /**
     * @return The NumPages value
     * @throws IOException Description of Exception
     */
    private int getNumPages() throws IOException {
        return decoder.getNumPages();
    }


    /**
     * Create image decoder to read the image file.
     *
     * @param file Image file name.
     * @throws Exception Description of Exception
     */
    private void open(final File file) throws Exception {
        this.file = file;

        // Find matching decoders
        final FileSeekableStream fss = new FileSeekableStream(file);
        final String[] decoders = ImageCodec.getDecoderNames(fss);
        if (decoders == null || decoders.length == 0) {
            throw new Exception("Unsupported file format. "
                    + "Cannot find decoder capable of reading: " + file.getName());
        }

        this.decoderName = decoders[0];

        // Create decoder
        this.decoder = ImageCodec.createImageDecoder(decoderName, fss, null);
    }


    /**
     * @param pageNb Description of Parameter
     * @return Description of the Returned Value
     * @throws Exception Description of Exception
     */
    private ImagePlus read(final int pageNb) throws Exception {
        final RenderedImage ri;
        try {
            ri = decoder.decodeAsRenderedImage(pageNb);
        } catch (final Exception ex) {
            ex.printStackTrace();
            String msg = ex.getMessage();
            if (msg == null || msg.trim().length() < 1) {
                msg = "Error decoding rendered image.";
            }
            throw new Exception(msg);
        }

        final WritableRaster wr = ImagePlusCreator.forceTileUpdate(ri);

        ImagePlus im;
        if (decoderName.equalsIgnoreCase("GIF")
                || decoderName.equalsIgnoreCase("JPEG")) {
            // Convert the way ImageJ does (ij.io.Opener.openJpegOrGif())
            final BufferedImage bi = new BufferedImage(ri.getColorModel(), wr, false, null);
            im = new ImagePlus(file.getName(), bi);
            if (im.getType() == ImagePlus.COLOR_RGB) {
                // Convert RGB to gray if all bands are equal
                Opener.convertGrayJpegTo8Bits(im);
            }
        } else {
            final String title = file.getName() + " [" + (pageNb + 1) + "/" + getNumPages() + "]";
            im = ImagePlusCreator.create(title, wr, ri.getColorModel());

            if (im.getType() == ImagePlus.COLOR_RGB) {
                // Convert RGB to gray if all bands are equal
                Opener.convertGrayJpegTo8Bits(im);
            }

            // Extract TIFF tags
            if (ri instanceof TIFFImage) {
                final TIFFImage ti = (TIFFImage) ri;
                try {
                    final Object o = ti.getProperty("tiff_directory");
                    if (o instanceof TIFFDirectory) {
                        final TIFFDirectory dir = (TIFFDirectory) o;

                        // ImageJ description string
                        final TIFFField descriptionField
                                = dir.getField(TiffDecoder.IMAGE_DESCRIPTION);
                        if (descriptionField != null) {
                            try {
                                DescriptionStringCoder.decode(descriptionField.getAsString(0), im);
                            } catch (final Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        Calibration c = im.getCalibration();
                        if (c == null) {
                            c = new Calibration(im);
                        }

                        // X resolution
                        final TIFFField xResField = dir.getField(TIFFImageDecoder.TIFF_X_RESOLUTION);
                        if (xResField != null) {
                            final double xRes = xResField.getAsDouble(0);
                            if (xRes != 0) {
                                c.pixelWidth = 1 / xRes;
                            }
                        }

                        // Y resolution
                        final TIFFField yResField = dir.getField(TIFFImageDecoder.TIFF_Y_RESOLUTION);
                        if (yResField != null) {
                            final double yRes = yResField.getAsDouble(0);
                            if (yRes != 0) {
                                c.pixelHeight = 1 / yRes;
                            }
                        }

                        // Resolution unit
                        final TIFFField resolutionUnitField = dir.getField(TIFFImageDecoder.TIFF_RESOLUTION_UNIT);
                        if (resolutionUnitField != null) {
                            final int resolutionUnit = resolutionUnitField.getAsInt(0);
                            if (resolutionUnit == 1 && c.getUnit() == null) {
                                // no meaningful units
                                c.setUnit(" ");
                            } else if (resolutionUnit == 2) {
                                c.setUnit("inch");
                            } else if (resolutionUnit == 3) {
                                c.setUnit("cm");
                            }
                        }

                        im.setCalibration(c);
                    }
                } catch (final NegativeArraySizeException ex) {
                    // my be thrown by ti.getPrivateIFD(8)
                    ex.printStackTrace();
                }
            }
        }

        return im;
    }


    private void close() {
        decoder = null;
        decoderName = null;
        file = null;
    }


    /*
     *  Basic image information including first image in the file.
     */
    public static class ImageInfo {

        public BufferedImage previewImage;
        public int numberOfPages;
        public String codecName;
    }
}
