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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.io.Opener;
import ij.io.TiffDecoder;
import ij.measure.Calibration;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;

import non_com.media.jai.codec.FileSeekableStream;
import non_com.media.jai.codec.ImageCodec;
import non_com.media.jai.codec.ImageDecoder;
import non_com.media.jai.codec.TIFFDirectory;
import non_com.media.jai.codec.TIFFField;
import non_com.media.jai.codec.TIFFImage;
import non_com.media.jai.codec.TIFFImageDecoder;

/**
 *  Read image files using JAI image I/O codec
 *  (http://developer.java.sun.com/developer/sampsource/jai/) and convert them
 *  to Image/J representation.
 *
 * @author     Jarek Sacha
 * @created    January 11, 2002
 * @version    $Revision: 1.4 $
 */
public class JAIReader {

  /**
   *  Read only the first image in the <code>file</code>.
   *
   * @param  file           Image file.
   * @return                Image decoded as BufferedImage.
   * @exception  Exception  If file is not in a supported image format or in
   *      case of I/O error.
   */
  public static ImageInfo readFirstImageAndInfo(File file) throws Exception {

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

    ImageInfo imageInfo = new ImageInfo();
    imageInfo.numberOfPages = decoder.getNumPages();
    imageInfo.codecName = decoders[0];

    if (renderedImage instanceof Image) {
      System.out.println("renderedImage instanceof Image");
      System.out.println(renderedImage.getClass().getName());
      imageInfo.previewImage = (Image) renderedImage;
    }
    else {
      ColorModel cm = renderedImage.getColorModel();
      if (cm == null) {
        WritableRaster writableRaster
             = ImagePlusCreator.forceTileUpdate(renderedImage);
        ImagePlus imagePlus = ImagePlusCreator.create(writableRaster, null);
        imageInfo.previewImage = imagePlus.getImage();
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

        imageInfo.previewImage = new BufferedImage(cm, writableRaster, false,
            null);
      }
    }

    return imageInfo;
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
  public static ImagePlus[] read(File file) throws Exception {

    // Find matching decoders
    FileSeekableStream fss = new FileSeekableStream(file);
    String[] decoders = ImageCodec.getDecoderNames(fss);
    if (decoders == null || decoders.length == 0) {
      throw new Exception("Unsupported file format. "
          + "Cannot find decoder capable of reading: " + file.getName());
    }

    String decoderName = decoders[0];

    // Create decoder
    ImageDecoder decoder = ImageCodec.createImageDecoder(decoderName, fss, null);

    // Get number of sub images
    int nbPages = decoder.getNumPages();
    if (nbPages < 1) {
      throw new Exception("Image decoding problem. "
          + "Image file has less then 1 page. Nothing to decode.");
    }

    // Iterate through pages
    IJ.showProgress(0);
    ArrayList imageList = new ArrayList();
    for (int i = 0; i < nbPages; ++i) {
      if (i != 0) {
        IJ.showStatus("Reading page " + i);
      }
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

      WritableRaster wr = ImagePlusCreator.forceTileUpdate(ri);

      ImagePlus im = null;
      if (decoderName.equalsIgnoreCase("GIF")
          || decoderName.equalsIgnoreCase("JPEG")) {
        // Convert the way ImageJ does (ij.io.Opener.openJpegOrGif())
        BufferedImage bi = new BufferedImage(ri.getColorModel(), wr, false, null);
        im = new ImagePlus(file.getName(), bi);
        if (im.getType() == ImagePlus.COLOR_RGB) {
          // Convert RGB to gray if all bands are equal
          Opener.convertGrayJpegTo8Bits(im);
        }
      }
      else {
        im = ImagePlusCreator.create(wr, ri.getColorModel());
        im.setTitle(file.getName() + " [" + (i + 1) + "/" + nbPages + "]");

        if (im.getType() == ImagePlus.COLOR_RGB) {
          // Convert RGB to gray if all bands are equal
          Opener.convertGrayJpegTo8Bits(im);
        }

        // Extract TIFF tags
        if (ri instanceof TIFFImage) {
          TIFFImage ti = (TIFFImage) ri;
          try {
            Object o = ti.getProperty("tiff_directory");
            if (o instanceof TIFFDirectory) {
              TIFFDirectory dir = (TIFFDirectory) o;

              // ImageJ description string
              TIFFField descriptionField
                   = dir.getField(TiffDecoder.IMAGE_DESCRIPTION);
              if (descriptionField != null) {
                try {
                  DescriptionStringCoder.decode(
                      descriptionField.getAsString(0), im);
                }
                catch(Exception ex) {
                  ex.printStackTrace();
                }
              }

              Calibration c = im.getCalibration();
              if (c == null) {
                c = new Calibration(im);
              }

              // X resolution
              TIFFField xResField = dir.getField(TIFFImageDecoder.TIFF_X_RESOLUTION);
              if (xResField != null) {
                double xRes = xResField.getAsDouble(0);
                if (xRes != 0) {
                  c.pixelWidth = 1 / xRes;
                }
              }

              // Y resolution
              TIFFField yResField = dir.getField(TIFFImageDecoder.TIFF_Y_RESOLUTION);
              if (yResField != null) {
                double yRes = yResField.getAsDouble(0);
                if (yRes != 0) {
                  c.pixelHeight = 1 / yRes;
                }
              }

              // Resolution unit
              TIFFField resolutionUnitField = dir.getField(
                  TIFFImageDecoder.TIFF_RESOLUTION_UNIT);
              if (resolutionUnitField != null) {
                int resolutionUnit = resolutionUnitField.getAsInt(0);
                if (resolutionUnit == 1 && c.getUnit() == null) {
                  // no meaningful units
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
          }
          catch (NegativeArraySizeException ex) {
            // my be thrown by ti.getPrivateIFD(8)
            ex.printStackTrace();
          }
        }
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


  /*
   *  Basic image information.
   */
  public static class ImageInfo {
    public Image previewImage;
    public int numberOfPages;
    public String codecName;
  }
}
