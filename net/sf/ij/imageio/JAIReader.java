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
package net.sf.ij.imageio;

import FileSeekableStream;
import ImageCodec;
import ImageDecodeParam;
import ImageDecoder;
import ImageDecoderImpl;
import SimpleRenderedImage;
import TIFFDecodeParam;
import TIFFDirectory;
import TIFFField;
import TIFFImage;
import TIFFImageDecoder;

//import com.sun.media.jai.codec.FileSeekableStream;
//import com.sun.media.jai.codec.ImageCodec;
//import com.sun.media.jai.codec.ImageDecoder;

import ij.*;
import ij.io.OpenDialog;
import ij.measure.Calibration;

import ij.plugin.PlugIn;
import ij.process.*;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.ImageIcon;

/**
 *  Read image files using JAI image I/O codec
 *  (http://developer.java.sun.com/developer/sampsource/jai/) and convert them
 *  to Image/J representation.
 *
 * @author     Jarek Sacha
 * @created    January 11, 2002
 * @version    $Revision: 1.4 $
 */
public class JAIReader implements PlugIn {

  /**
   */
  private boolean debugMode;


  /**
   *  Read only the first image in the <code>file</code>.
   *
   * @param  file           Image file.
   * @return                Image decoded as BufferedImage.
   * @exception  Exception  If file is not in a supported image format or in
   *      case of I/O error.
   */
  public static Image readFirstAsImage(File file) throws Exception {

    // Find matching decoders
    FileSeekableStream fss = new FileSeekableStream(file);
    String[] decoders = ImageCodec.getDecoderNames(fss);
    if (decoders == null || decoders.length == 0) {
      throw new Exception("Unsupported file format. "
           + "Cannot find decoder capable of reading: " + file.getName());
    }

    // Create decoder
    ImageDecoder decoder = ImageCodec.createImageDecoder(decoders[0], fss, null);

    RenderedImage renderedImage = decoder.decodeAsRenderedImage();

    ColorModel cm = renderedImage.getColorModel();
    if (cm == null) {
      WritableRaster writableRaster = forceTileUpdate(renderedImage);
      ImagePlus imagePlus = createImagePlus(writableRaster, null);
      return imagePlus.getImage();
    }
    else {
      Raster raster = renderedImage.getData();
      WritableRaster writableRaster = null;
      if (raster instanceof WritableRaster) {
        writableRaster = (WritableRaster) raster;
      }
      else {
        writableRaster = raster.createCompatibleWritableRaster();
      }

      return new BufferedImage(cm, writableRaster, false, null);
    }
  }


  /**
   *  Gets the dataTypeAsString attribute of the JAIOpener object
   *
   * @param  id             Description of the Parameter
   * @return                The dataTypeAsString value
   * @exception  Exception  Description of the Exception
   */
  private static String getDataTypeAsString(int id) throws Exception {
    switch (id) {
      case DataBuffer.TYPE_BYTE:
        return "byte";
      case DataBuffer.TYPE_DOUBLE:
        return "double";
      case DataBuffer.TYPE_FLOAT:
        return "float";
      case DataBuffer.TYPE_INT:
        return "int";
      case DataBuffer.TYPE_SHORT:
        return "short";
      case DataBuffer.TYPE_UNDEFINED:
        return "undefined";
      case DataBuffer.TYPE_USHORT:
        return "ushort";
      default:
        throw new Exception("Unrecognized DataBuffer data type");
    }
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
  private static ImageProcessor createProcessor(int w, int h, DataBuffer buffer,
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
      case DataBuffer.TYPE_DOUBLE:
        throw new Exception("Unsupported pixel type: "
             + getDataTypeAsString(buffer.getDataType()));
      case DataBuffer.TYPE_UNDEFINED:
        throw new Exception("Pixel type is undefined.");
      default:
        throw new Exception("Unrecognized DataBuffer data type");
    }
  }


