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

package net.sf.ij_plugins.imageio;

import ij.ImagePlus;
import ij.process.*;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;


/**
 * Creates/converts BufferedImage objects from ImageJ's ImageProcessor or ImagePlus. All Image/J
 * image types are supported.
 *
 * @author Jarek Sacha
 */
public class BufferedImageFactory {

    private static final byte[][] grayIndexCmaps = {
            null,
            // 1 bit
            {(byte) 0x00, (byte) 0xff},
            // 2 bits
            {(byte) 0x00, (byte) 0x55, (byte) 0xaa, (byte) 0xff},
            null,
            // 4 bits
            {(byte) 0x00, (byte) 0x11, (byte) 0x22, (byte) 0x33,
                    (byte) 0x44, (byte) 0x55, (byte) 0x66, (byte) 0x77,
                    (byte) 0x88, (byte) 0x99, (byte) 0xaa, (byte) 0xbb,
                    (byte) 0xcc, (byte) 0xdd, (byte) 0xee, (byte) 0xff}
    };


    /**
     * Made private to prevent sub-classing.
     */
    private BufferedImageFactory() {
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
    public static BufferedImage createFrom(final ImagePlus src, final int sliceNb, final boolean preferBinary) {

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
                if (bp.isBinary() && preferBinary) {

                    final int width = bp.getWidth();
                    final int height = bp.getHeight();
                    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
                    WritableRaster raster = bi.getRaster();
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            raster.setSample(x, y, 0, (bp.get(x, y) == 0) ? 0 : 1);
                        }
                    }
                    return bi;

//                    IndexColorModel cm = new IndexColorModel(1, 2, new byte[]{0, (byte) 255}, new byte[]{0, (byte) 255}, new byte[]{0, (byte) 255});
//                    return createFrom(bp, cm);

//                    final ColorModel cm = ip.getColorModel();
//                    if (cm instanceof IndexColorModel) {
//                        return createFrom(bp, (IndexColorModel) cm);
//                    } else {
//                        throw new RuntimeException("Expecting 'IndexColorModel', got: " + cm);
//                    }
                } else {
                    return createFrom(bp);
                }

            case ImagePlus.GRAY16:
                return createFrom((ShortProcessor) ip);
            case ImagePlus.GRAY32:
                return createFrom((FloatProcessor) ip);
            case ImagePlus.COLOR_256:
                return createFrom((ByteProcessor) ip, (IndexColorModel) ip.getColorModel());
            case ImagePlus.COLOR_RGB:
                return createFrom((ColorProcessor) ip);
            default:
                throw new IllegalArgumentException("Unrecognized image type: " + src.getType() + ".");
        }
    }


