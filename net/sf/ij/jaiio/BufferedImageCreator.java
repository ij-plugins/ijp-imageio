/*
 * Image/J Plugins
 * Copyright (C) 2002 Jarek Sacha
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
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.awt.Point;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import non_com.media.jai.ComponentSampleModelJAI;
import non_com.media.jai.DataBufferFloat;
import non_com.media.jai.FloatDoubleColorModel;
import non_com.media.jai.RasterFactory;
import non_com.media.jai.codec.ImageCodec;

/**
 * @author     Jarek Sacha
 * @created    February 18, 2002
 * @version    $Revision: 1.1 $
 */
public class BufferedImageCreator {

  /*
   *  Made private to prevent subclassing.
   */
  private BufferedImageCreator() {
  }


  /**
   *  Create BufferedImage from a slice <code>sliceNb</code> in image <code>src</code>
   *  . Indexing starts at 0. New image has a copy of pixels in the source
   *  image.
   *
   * @param  src      Source image.
   * @param  sliceNb  Slice number, numbering starts at 0.
   * @return          New BufferedImage.
   */
  public static BufferedImage create(ImagePlus src, int sliceNb) {

    // Get slice image processor
    int oldSliceNb = src.getCurrentSlice();
    src.setSlice(sliceNb + 1);
    ImageProcessor ip = src.getProcessor();
    src.setSlice(oldSliceNb);

    // Convert image processor
    switch (src.getType()) {
      case ImagePlus.GRAY8:
        if (src.isInvertedLut()) {
          ip = ip.duplicate();
          ip.invert();
        }
        ColorModel cm = ip.getColorModel();
        if (cm != null && (cm instanceof IndexColorModel)) {
          return create((ByteProcessor) ip, (IndexColorModel) cm);
        }
        else {
          return create((ByteProcessor) ip);
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
        throw new IllegalArgumentException(
            "Unrecognized image type: " + src.getType() + ".");
    }
  }


  /**
   *  Create BufferedImages corresponding to each slice in the source image.
   *
   * @param  src  Source image.
   * @return      Array of BufferedImages, one per source slice.
   */
  public static BufferedImage[] createArray(ImagePlus src) {

    BufferedImage[] r = new BufferedImage[src.getStackSize()];

    // Get slice image processor
    int oldSliceNb = src.getCurrentSlice();
    for (int i = 0; i < r.length; ++i) {
      // Set slice here to minimize slice switching by create()
      src.setSlice(i + 1);
      r[i] = create(src, i);
    }

    src.setSlice(oldSliceNb);

    return r;
  }



  /**
   *  Create BufferedImage from ByteProcessor.
   *
   * @param  src  ByteProcessor source.
   * @return      BufferedImage.
   */
  public static BufferedImage create(ByteProcessor src) {
    byte[] r = new byte[256];
    byte[] g = new byte[256];
    byte[] b = new byte[256];
    for (int i = 0; i < 256; ++i) {
      r[i] = g[i] = b[i] = (byte) (i & 0xff);
    }
    IndexColorModel icm = new IndexColorModel(8, 256, r, g, b);

    return create(src, icm);
  }


  /**
   *  Create BufferedImage from an 256 indexed color image.
   *
   * @param  src  ByteProcessor source.
   * @param  icm  Color model.
   * @return      BufferedImage.
   */
  public static BufferedImage create(ByteProcessor src, IndexColorModel icm) {
    WritableRaster wr = icm.createCompatibleWritableRaster(src.getWidth(),
        src.getHeight());
    DataBufferByte dataBuffer = (DataBufferByte) wr.getDataBuffer();
    byte[] srcPixels = (byte[]) src.getPixels();
    byte[] destPixels = dataBuffer.getData();
    System.arraycopy(srcPixels, 0, destPixels, 0, destPixels.length);

    BufferedImage bufferedImage = new BufferedImage(icm, wr, false, null);

    return bufferedImage;
  }


  /**
   *  Create BufferedImage from ShortProcessor. Pixel values are assumed to be
   *  unsigned short integers.
   *
   * @param  src  ShortProcessor source.
   * @return      BufferedImage.
   */
  public static BufferedImage create(ShortProcessor src) {
    BufferedImage bufferedImage = new BufferedImage(
        src.getWidth(), src.getHeight(), BufferedImage.TYPE_USHORT_GRAY);
    Raster raster = bufferedImage.getData();
    DataBufferUShort dataBuffer = (DataBufferUShort) raster.getDataBuffer();
    System.arraycopy(src.getPixels(), 0, dataBuffer.getData(), 0,
        dataBuffer.getData().length);

    return bufferedImage;
  }


  /**
   *  Description of the Method
   *
   * @param  src  Description of Parameter
   * @return      Description of the Returned Value
   */
  public static BufferedImage create(FloatProcessor src) {

    int w = src.getWidth();
    int h = src.getHeight();

    int nbBands = 1;
    int[] rgbOffset = new int[nbBands];
    SampleModel sampleModel = RasterFactory.createPixelInterleavedSampleModel(
        DataBuffer.TYPE_FLOAT, w, h, nbBands, nbBands * w, rgbOffset);

    ColorModel colorModel = ImageCodec.createComponentColorModel(sampleModel);

    float[] pixels = (float[]) src.getPixels();
    DataBufferFloat dataBuffer = new DataBufferFloat(pixels, pixels.length);

    WritableRaster raster = RasterFactory.createWritableRaster(sampleModel,
        dataBuffer, new Point(0, 0));

    return new BufferedImage(colorModel, raster, false, null);
  }


  /**
   *  Create BufferedImage from ShortProcessor. Pixel values are assumed to be
   *  unsigned short integers.
   *
   * @param  src  ShortProcessor source.
   * @return      BufferedImage.
   */
  public static BufferedImage create(ColorProcessor src) {
    ColorModel cm = src.getColorModel();
    WritableRaster raster = cm.createCompatibleWritableRaster(src.getWidth(),
        src.getHeight());
    DataBufferInt dataBuffer = (DataBufferInt) raster.getDataBuffer();
    System.arraycopy(src.getPixels(), 0, dataBuffer.getData(), 0,
        dataBuffer.getData().length);

    BufferedImage bufferedImage = new BufferedImage(cm, raster, false, null);
    return bufferedImage;
  }

}