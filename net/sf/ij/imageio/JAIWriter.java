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

import non_com.media.jai.codec.ImageEncoder;
import non_com.media.jai.codec.TIFFImage;
import non_com.media.jai.codec.TIFFImageEncoder;
import non_com.media.jai.codec.ImageCodec;
import non_com.media.jai.codec.TIFFEncodeParam;

import ij.ImagePlus;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 *  Writes images to files using JAI 1.1.1 codecs.
 *
 * @author     Jarek Sacha
 * @created    February 18, 2002
 * @version    $Revision: 1.3 $
 */

public class JAIWriter {

  private final static boolean DEBUG = true;

  private final static String TIFF_FORMAT_NAME = "tiff";
  private final static String DEFAULT_FORMAT_NAME = TIFF_FORMAT_NAME;

  private String formatName = DEFAULT_FORMAT_NAME;


  /**
   *  Constructor for the JAIWriter object
   */
  public JAIWriter() {
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
   *  Gets the FormatNames attribute of the JAIWriter object
   *
   * @return    The FormatNames value
   */
  public String[] getFormatNames() {
    Enumeration codecs = ImageCodec.getCodecs();
    ArrayList l = new ArrayList();
    while (codecs.hasMoreElements()) {
      ImageCodec imageCodec = (ImageCodec) codecs.nextElement();
      l.add(imageCodec.getFormatName());
    }
    return (String[]) l.toArray(new String[l.size()]);
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
        BufferedImage bi = BufferedImageCreator.create(im, 0);
        if (DEBUG) {
          new ImagePlus("BufferedImage", bi).show();
        }
        ArrayList list = new ArrayList();
        for (int i = 1; i < im.getStackSize(); ++i) {
          list.add(BufferedImageCreator.create(im, i));
        }

        TIFFEncodeParam param = (TIFFEncodeParam) imageEncoder.getParam();
        if (param == null) {
          param = new TIFFEncodeParam();
        }
        if (list.size() > 0) {
          param.setExtraImages(list.iterator());
          imageEncoder.setParam(param);
        }

        imageEncoder.encode(bi);
      }
      else {
        if (im.getStackSize() > 1) {
          throw new IllegalArgumentException(formatName.toUpperCase()
               + " format does not support multi-image files. "
               + "Image was not saved.");
        }
        BufferedImage bi = BufferedImageCreator.create(im, 0);
        if (DEBUG) {
          new ImagePlus("BufferedImage", bi).show();
        }
        imageEncoder.encode(bi);
      }
    }
    finally {
      outputStream.close();
    }
  }
}
