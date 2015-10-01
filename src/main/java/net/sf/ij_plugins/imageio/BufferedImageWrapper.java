/*
 * Image/J Plugins
 * Copyright (C) 2002-2010 Jarek Sacha
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

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codecimpl.util.RasterFactory;
import ij.ImagePlus;
import ij.process.*;
import net.sf.ij.jaiio.JaiioUtil;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;


/**
 * Creates/converts BufferedImage objects from Image/J's ImageProcessor or ImagePlus. All Image/J
 * image types are supported.
 *
 * @author Jarek Sacha
 */
public class BufferedImageWrapper {

    /*
     *  Made private to prevent sub-classing.
     */


    private BufferedImageWrapper() {
    }


    /**
     * Create BufferedImage from a slice <code>sliceNb</code> in image <code>src</code> . Indexing
     * starts at 0. New image has a copy of pixels in the source image.
     *
     * @param src          Source image.
     * @param sliceNb      Slice number, numbering starts at 0.
     * @param preferBinary Prefer to save two level binary images using 1 bit per pixel.
     * @return New BufferedImage.
     */
    public static BufferedImage create(final ImagePlus src, final int sliceNb, final boolean preferBinary) {

        // Get slice image processor
        final int oldSliceNb = src.getCurrentSlice();
        src.setSlice(sliceNb + 1);
        final ImageProcessor ip = src.getProcessor().duplicate();
        src.setSlice(oldSliceNb);

        // Convert image processor
        switch (src.getType()) {
            case ImagePlus.GRAY8:
                // Do not use color model provided by ImageProcessor since it can be 16 bit even for 8 bit ByteProcessor.
                final ByteProcessor bp = (ByteProcessor) ip;
                if (JaiioUtil.isBinary(bp) && preferBinary) {
                    final ColorModel cm = ip.getColorModel();
                    if (cm instanceof IndexColorModel) {
                        return create(bp, (IndexColorModel) ip.getColorModel());
                    } else {
                        throw new RuntimeException("Expecting 'IndexColorModel', got: " + cm);
                    }
                } else {
                    return create(bp);
                }

            case ImagePlus.GRAY16:
                return create((ShortProcessor) ip);
            case ImagePlus.GRAY32:
                return create((FloatProcessor) ip);
            case ImagePlus.COLOR_256:
                return create((ByteProcessor) ip, (IndexColorModel) ip.getColorModel());
            case ImagePlus.COLOR_RGB:
                return create((ColorProcessor) ip);
            default:
                throw new IllegalArgumentException("Unrecognized image type: " + src.getType() + ".");
        }
    }


    public static BufferedImage createBinary(final ByteProcessor src) {
        // TODO: create destination BufferedImage directly without creating intermediate image (save memory and time)

//        final BufferedImage srcBI = create(src);
//
//        final BufferedImage destBI = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
//        final Graphics2D g2d = destBI.createGraphics();
//        g2d.drawImage(srcBI, 0, 0, null);
//
//        return destBI;

        final int w = src.getWidth();
        final int h = src.getHeight();

        final SampleModel sampleModel = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, w, h, 1);
        final ColorModel colorModel = ImageCodec.createGrayIndexColorModel(sampleModel, true);
        final byte[] pixels = (byte[]) src.getPixels();
        final DataBufferByte dataBuffer = new DataBufferByte(pixels, pixels.length);

        final int bitsPerPixel = 1;
        final WritableRaster raster = RasterFactory.createPackedRaster(dataBuffer, w, h, bitsPerPixel, new Point(0, 0));

