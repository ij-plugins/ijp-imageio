/*
 * Image/J Plugins
 * Copyright (C) 2002-2009 Jarek Sacha
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

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.*;

import java.awt.image.*;

/**
 * Creates/converts Image/J's image objects from Java2D/JAI representation.
 *
 * @author Jarek Sacha
 */
public class ImagePlusCreator {


    private ImagePlusCreator() {
    }


    /**
     * Force Rendered image to set all the tails that it may have. In multi-tile
     * images not all tiles may be updated when a RenderedImage is created.
     *
     * @param ri image that may need tile update.
     * @return WritableRaster with all tiles updated.
     */
    public static WritableRaster forceTileUpdate(final RenderedImage ri) {
        Raster r = ri.getData();
        if (!(r instanceof WritableRaster)) {
            r = Raster.createWritableRaster(r.getSampleModel(), r.getDataBuffer(), null);
        }

        final WritableRaster wr = (WritableRaster) r;
        final int xTiles = ri.getNumXTiles();
        final int yTiles = ri.getNumYTiles();
        for (int ty = 0; ty < yTiles; ++ty) {
            for (int tx = 0; tx < xTiles; ++tx) {
                wr.setRect(ri.getTile(tx, ty));
            }
        }

        return wr;
    }


    /**
     * Create an ImageProcessor object from a DataBuffer.
     *
     * @param w      Image width.
     * @param h      Image height.
     * @param buffer Data buffer.
     * @param cm     Color model.
     * @return Image processor object.
     * @throws UnsupportedImageModelException If data buffer is in unknown format.
     */
    public static ImageProcessor createProcessor(final int w, final int h, final DataBuffer buffer,
                                                 final ColorModel cm) throws UnsupportedImageModelException {

        if (buffer.getOffset() != 0) {
            throw new UnsupportedImageModelException("Expecting BufferData with no offset.");
        }

        switch (buffer.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                return new ByteProcessor(w, h, ((DataBufferByte) buffer).getData(), cm);
            case DataBuffer.TYPE_USHORT:
                return new ShortProcessor(w, h, ((DataBufferUShort) buffer).getData(), cm);
            case DataBuffer.TYPE_SHORT:
                final short[] pixels = ((DataBufferShort) buffer).getData();
                for (int i = 0; i < pixels.length; ++i) {
                    pixels[i] = (short) (pixels[i] + 32768);
                }
                return new ShortProcessor(w, h, pixels, cm);
            case DataBuffer.TYPE_INT:
                return new FloatProcessor(w, h, ((DataBufferInt) buffer).getData());
            case DataBuffer.TYPE_FLOAT: {
                final DataBufferFloat dbFloat = (DataBufferFloat) buffer;
                return new FloatProcessor(w, h, dbFloat.getData(), cm);
            }
            case DataBuffer.TYPE_DOUBLE:
                return new FloatProcessor(w, h, ((DataBufferDouble) buffer).getData());
            case DataBuffer.TYPE_UNDEFINED:
                // ENH: Should this be reported as data problem?
                throw new UnsupportedImageModelException("Pixel type is undefined.");
            default:
                throw new UnsupportedImageModelException("Unrecognized DataBuffer data type");
        }
    }


    /**
     * Convert BufferedImage to ImageProcessor.
     *
     * @param src image to be converted.
     * @return Instance of ImageProcessor, for instance, ColorProcessor.
     * @throws UnsupportedImageModelException when unable to determine how to convert the image.
     */
    public static ImageProcessor createProcessor(final BufferedImage src) throws UnsupportedImageModelException {

        // TODO verify that short pixels are converted correctly

        final Raster raster = src.getRaster();
        ColorModel colorModel = src.getColorModel();
        final DataBuffer dataBuffer = raster.getDataBuffer();

        final int numBanks = dataBuffer.getNumBanks();
        if (numBanks > 1 && colorModel == null) {
            throw new UnsupportedImageModelException("Don't know what to do with image with no " +
                    "color model and multiple banks.");
        }

        final SampleModel sm = raster.getSampleModel();
        if (numBanks > 1 || sm.getNumBands() > 1
                ) {
            // If image has multiple banks or multiple color components, assume that it
            // is a color image and relay on AWT for proper decoding.
            return new ColorProcessor(src);
        } else if (sm.getSampleSize(0) < 8) {
            // Temporary fix for less then 8 bit images
            return new ByteProcessor(src);
        } else {
            if (!(colorModel instanceof IndexColorModel)) {
                // Image/J (as of version 1.26r) can not properly deal with non color
                // images and ColorModel that is not an instance of IndexedColorModel.
                colorModel = null;
            }

            return createProcessor(raster.getWidth(), raster.getHeight(), raster.getDataBuffer(), colorModel);
        }
    }