//    public static BufferedImage createBinary(final ByteProcessor src) {
//        // TODO: create destination BufferedImage directly without creating intermediate image (save memory and time)
//
////        final BufferedImage srcBI = create(src);
////
////        final BufferedImage destBI = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
////        final Graphics2D g2d = destBI.createGraphics();
////        g2d.drawImage(srcBI, 0, 0, null);
////
////        return destBI;
//
//        final int w = src.getWidth();
//        final int h = src.getHeight();
//
//        final SampleModel sampleModel = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, w, h, 1);
//        final ColorModel colorModel = createGrayIndexColorModel(sampleModel, true);
//        final byte[] pixels = (byte[]) src.getPixels();
//        final DataBufferByte dataBuffer = new DataBufferByte(pixels, pixels.length);
//
//        final int bitsPerPixel = 1;
//        final WritableRaster raster = RasterFactory.createPackedRaster(dataBuffer, w, h, bitsPerPixel, new Point(0, 0));
//
//        return new BufferedImage(colorModel, raster, false, null);
//
//    }


    /**
     * Create BufferedImage from ImageProcessor.
     * If input is an instance of ByteProcessor it will share pixels with output.
     * If this returned image is intended for display, it is safer to use {@link #createGraphicsCompatibleFrom(ij.process.ImageProcessor)}.
     *
     * @param src ImageProcessor source.
     * @return new BufferedImage.
     * @see #createGraphicsCompatibleFrom(ij.process.ImageProcessor)
     */
    public static BufferedImage createFrom(final ImageProcessor src) {
        Validate.notNull(src);

        if (src instanceof BinaryProcessor) {
            return createFrom((BinaryProcessor) src);
        } else if (src instanceof ByteProcessor) {
            return createFrom((ByteProcessor) src);
        } else if (src instanceof ShortProcessor) {
            return createFrom((ShortProcessor) src);
        } else if (src instanceof FloatProcessor) {
            return createFrom((FloatProcessor) src);
        } else if (src instanceof ColorProcessor) {
            return createFrom((ColorProcessor) src);
        } else {
            throw new IllegalArgumentException("Unsupported processor type: " + src.getClass().getName());
        }
    }


    /**
     * Create BufferedImage from BinaryProcessor.
     *
     * @param src ByteProcessor source.
     * @return BufferedImage.
     */
    public static BufferedImage createFrom(final BinaryProcessor src) {

        final IndexColorModel icm = (IndexColorModel) src.getColorModel();
        final int mapSize = icm.getMapSize();
        if (mapSize != 2) {
            throw new IllegalArgumentException(
                    "Input BinaryProcessor has to have index color model with map size of 2, got " + mapSize + ".");
        }

        final WritableRaster wr = icm.createCompatibleWritableRaster(src.getWidth(), src.getHeight());

        final byte[] bitsOn = {(byte) 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
        final byte[] srcPixels = (byte[]) src.getPixels();
        final DataBufferByte dataBuffer = (DataBufferByte) wr.getDataBuffer();
        final byte[] destPixels = dataBuffer.getData();
        // Double check that dest data are large enough
        final int srcWidth = src.getWidth();
        final int destWidth = (src.getWidth() + 7) / 8;
        final int expectedDestSize = destWidth * src.getHeight();
        if (destPixels.length != expectedDestSize) {
            throw new IllegalStateException("Internal error: wrong size of destPixels.");
        }
        // Single bit image, pack bits
        for (int i = 0; i < destPixels.length; ++i) {
            byte destByte = 0x00;
            final int offset = (i / destWidth) * srcWidth + (i % destWidth) * 8;
            for (int j = 0; j < 8 && ((j + offset) < srcPixels.length); ++j) {
                if (srcPixels[j + offset] != 0) {
                    destByte += bitsOn[j];
                }
            }
            destPixels[i] = destByte;
        }
        return new BufferedImage(icm, wr, false, null);
    }


    /**
     * Create BufferedImage from ByteProcessor, use IndexColorModel with uniform gray level distribution from 0 to 255.
     * Share pixels with source.
     * <p>
     * Alternative approach is to use {@link ij.process.ByteProcessor#createBufferedImage()} which uses color model
     * currently assigned to source ByteProcessor.
     *
     * @param src ByteProcessor source.
     * @return BufferedImage.
     * @see ij.process.ByteProcessor#createBufferedImage()
     */
    public static BufferedImage createFrom(final ByteProcessor src) {
        final byte[] r = new byte[256];
        final byte[] g = new byte[256];
        final byte[] b = new byte[256];
        for (int i = 0; i < 256; ++i) {
            r[i] = g[i] = b[i] = (byte) (i & 0xff);
        }
        final IndexColorModel icm = new IndexColorModel(8, 256, r, g, b);

        return createFrom(src, icm);
    }


    /**
     * Create BufferedImage from ShortProcessor.
     * Convenience call to {@link ij.process.ShortProcessor#get16BitBufferedImage()}.
     * As of ImageJ 1.41o this creates a copy of the data, pixels are not shared between source and destination.
     *
     * @param src source image
     * @return buffered image of type BufferedImage.TYPE_USHORT_GRAY
     * @see ij.process.ShortProcessor#get16BitBufferedImage()
     */
    public static BufferedImage createFrom(final ShortProcessor src) {
        // TODO: create destination BufferedImage directly without creating intermediate image (save memory and time)
        return src.get16BitBufferedImage();
    }


//    /**
//     * Create BufferedImage from ShortProcessor. Pixel values are assumed to be unsigned short
//     * integers.
//     *
//     * @param src ShortProcessor source.
//     * @return BufferedImage.
//     */
//    public static BufferedImage createFrom2(final ShortProcessor src) {
//
//        final int w = src.getWidth();
//        final int h = src.getHeight();
//
//        final int nbBands = 1;
//        final int[] rgbOffset = new int[nbBands];
//        final SampleModel sampleModel = RasterFactory.createPixelInterleavedSampleModel(
//                DataBuffer.TYPE_USHORT, w, h, nbBands, nbBands * w, rgbOffset);
//
//        final ColorModel colorModel = ImageCodec.createComponentColorModel(sampleModel);
//
//        final short[] pixels = (short[]) src.getPixels();
//        final DataBufferShort dataBuffer = new DataBufferShort(pixels, pixels.length);
//
//        final WritableRaster raster = RasterFactory.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));
//
//        return new BufferedImage(colorModel, raster, false, null);
//
//    }


    /**
     * Create BufferedImage from FloatProcessor.
     *
     * @param src FloatProcessor source.
     * @return BufferedImage.
     */
    public static BufferedImage createFrom(final FloatProcessor src) {

        final int width = src.getWidth();
        final int height = src.getHeight();

        final float[] pixels = (float[]) src.getPixels();
        final DataBufferFloat dataBuffer = new DataBufferFloat(pixels, pixels.length);

        final int nbBands = 1;
        final int[] rgbOffset = new int[nbBands];
        final SampleModel sampleModel
                = new ComponentSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1, width, rgbOffset);
        final WritableRaster wr = Raster.createWritableRaster(sampleModel, dataBuffer, new Point());
        final ColorModel colorModel = createFloatColorModel(nbBands);

        // Put all together into a buffered image
        return new BufferedImage(colorModel, wr, true, null);
    }


    /**
     * Create BufferedImage from ColorProcessor.
     *
     * @param src source.
     * @return new BufferedImage.
     */
    public static BufferedImage createFrom(final ColorProcessor src) {
        final ColorModel cm = src.getColorModel();
        final WritableRaster raster = cm.createCompatibleWritableRaster(src.getWidth(), src.getHeight());
        final DataBufferInt dataBuffer = (DataBufferInt) raster.getDataBuffer();
        final int[] srcPixels = (int[]) src.getPixels();
        System.arraycopy(srcPixels, 0, dataBuffer.getData(), 0, dataBuffer.getData().length);

        return new BufferedImage(cm, raster, false, null);
    }


    /**
     * Create BufferedImage from an 256 indexed color image. If supplied color model mas map size
     * less than 256 it will be extended.
     *
     * @param src ByteProcessor source.
     * @param icm Color model.
     * @return BufferedImage.
     * @see #createFrom(ij.process.ByteProcessor, java.awt.image.IndexColorModel)
     */
    private static BufferedImage createFromExp(final ByteProcessor src, final IndexColorModel icm) {

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

        return createFrom(src, icm256);
    }


    /**
     * Create BufferedImage from an 256 indexed color image. Share pixels with source.
     *
     * @param src ByteProcessor source.
     * @param icm Color model.
     * @return new BufferedImage.
     */
    public static BufferedImage createFrom(final ByteProcessor src, final IndexColorModel icm) {

        final int width = src.getWidth();
        final int height = src.getHeight();
        final byte[] pixels = (byte[]) src.getPixels();

        // Create raster from byte array pixels
        final DataBufferByte dataBuffer = new DataBufferByte(pixels, pixels.length);
        final SampleModel sampleModel
                = new ComponentSampleModel(DataBuffer.TYPE_BYTE, width, height, 1, width, new int[]{0});
        final WritableRaster wr = Raster.createWritableRaster(sampleModel, dataBuffer, null);

        // Put all together into a buffered image
        return new BufferedImage(icm, wr, true, null);
    }


    public static ColorModel createFloatColorModel(final int bands) {
        Validate.isTrue(bands >= 1 && bands <= 4, "Invalid 'bands' value: %d", bands);

        final ColorSpace cs = bands <= 2
                ? ColorSpace.getInstance(ColorSpace.CS_GRAY)
                : ColorSpace.getInstance(ColorSpace.CS_sRGB);
        final boolean hasAlpha = bands % 2 == 0;

        return new FloatDoubleColorModel(cs, hasAlpha, false,
                (hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE), DataBuffer.TYPE_FLOAT);
    }


    /**
     * Create BufferedImage from AWT Image
     *
     * @param src source Image.
     * @return new BufferedImage.
     */
    public static BufferedImage createCurrentScreenCompatibleImage(final Image src) {
        final int w = src.getWidth(null);
        final int h = src.getHeight(null);
        final GraphicsEnvironment local = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice screen = local.getDefaultScreenDevice();
        final GraphicsConfiguration config = screen.getDefaultConfiguration();
        final BufferedImage bi = config.createCompatibleImage(w, h);
        final Graphics2D g2D = bi.createGraphics();
        g2D.drawImage(src, 0, 0, null);
        g2D.dispose();

        return bi;
    }

    /**
     * Create a BufferedImage that is compatible with current default screen graphics device.
     *
     * @param src source image
     * @return buffered image.
     * @see GraphicsConfiguration#createCompatibleImage(int, int)
     */
    public static BufferedImage createGraphicsCompatibleFrom(final ImageProcessor src) {
        Validate.notNull(src, "Argument 'src' cannot be null.");

        final BufferedImage image = createFrom(src);
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice gs = ge.getDefaultScreenDevice();
        final GraphicsConfiguration gc = gs.getDefaultConfiguration();
        final BufferedImage result = gc.createCompatibleImage(image.getWidth(), image.getHeight());
        final Graphics g = result.getGraphics();
        g.drawImage(image, 0, 0, null);

        return result;
    }

    /**
     * A convenience methods to create an instance of
     * <code>IndexColorModel</code> suitable for the given 1-banded
     * <code>SampleModel</code>.
     *
     * @param sm          a 1-banded <code>SampleModel</code>.
     * @param blackIsZero <code>true</code> if the gray ramp should
     *                    go from black to white, <code>false</code>otherwise.
     */
    private static ColorModel createGrayIndexColorModel(SampleModel sm,
                                                        boolean blackIsZero) {
        if (sm.getNumBands() != 1) {
            throw new IllegalArgumentException();
        }
        int sampleSize = sm.getSampleSize(0);

        byte[] cmap = null;
        if (sampleSize < 8) {
            cmap = grayIndexCmaps[sampleSize];
            if (!blackIsZero) {
                int length = cmap.length;
                byte[] newCmap = new byte[length];
                for (int i = 0; i < length; i++) {
                    newCmap[i] = cmap[length - i - 1];
                }
                cmap = newCmap;
            }
        } else {
            cmap = new byte[256];
            if (blackIsZero) {
                for (int i = 0; i < 256; i++) {
                    cmap[i] = (byte) i;
                }
            } else {
                for (int i = 0; i < 256; i++) {
                    cmap[i] = (byte) (255 - i);
                }
            }
        }

        return new IndexColorModel(sampleSize, cmap.length,
                cmap, cmap, cmap);
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


}