        return new BufferedImage(colorModel, raster, false, null);

    }


    /**
     * Create BufferedImage from ByteProcessor, share pixel data.
     *
     * @param src ByteProcessor source.
     * @return BufferedImage.
     */
    public static BufferedImage create(final ByteProcessor src) {
        // Create gray level color model
        final byte[] rLUT = new byte[256];
        final byte[] gLUT = new byte[256];
        final byte[] bLUT = new byte[256];
        for (int i = 0; i < 256; i++) {
            rLUT[i] = (byte) i;
            gLUT[i] = (byte) i;
            bLUT[i] = (byte) i;
        }
        final IndexColorModel icm = new IndexColorModel(8, 256, rLUT, gLUT, bLUT);

        return createFrom256Index(src, icm);
    }


    /**
     * Create BufferedImage from an 256 indexed color image. If supplied color model mas map size
     * less than 256 it will be extended.
     *
     * @param src ByteProcessor source.
     * @param icm Color model.
     * @return BufferedImage.
     * @see #create(ij.process.ByteProcessor,java.awt.image.IndexColorModel)
     */
    public static BufferedImage create(final ByteProcessor src, final IndexColorModel icm) {

        final int mapSize = icm.getMapSize();
        final IndexColorModel icm256;
        if (mapSize == 256) {
            // Use current color model
            icm256 = icm;
        } else if (mapSize < 256) {
            // Extend color model to 256
            final byte[] r = new byte[256];
            final byte[] g = new byte[256];
            final byte[] b = new byte[256];
            icm.getReds(r);
            icm.getGreens(g);
            icm.getBlues(b);

            icm256 = new IndexColorModel(8, 256, r, g, b);
        } else {
            throw new UnsupportedOperationException("Unable to properly decode this image (color map).\n" +
                    "Please report this problem at http://ij-plugins.sf.net\n" +
                    "or by sending email to 'jsacha at users.sourceforge.net'\n" +
                    "  Map size    = " + mapSize + ".");
        }

        return createFrom256Index(src, icm256);
    }


    /**
     * Create BufferedImage from an 256 indexed color image. Share pixels with source.
     *
     * @param src ByteProcessor source.
     * @param icm Color model.
     * @return new BufferedImage.
     */
    private static BufferedImage createFrom256Index(final ByteProcessor src, final IndexColorModel icm) {

        final int width = src.getWidth();
        final int height = src.getHeight();
        final byte[] pixels = (byte[]) src.getPixels();

        // Create raster from byte array pixels
        final DataBufferByte dataBuffer = new DataBufferByte(pixels, pixels.length);
        final SampleModel sampleModel
                = new ComponentSampleModel(DataBuffer.TYPE_BYTE, width, height, 1, width, new int[]{0});
        final WritableRaster wr = Raster.createWritableRaster(sampleModel, dataBuffer, new Point());

        // Put all together into a buffered image
        return new BufferedImage(icm, wr, true, null);
    }