    /**
     * Create instance of ImagePlus from a BufferedImage.
     *
     * @param title name of the output image.
     * @param bi    source buffered image
     * @return ImagePlus object created from WritableRaster r and
     *         ColorModel cm
     * @throws UnsupportedImageModelException when enable to create ImagePlus.
     * @see #create(String, java.awt.image.WritableRaster, java.awt.image.ColorModel)
     */
    public static ImagePlus create(final String title, final BufferedImage bi) throws UnsupportedImageModelException {
        return create(title, bi.getRaster(), bi.getColorModel());
    }


    /**
     * Create instance of ImagePlus from WritableRaster r and ColorModel cm.
     *
     * @param title name of the output image.
     * @param r     Raster containing pixel data.
     * @param cm    Image color model (can be null).
     * @return ImagePlus object created from WritableRaster r and
     *         ColorModel cm
     * @throws UnsupportedImageModelException when enable to create ImagePlus.
     */
    public static ImagePlus create(final String title, final WritableRaster r, ColorModel cm)
            throws UnsupportedImageModelException {

        final DataBuffer db = r.getDataBuffer();

        final int numBanks = db.getNumBanks();
        if (numBanks > 1 && cm == null) {
            throw new UnsupportedImageModelException("Don't know what to do with image with no " +
                    "color model and multiple banks.");
        }

        final SampleModel sm = r.getSampleModel();
        final ImagePlus result;
        if (numBanks > 1 || sm.getNumBands() > 1
                ) {
            // If image has multiple banks or multiple color components, assume that it
            // is a color image and relay on AWT for proper decoding.
            final BufferedImage bi = new BufferedImage(cm, r, false, null);
            result = new ImagePlus(title, new ColorProcessor(bi));
        } else if (sm.getSampleSize(0) < 8) {
            // Temporary fix for less then 8 bit images
            final BufferedImage bi = new BufferedImage(cm, r, false, null);
            switch (bi.getType()) {
                case BufferedImage.TYPE_BYTE_GRAY:
                    result = new ImagePlus(title, new ByteProcessor(bi));
                    break;
                case BufferedImage.TYPE_BYTE_BINARY:
                    final int width = bi.getWidth();
                    final int height = bi.getHeight();
                    final ByteProcessor bp = new ByteProcessor(width, height);
                    final Raster data = bi.getData();
                    final int[] p = new int[1];
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            data.getPixel(x, y, p);
                            bp.set(x, y, p[0]);
                        }
                    }
                    bp.setColorModel(cm);
                    result = new ImagePlus(title, bp);
                    break;
                default:
                    throw new UnsupportedImageModelException("Unable to process buffered image of type: " + bi.getType());
            }
        } else {
            if (!(cm instanceof IndexColorModel)) {
                // Image/J (as of version 1.26r) can not properly deal with non color
                // images and ColorModel that is not an instance of IndexedColorModel.
                cm = null;
            }

            final ImageProcessor ip = createProcessor(r.getWidth(), r.getHeight(),
                    r.getDataBuffer(), cm);
            final ImagePlus im = new ImagePlus(title, ip);

            // Add calibration function for 'short' pixels
            if (db.getDataType() == DataBuffer.TYPE_SHORT) {

                final Calibration cal = new Calibration(im);
                final double[] coeff = new double[2];
                coeff[0] = -32768.0;
                coeff[1] = 1.0;
                cal.setFunction(Calibration.STRAIGHT_LINE, coeff, "gray value");
                im.setCalibration(cal);
            } else if (cm == null) {
                final Calibration cal = im.getCalibration();
                im.setCalibration(null);
                final ImageStatistics stats = im.getStatistics();
                im.setCalibration(cal);
                ip.setMinAndMax(stats.min, stats.max);
                im.updateImage();
            }

            result = im;
        }

        return result;

    }
}