  /**
   *  Constructor for the printInfo object
   *
   * @param  ri             Description of the Parameter
   * @exception  Exception  Description of the Exception
   */
  private static void printInfo(RenderedImage ri) throws Exception {
    IJ.write("");
    IJ.write("MinTileX       :  " + ri.getMinTileX());
    IJ.write("MinTileY       :  " + ri.getMinTileX());
    IJ.write("NumXTiles      :  " + ri.getNumXTiles());
    IJ.write("NumYTiles      :  " + ri.getNumYTiles());
    IJ.write("TileHeight     :  " + ri.getTileHeight());
    IJ.write("TileWidth      :  " + ri.getTileWidth());
    IJ.write("TileGridXOffset:  " + ri.getTileGridXOffset());
    IJ.write("TileGridYOffset:  " + ri.getTileGridYOffset());

    Raster r = ri.getData();
    IJ.write("Width    : " + r.getWidth());
    IJ.write("Height   : " + r.getHeight());
    IJ.write("MinX     : " + r.getMinX());
    IJ.write("MinY     : " + r.getMinY());
    IJ.write("NumBands : " + r.getNumBands());
    IJ.write("NumDataElements       : " + r.getNumDataElements());
    IJ.write("SampleModelTranslateX : " + r.getSampleModelTranslateX());
    IJ.write("SampleModelTranslateY : " + r.getSampleModelTranslateY());

    DataBuffer db = r.getDataBuffer();
    IJ.write("Offset: " + db.getOffset());
    String typeName = getDataTypeAsString(db.getDataType());
    IJ.write("Type:   " + typeName);
    IJ.write("Number of banks:   " + db.getNumBanks());

    ColorModel cm = ri.getColorModel();
    if (cm == null) {
      IJ.write("No color model.");
    }
    else {
      IJ.write("Number of color components: " +
          cm.getColorSpace().getNumComponents());
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
  private static ImagePlus createImagePlus(WritableRaster r, ColorModel cm)
       throws Exception {

    DataBuffer db = r.getDataBuffer();

    int numBanks = db.getNumBanks();
    if (numBanks > 1 && cm == null) {
      throw new Exception("Don't know what to do with image with no " +
          "color model and multiple banks.");
    }

    if (db.getNumBanks() > 1 ||
        (cm != null && cm.getColorSpace().getNumComponents() != 1)) {
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


  /**
   *  Attempt to combine images into a single stack. Images can be combined into
   *  a stack if all of them are single slice images of the same type and
   *  dimensions.
   *
   * @param  images  Array of images.
   * @return         Input images combined into a stack. Return null if images
   *      cannot be combined.
   */
  private static ImagePlus combineImages(ImagePlus[] images) {
    if (images == null || images.length <= 1) {
      return null;
    }

    if (images[0].getStackSize() != 1) {
      return null;
    }

    int fileType = images[0].getFileInfo().fileType;
    int w = images[0].getWidth();
    int h = images[0].getHeight();
    ImageStack stack = images[0].getStack();
    for (int i = 1; i < images.length; ++i) {
      ImagePlus im = images[i];
      if (im.getStackSize() != 1) {
        return null;
      }
      if (fileType == im.getFileInfo().fileType
           && w == im.getWidth() && h == im.getHeight()) {
        stack.addSlice(null, im.getProcessor().getPixels());
      }
      else {
        return null;
      }
    }

    images[0].setStack(images[0].getTitle(), stack);
    return images[0];
  }


  /**
   *  Force Rendered image to set all the tails that it may have. In multi-tile
   *  images not all tiles may be updated when a RenderedImage is created.
   *
   * @param  ri  image that may need tile update.
   * @return     Description of the Returned Value
   */
  private static WritableRaster forceTileUpdate(RenderedImage ri) {
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
   *  Sets the DebugMode attribute of the JAIOpener object
   *
   * @param  debugMode  The new DebugMode value
   */
  public void setDebugMode(boolean debugMode) {
    this.debugMode = debugMode;
  }


  /**
   *  Gets the DebugMode attribute of the JAIOpener object
   *
   * @return    The DebugMode value
   */
  public boolean isDebugMode() {
    return debugMode;
  }


  /**
   *  Convenience method that simplifies execution from within Image/J.
   *  Implementation of the method in ij.plogin.PlugIn interface.
   *
   * @param  arg  Not used, required by the interface specification.
   */
  public void run(String arg) {

    OpenDialog openDialog = new OpenDialog("Open...", null);
    if (openDialog.getFileName() == null) {
      // No selection
      return;
    }

    File file = new File(openDialog.getDirectory(), openDialog.getFileName());
    IJ.showStatus("Opening: " + file.getName());

    try {
      ImagePlus[] images = read(file);
      if (images != null) {
        for (int i = 0; i < images.length; ++i) {
          images[i].show();
        }
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      IJ.showMessage("JAIReader", "Error opening file: "
           + openDialog.getFileName() + ".\n\n" + ex.getMessage());
    }
  }


  /**
   *  Open image in the file using registered codecs. A file may contain
   *  multiple images. If all images in the file are of the same type and size
   *  they will be combines into single stack within ImagesPlus object returned
   *  as the first an only element of the image array.
   *
   * @param  file           File to open image from.
   * @return                Array of images contained in the file.
   * @exception  Exception  when unable to read image from the specified file.
   */
  public ImagePlus[] read(File file) throws Exception {

    // Find matching decoders
    FileSeekableStream fss = new FileSeekableStream(file);
    String[] decoders = ImageCodec.getDecoderNames(fss);
    if (decoders == null || decoders.length == 0) {
      throw new Exception("Unsupported file format. "
           + "Cannot find decoder capable of reading: " + file.getName());
    }

    // Create decoder
    ImageDecoder decoder = ImageCodec.createImageDecoder(decoders[0], fss, null);

    // Get number of subimages
    int nbPages = decoder.getNumPages();
    if (nbPages < 1) {
      throw new Exception("Image decoding problem. "
           + "Image file has less then 1 page. Nothing to decode.");
    }

    // Iterate through pages
    IJ.showProgress(0);
    ArrayList imageList = new ArrayList();
    for (int i = 0; i < nbPages; ++i) {
      RenderedImage ri = null;
      try {
        ri = decoder.decodeAsRenderedImage(i);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        String msg = ex.getMessage();
        if (msg == null || msg.trim().length() < 1) {
          msg = "Error decoding rendered image.";
        }
        throw new Exception(msg);
      }
      if (debugMode) {
        printInfo(ri);
      }
      WritableRaster wr = forceTileUpdate(ri);

      ImagePlus im = createImagePlus(wr, ri.getColorModel());
      im.setTitle(file.getName() + " [" + (i + 1) + "/" + nbPages + "]");

      // Extract TIFF tags
      if (ri instanceof TIFFImage) {
        TIFFImage ti = (TIFFImage) ri;
        TIFFDirectory dir = ti.getPrivateIFD(8);

        Calibration c = im.getCalibration();
        if (c == null) {
          c = new Calibration(im);
        }

        TIFFField xResField = dir.getField(TIFFImageDecoder.TIFF_X_RESOLUTION);
        if (xResField != null) {
          double xRes = xResField.getAsDouble(0);
          if (xRes != 0) {
            c.pixelWidth = 1 / xRes;
          }
        }

        TIFFField yResField = dir.getField(TIFFImageDecoder.TIFF_Y_RESOLUTION);
        if (yResField != null) {
          double yRes = yResField.getAsDouble(0);
          if (yRes != 0) {
            c.pixelHeight = 1 / yRes;
          }
        }

        TIFFField resolutionUnitField = dir.getField(
            TIFFImageDecoder.TIFF_RESOLUTION_UNIT);
        if (resolutionUnitField != null) {
          int resolutionUnit = resolutionUnitField.getAsInt(0);
          if (resolutionUnit == 1 && c.getUnit() == null) {
            // no meningful units
            c.setUnit(" ");
          }
          else if (resolutionUnit == 2) {
            c.setUnit("inch");
          }
          else if (resolutionUnit == 3) {
            c.setUnit("cm");
          }
        }

        im.setCalibration(c);
      }

      imageList.add(im);
      IJ.showProgress((double) (i + 1) / nbPages);
    }
    IJ.showProgress(1);

    ImagePlus[] images = (ImagePlus[]) imageList.toArray(
        new ImagePlus[imageList.size()]);

    if (nbPages == 1) {
      // Do not use page numbers in image name
      images[0].setTitle(file.getName());
    }
    else {
      // Attempt to combine images into a single stack.
      ImagePlus im = combineImages(images);
      if (im != null) {
        im.setTitle(file.getName());
        images = new ImagePlus[1];
        images[0] = im;
      }
    }

    return images;
  }
}
