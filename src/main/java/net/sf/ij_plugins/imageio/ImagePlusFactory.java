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

import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.*;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.imageio.plugins.tiff.TIFFField;
import java.awt.image.*;

import static java.awt.image.DataBuffer.TYPE_USHORT;

/**
 * Creates/converts Image/J's image objects from Java2D/JAI representation.
 *
 * @author Jarek Sacha
 */
public class ImagePlusFactory {


    private ImagePlusFactory() {
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
     * @throws IJImageIOException If data buffer is in unknown format.
     */
    public static ImageProcessor createProcessor(final int w, final int h, final DataBuffer buffer,
                                                 final ColorModel cm) throws IJImageIOException {

        if (buffer.getOffset() != 0) {
            throw new IJImageIOException("Expecting BufferData with no offset.");
        }

        switch (buffer.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                return new ByteProcessor(w, h, ((DataBufferByte) buffer).getData(), cm);
            case TYPE_USHORT:
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
                throw new IJImageIOException("Pixel type is undefined.");
            default:
                throw new IJImageIOException("Unrecognized DataBuffer data type");
        }
    }


    /**
     * Convert BufferedImage to ImageProcessor.
     *
     * @param src image to be converted.
     * @return Instance of ImageProcessor, for instance, ColorProcessor.
     * @throws IJImageIOException when unable to determine how to convert the image.
     */
    public static ImageProcessor createProcessor(final BufferedImage src) throws IJImageIOException {

        // TODO verify that short pixels are converted correctly

        final Raster raster = src.getRaster();
        ColorModel colorModel = src.getColorModel();
        final DataBuffer dataBuffer = raster.getDataBuffer();

        final int numBanks = dataBuffer.getNumBanks();
        if (numBanks > 1 && colorModel == null) {
            throw new IJImageIOException("Don't know what to do with image with no " +
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
     * ColorModel cm
     * @throws IJImageIOException when enable to create ImagePlus.
     * @see #create(String, WritableRaster, ColorModel)
     */
    public static ImagePlus create(final String title, final BufferedImage bi) throws IJImageIOException {
        return create(title, bi.getRaster(), bi.getColorModel());
    }

    public static ImagePlus create(final String title, final IJImageIO.ImageAndMetadata mi) throws IJImageIOException {
        ImagePlus imp = create(title, mi.image.getRaster(), mi.image.getColorModel());

        try {
            // WE will assume that this is a TIFF file, if it not an exception from the fallowing will get us out of here
            TIFFDirectory tmd = TIFFDirectory.createFromMetadata(mi.metadata);

            TIFFField xResField = tmd.getTIFFField(BaselineTIFFTagSet.TAG_X_RESOLUTION);
            if (xResField != null) {
                long[] ls = xResField.getAsRational(0);
                Calibration cal = imp.getCalibration();
                cal.pixelWidth = ls[1] / (double) ls[0];
                imp.setCalibration(cal);
            }

            TIFFField yResField = tmd.getTIFFField(BaselineTIFFTagSet.TAG_Y_RESOLUTION);
            if (yResField != null) {
                long[] ls = yResField.getAsRational(0);
                Calibration cal = imp.getCalibration();
                cal.pixelHeight = ls[1] / (double) ls[0];
                imp.setCalibration(cal);
            }

            TIFFField resolutionUnitField = tmd.getTIFFField(BaselineTIFFTagSet.TAG_RESOLUTION_UNIT);
            if (resolutionUnitField != null) {
                Calibration cal = imp.getCalibration();
                switch (resolutionUnitField.getAsInt(0)) {
                    case 1:
                        cal.setUnit(" ");
                        break;
                    case 2:
                        cal.setUnit("inch");
                        break;
                    case 3:
                        cal.setUnit("cm");
                        break;
                    default:
                        throw new IJImageIOException("Unsupported resolution unit field value: " + resolutionUnitField.getAsInt(0));
                }
                imp.setCalibration(cal);
            }

            {
                TIFFField field = tmd.getTIFFField(BaselineTIFFTagSet.TAG_IMAGE_DESCRIPTION);
                if (field != null && field.getCount() > 0) {
                    String description = field.getAsString(0);
                    DescriptionStringCoder.decode(description, imp);
                }
            }
        } catch (IIOInvalidTreeException ex) {
            // Ignore attempt to treat this as a TIFF file
        }
        return imp;
    }

    /**
     * Create instance of ImagePlus from WritableRaster r and ColorModel cm.
     *
     * @param title name of the output image.
     * @param r     Raster containing pixel data.
     * @param cm    Image color model (can be null).
     * @return ImagePlus object created from WritableRaster r and
     * ColorModel cm
     * @throws IJImageIOException when enable to create ImagePlus.
     */
    public static ImagePlus create(final String title, final WritableRaster r, ColorModel cm)
            throws IJImageIOException {

        final DataBuffer db = r.getDataBuffer();

        final int numBanks = db.getNumBanks();
        if (numBanks > 1 && cm == null) {
            throw new IJImageIOException("Don't know what to do with image with no " +
                    "color model and multiple banks.");
        }

        final SampleModel sm = r.getSampleModel();
        final ImagePlus result;
        if (numBanks > 1 || sm.getNumBands() > 1) {
            if (sm.getNumBands() == 3 && sm.getDataType() == DataBuffer.TYPE_USHORT) {
                // Assume we have RGB48 image, so interpret it as a composite color image
                int width = r.getWidth();
                int height = r.getHeight();
                ShortProcessor red = new ShortProcessor(width, height);
                ShortProcessor green = new ShortProcessor(width, height);
                ShortProcessor blue = new ShortProcessor(width, height);
                int[] iArray = new int[3];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        sm.getPixel(x, y, iArray, db);
                        red.set(x, y, iArray[0]);
                        green.set(x, y, iArray[1]);
                        blue.set(x, y, iArray[2]);
                    }
                }
                ImageStack stack = new ImageStack(width, height);
                stack.addSlice(red);
                stack.addSlice(green);
                stack.addSlice(blue);
                stack.setSliceLabel("Red", 1);
                stack.setSliceLabel("Green", 2);
                stack.setSliceLabel("Blue", 3);
                result = new CompositeImage(new ImagePlus(title, stack), CompositeImage.COMPOSITE);
            } else {
                // If image has multiple banks or multiple color components, assume that it
                // is a color image and relay on AWT for proper decoding.
                final BufferedImage bi = new BufferedImage(cm, r, false, null);
                result = new ImagePlus(title, new ColorProcessor(bi));
            }
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
                    throw new IJImageIOException("Unable to process buffered image of type: " + bi.getType());
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
