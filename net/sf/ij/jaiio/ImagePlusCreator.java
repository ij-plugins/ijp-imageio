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
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortProcessor;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import non_com.media.jai.DataBufferDouble;
import non_com.media.jai.DataBufferFloat;
import non_com.media.jai.FloatDoubleColorModel;

/**
 *  Read image files using JAI image I/O codec
 *  (http://developer.java.sun.com/developer/sampsource/jai/) and convert them
 *  to Image/J representation.
 *
 * @author     Jarek Sacha
 * @created    January 11, 2002
 * @version    $Revision: 1.1 $
 */
public class ImagePlusCreator {

  private ImagePlusCreator() {
  }


  /**
   *  Force Rendered image to set all the tails that it may have. In multi-tile
   *  images not all tiles may be updated when a RenderedImage is created.
   *
   * @param  ri  image that may need tile update.
   * @return     Description of the Returned Value
   */
  public static WritableRaster forceTileUpdate(RenderedImage ri) {
    Raster r = ri.getData();
    if (!(r instanceof WritableRaster)) {
      r = r.createWritableRaster(r.getSampleModel(), r.getDataBuffer(), null);
    }

    WritableRaster wr = (WritableRaster) r;
    int xTiles = ri.getNumXTiles();
    int yTiles = ri.getNumYTiles();
    for (int ty = 0; ty < yTiles; ++ty) {
      for (int tx = 0; tx < xTiles; ++tx) {
        wr.setRect(ri.getTile(tx, ty));
      }
    }

    return wr;
  }


  /**
   *  Description of the Method
   *
   * @param  w              Description of the Parameter
   * @param  h              Description of the Parameter
   * @param  buffer         Description of the Parameter
   * @param  cm             Description of the Parameter
   * @return                Description of the Return Value
   * @exception  Exception  Description of the Exception
   */
  public static ImageProcessor createProcessor(int w, int h, DataBuffer buffer,
      ColorModel cm) throws Exception {

    if (buffer.getOffset() != 0) {
      throw new Exception("Expecting BufferData with no offset.");
    }

    switch (buffer.getDataType()) {
      case DataBuffer.TYPE_BYTE:
        return new ByteProcessor(w, h, ((DataBufferByte) buffer).getData(), cm);
      case DataBuffer.TYPE_USHORT:
        return new ShortProcessor(w, h, ((DataBufferUShort) buffer).getData(), cm);
      case DataBuffer.TYPE_SHORT:
        short[] pixels = ((DataBufferShort) buffer).getData();
        for (int i = 0; i < pixels.length; ++i) {
          pixels[i] = (short) (pixels[i] + 32768);
        }
        return new ShortProcessor(w, h, pixels, cm);
      case DataBuffer.TYPE_INT:
        return new FloatProcessor(w, h, ((DataBufferInt) buffer).getData());
      case DataBuffer.TYPE_FLOAT:
      {
        DataBufferFloat dbFloat = (DataBufferFloat) buffer;
        return new FloatProcessor(w, h, dbFloat.getData(), cm);
      }
      case DataBuffer.TYPE_DOUBLE:
        return new FloatProcessor(w, h, ((DataBufferDouble) buffer).getData());
//        throw new Exception("Unsupported pixel type: "
//             + getDataTypeAsString(buffer.getDataType()));
      case DataBuffer.TYPE_UNDEFINED:
        throw new Exception("Pixel type is undefined.");
      default:
        throw new Exception("Unrecognized DataBuffer data type");
    }
  }



  /**
   *  Create instance of ImagePlus from WritableRaster r and ColorModel cm.
   *
   * @param  r              Raster containing pixel data.
   * @param  cm             Image color model (can be null).
   * @return                ImagePlus object created from WritableRaster r and
   *      ColorModel cm
   * @exception  Exception  when enable to create ImagePlus.
   */
  public static ImagePlus create(WritableRaster r, ColorModel cm)
       throws Exception {

    DataBuffer db = r.getDataBuffer();

    int numBanks = db.getNumBanks();
    if (numBanks > 1 && cm == null) {
      throw new Exception("Don't know what to do with image with no " +
          "color model and multiple banks.");
    }

    SampleModel sm = r.getSampleModel();
    int dbType = db.getDataType();
    if (db.getNumBanks() > 1
        || sm.getNumBands() > 1
//         || (cm != null
//         && !(cm instanceof IndexColorModel)
//         && !(cm instanceof FloatDoubleColorModel)
//         && dbType != DataBuffer.TYPE_BYTE
//         && dbType != DataBuffer.TYPE_SHORT
//         && dbType != DataBuffer.TYPE_USHORT)
        ) {
      // If image has multiple banks or multiple color components, assume that it
      // is a color image and relay on AWT for proper decoding.
      BufferedImage bi = new BufferedImage(cm, r, false, null);
      return new ImagePlus(null, new ColorProcessor((Image) bi));
    }
    else {
      if (!(cm instanceof IndexColorModel)) {
        // Image/J (as of version 1.26r) can not properly deal with non color
        // images and ColorModel that is not an instance of IndexedColorModel.
        cm = null;
      }

      ImageProcessor ip = createProcessor(r.getWidth(), r.getHeight(),
          r.getDataBuffer(), cm);
      ImagePlus im = new ImagePlus(null, ip);

      // Add calibration function for 'short' pixels
      if (db.getDataType() == DataBuffer.TYPE_SHORT) {

        Calibration cal = new Calibration(im);
        double[] coeff = new double[2];
        coeff[0] = -32768.0;
        coeff[1] = 1.0;
        cal.setFunction(Calibration.STRAIGHT_LINE, coeff, "gray value");
        im.setCalibration(cal);
      }

      Calibration cal = im.getCalibration();
      im.setCalibration(null);
      ImageStatistics stats = im.getStatistics();
      im.setCalibration(cal);
      ip.setMinAndMax(stats.min, stats.max);
      im.updateImage();

      return im;
    }

  }
}