//    /**
//     * Create BufferedImage from an indexed color image.
//     *
//     * @param src ByteProcessor source.
//     * @param icm Color model.
//     * @return BufferedImage.
//     */
//    public static BufferedImage create(final ByteProcessor src, final IndexColorModel icm) {
//        final WritableRaster wr = icm.createCompatibleWritableRaster(src.getWidth(),
//                src.getHeight());
//
//        final byte[] bitsOn = {(byte) 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
//        final byte[] srcPixels = (byte[]) src.getPixels();
//        final DataBufferByte dataBuffer = (DataBufferByte) wr.getDataBuffer();
//        final byte[] destPixels = dataBuffer.getData();
//        final int mapSize = icm.getMapSize();
//        if (mapSize == 256) {
//            System.arraycopy(srcPixels, 0, destPixels, 0, destPixels.length);
//            return new BufferedImage(icm, wr, false, null);
//        } else if (mapSize == 2) {
//            // Double check that dest data are large enough
//            int srcWidth = src.getWidth();
//            int destWidth = (src.getWidth() + 7) / 8;
//            int expectedDestSize = destWidth * src.getHeight();
//            if (destPixels.length != expectedDestSize) {
//                throw new IllegalStateException("Internal error: wrong size of destPixels.");
//            }
//            // Single bit image, pack bits
//            for (int i = 0; i < destPixels.length; ++i) {
//                byte destByte = 0x00;
//                int offset = (i / destWidth) * srcWidth + (i % destWidth) * 8;
//                for (int j = 0; j < 8 && ((j + offset) < srcPixels.length); ++j) {
//                    if (srcPixels[j + offset] != 0) {
//                        destByte += bitsOn[j];
//                    }
//                }
//                destPixels[i] = destByte;
//            }
//            return new BufferedImage(icm, wr, false, null);
//        } else {
//            // FIX: deal with all bit packing schemes
//            throw new UnsupportedOperationException("Unable to properly decode this image (color map).\n" +
//                    "Please report this problem at http://ij-plugins.sf.net\n" +
//                    "or by sending email to 'jsacha at users.sourceforge.net'\n" +
//                    "  Map size    = " + mapSize + "\n" +
//                    "  Src pixels  = " + srcPixels.length + "\n" +
//                    "  Dest pixels = " + destPixels.length);
//        }
//    }


    /**
     * Create BufferedImage from ShortProcessor. Pixel values are assumed to be unsigned short
     * integers.
     *
     * @param src ShortProcessor source.
     * @return BufferedImage.
     */
    public static BufferedImage create(final ShortProcessor src) {

        final int w = src.getWidth();
        final int h = src.getHeight();

        final int nbBands = 1;
        final int[] rgbOffset = new int[nbBands];
        final SampleModel sampleModel = RasterFactory.createPixelInterleavedSampleModel(
                DataBuffer.TYPE_USHORT, w, h, nbBands, nbBands * w, rgbOffset);

        final ColorModel colorModel = ImageCodec.createComponentColorModel(sampleModel);

        final short[] pixels = (short[]) src.getPixels();
        final DataBufferShort dataBuffer = new DataBufferShort(pixels, pixels.length);

        final WritableRaster raster = RasterFactory.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));

        return new BufferedImage(colorModel, raster, false, null);

    }


    /**
     * Create BufferedImage from FloatProcessor.
     *
     * @param src FloatProcessor source.
     * @return BufferedImage.
     */
    public static BufferedImage create(final FloatProcessor src) {

        final int w = src.getWidth();
        final int h = src.getHeight();

        final int nbBands = 1;
        final int[] rgbOffset = new int[nbBands];
        final SampleModel sampleModel = RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_FLOAT, w, h, nbBands, nbBands * w, rgbOffset);

        final ColorModel colorModel = ImageCodec.createComponentColorModel(sampleModel);

        final float[] pixels = (float[]) src.getPixels();
        final DataBufferFloat dataBuffer = new DataBufferFloat(pixels, pixels.length);

        final WritableRaster raster = RasterFactory.createWritableRaster(sampleModel,
                dataBuffer, new Point(0, 0));

        return new BufferedImage(colorModel, raster, false, null);
    }


    /**
     * Create BufferedImage from ColorProcessor.
     *
     * @param src ColorProcessor source.
     * @return BufferedImage.
     */
    public static BufferedImage create(final ColorProcessor src) {
        final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        final int[] bits = {8, 8, 8};
        final ColorModel cm = new ComponentColorModel(cs, bits, false, false,
                Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        final WritableRaster raster = cm.createCompatibleWritableRaster(src.getWidth(),
                src.getHeight());
        final DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();

        final byte[] data = dataBuffer.getData();
        final int n = ((int[]) src.getPixels()).length;
        final byte[] r = new byte[n];
        final byte[] g = new byte[n];
        final byte[] b = new byte[n];
        src.getRGB(r, g, b);
        for (int i = 0; i < n; ++i) {
            final int offset = i * 3;
            data[offset] = r[i];
            data[offset + 1] = g[i];
            data[offset + 2] = b[i];
        }

        return new BufferedImage(cm, raster, false, null);
    }
}
