/***
 * Image/J Plugins
 * Copyright (C) 2002-2004 Jarek Sacha
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
import ij.io.TiffDecoder;
import ij.measure.Calibration;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import non_com.media.jai.codec.*;

/**
 *  Writes images to files using <a href="http://developer.java.sun.com/developer/sampsource/jai/">
 *  JAI image I/O codec </a> . If writing to TIFF files, image resolution and
 *  Image/J's description string containing calibration information are also
 *  saved.
 *
 * @author     Jarek Sacha
 * @version    $Revision: 1.2 $
 */
public class JAIWriter {

  private final static double TIFF_RATIONAL_SCALE = 1000000;
  private final static String TIFF_FORMAT_NAME = "tiff";
  private final static String DEFAULT_FORMAT_NAME = TIFF_FORMAT_NAME;

  private String formatName = DEFAULT_FORMAT_NAME;
  private ImageEncodeParam encodeParam;


  /**  Constructor for the JAIWriter object */
  public JAIWriter() {
  }


  /**
   *  Gets all supported format names.
   *
   * @return    The FormatNames value
   */
  public static String[] getFormatNames() {
    Enumeration codecs = ImageCodec.getCodecs();
    ArrayList l = new ArrayList();
    while (codecs.hasMoreElements()) {
      ImageCodec imageCodec = (ImageCodec) codecs.nextElement();
      l.add(imageCodec.getFormatName());
    }
    return (String[]) l.toArray(new String[l.size()]);
  }


  /*
   *
   */
  private static long[][] toRational(double x) {
    long[][] r = {{
      (long) (TIFF_RATIONAL_SCALE * x),
      (long) TIFF_RATIONAL_SCALE}};
    return r;
  }


  /**
   *  Sets the FormatName attribute of the JAIWriter object
   *
   * @param  formatName  The new FormatName value
   */
  public void setFormatName(String formatName) {
    this.formatName = formatName;
  }


  /**
   *  Gets the FormatName attribute of the JAIWriter object
   *
   * @return    The FormatName value
   */
  public String getFormatName() {
    return formatName;
  }


  /**
   *  Write image <code>im</code> to file <code>fileName</code>.
   *
   * @param  fileName                      Image output file name.
   * @param  im                            Image to save.
   * @exception  FileNotFoundException     If the file exists but is a directory
   *      rather than a regular file, does not exist but cannot be created, or
   *      cannot be opened for any other reason.
   * @exception  IOException               If there were error writing to file.
   * @exception  IllegalArgumentException  When trying to save having multiple
   *      slices using file format different then TIFF.
   */
  public void write(String fileName, ImagePlus im)
      throws FileNotFoundException, IOException, IllegalArgumentException {
    FileOutputStream outputStream = new FileOutputStream(fileName);
    try {
      ImageEncoder imageEncoder = ImageCodec.createImageEncoder(formatName,
          outputStream, null);

      if (imageEncoder instanceof TIFFImageEncoder) {
        TIFFEncodeParam param = (TIFFEncodeParam)
            ((encodeParam instanceof TIFFEncodeParam)
            ? encodeParam : null); //imageEncoder.getParam());
        if (param == null) {
          param = new TIFFEncodeParam();
        }
//        param.setCompression(TIFFEncodeParam.COMPRESSION_GROUP4);
//        param.setCompression(TIFFEncodeParam.COMPRESSION_JPEG_TTN2);
//        param.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
//        param.setDeflateLevel(9);

        // Create list of extra images in the file
        BufferedImage bi = BufferedImageCreator.create(im, 0);
        ArrayList list = new ArrayList();
        for (int i = 1; i < im.getStackSize(); ++i) {
          list.add(BufferedImageCreator.create(im, i));
        }
        if (list.size() > 0) {
          param.setExtraImages(list.iterator());
        }

        // Construct extra TIFF tags
        ArrayList extraTags = new ArrayList();
        String[] desciption = {DescriptionStringCoder.encode(im)};
        extraTags.add(new TIFFField(TiffDecoder.IMAGE_DESCRIPTION,
            TIFFField.TIFF_ASCII, 1, desciption));

        Calibration calib = im.getCalibration();
        if (calib != null) {
          if (calib.pixelWidth != 0.0) {
            extraTags.add(new TIFFField(TIFFImageDecoder.TIFF_X_RESOLUTION,
                TIFFField.TIFF_RATIONAL, 1, toRational(1 / calib.pixelWidth)));
          }

          if (calib.pixelHeight != 0.0) {
            extraTags.add(new TIFFField(TIFFImageDecoder.TIFF_Y_RESOLUTION,
                TIFFField.TIFF_RATIONAL, 1, toRational(1 / calib.pixelHeight)));
          }

          String unitName = calib.getUnit();
          short unitCode = 0;
          if (unitName == null || unitName.trim().length() == 0) {
            // no meaningful units
            unitCode = 1;
          } else if (unitName.compareToIgnoreCase("inch") == 0) {
            unitCode = 2;
          } else if (unitName.compareToIgnoreCase("cm") == 0) {
            unitCode = 3;
          }

          if (unitCode > 0) {
            int[] unit = {(int) unitCode};
            extraTags.add(new TIFFField(TIFFImageDecoder.TIFF_RESOLUTION_UNIT,
                TIFFField.TIFF_SHORT, 1, intsToChars(unit)));
          }
        }

        param.setExtraFields(
            (TIFFField[]) extraTags.toArray(new TIFFField[extraTags.size()]));

        imageEncoder.setParam(param);
        imageEncoder.encode(bi);
      } else {
        if (im.getStackSize() > 1) {
          throw new IllegalArgumentException(formatName.toUpperCase()
              + " format does not support multi-image files. "
              + "Image was not saved.");
        }
        BufferedImage bi = BufferedImageCreator.create(im, 0);
        imageEncoder.encode(bi);
      }
    } finally {
      outputStream.close();
    }
  }


  /**
   *  This is a copy of a private method of TIFFImageEncoder. Here it is used to
   *  convert USHORT to TIFFImageEncoder representation.
   *
   * @param  intArray  Integer array representing unsigned short values.
   * @return           Input array represented as char (16 bit).
   */
  private final static char[] intsToChars(int[] intArray) {
    int arrayLength = intArray.length;
    char[] charArray = new char[arrayLength];
    for (int i = 0; i < arrayLength; i++) {
      charArray[i] = (char) (intArray[i] & 0x0000ffff);
    }
    return charArray;
  }

  public void setImageEncodeParam(ImageEncodeParam encodeParam) {
    this.encodeParam = encodeParam;
  }

}
