/*
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

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.filechooser.FileFilter;

import non_com.media.jai.codec.FileSeekableStream;
import non_com.media.jai.codec.ImageCodec;

/**
 *  File filter that detects image files supported by registered JAI codecs.
 *  File types are determined be magic number in the header rather then file
 *  extension.
 *
 * @author     Jarek Sacha
 * @created    November 6, 2000
 */

public class JAIFileFilter extends FileFilter {

  /**  Files smaller then min size are ignored by the filter. */
  public final static int MIN_IMAGE_FILE_SIZE = 8;

  private String codecName = null;
  private String decription = "All Supported Images";
  private ImageCodec activeCodecs[] = null;
  private int maxHeaderSize = 0;
  private byte[] headerBytes = null;


  /**  Create file filter accepting all images supported by registered JAI codecs. */
  public JAIFileFilter() {
    setupFilter(null);
  }


  /**
   *  Create file filter accepting images supported by given codec.
   *
   * @param  codecName  Codec name.
   */
  public JAIFileFilter(String codecName) {
    setupFilter(codecName);
  }


  /*
   *
   */
  private static void readHeaderSample(File file, byte[] headerBytes)
       throws Exception {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      fis.read(headerBytes);
    }
    catch (Exception ex) {
      throw ex;
    }
    finally {
      if (fis != null) {
        fis.close();
      }
    }
  }


  /**
   *  Gets the Description attribute of the ExtensionFileFilter object
   *
   * @return    The Description value
   */
  public String getDescription() {
    return decription;
  }


  /**
   *  Gets the CodecName attribute of the JAIFileFilter object. May return null
   *  if no codec is selected (all files).
   *
   * @return    The CodecName value
   */
  public String getCodecName() {
    return codecName;
  }


  /**
   *  Whether the given file is accepted by this filter.
   *
   * @param  file  File.
   * @return       true is it is a directory or a file that can be accessed by
   *      associated codec.
   */
  public boolean accept(File file) {
    if (file == null || !file.exists() || !file.canRead()) {
      return false;
    }
    if (file.isDirectory()) {
      return true;
    }
    if (file.length() < MIN_IMAGE_FILE_SIZE) {
      return false;
    }

    // Read file header
    try {
      readHeaderSample(file, headerBytes);
    }
    catch (Exception ex) {
      return false;
    }

    // Look for decoder
    for (int i = 0; i < activeCodecs.length; ++i) {
      if (activeCodecs[i].isFormatRecognized(headerBytes)) {
        return true;
      }
    }

    return false;
  }


  /*
   *
   */
  private void setupFilter(String codecName) {
    this.codecName = codecName;
    decription = "All Supported Images";
    activeCodecs = null;

    // Get handles filter codecs
    if (codecName == null) {
      // All suported formats.
      Enumeration codecEnumeration = ImageCodec.getCodecs();
      ArrayList codecArray = new ArrayList();
      while (codecEnumeration.hasMoreElements()) {
        codecArray.add(codecEnumeration.nextElement());
      }
      activeCodecs = (ImageCodec[]) codecArray.toArray(
          new ImageCodec[codecArray.size()]);
    }
    else {
      ImageCodec codec = ImageCodec.getCodec(codecName);
      if (codec != null) {
        activeCodecs = new ImageCodec[1];
        activeCodecs[0] = codec;
        this.codecName = codecName;
        decription = codecName.toUpperCase();
      }
    }

    if (activeCodecs == null) {
      throw new RuntimeException("Unable to find codecs for: " + codecName);
    }

    // Find how large header size is needed for file type detection.
    for (int i = 0; i < activeCodecs.length; ++i) {
      int h = activeCodecs[i].getNumHeaderBytes();
      if (h == 0) {
        throw new RuntimeException("Codec "
            + activeCodecs[i].getFormatName()
            + " unable to recognize its files by header.");
      }
      if (h > maxHeaderSize) {
        maxHeaderSize = h;
      }
    }

    if (maxHeaderSize > MIN_IMAGE_FILE_SIZE) {
      throw new RuntimeException(
          "MIN_IMAGE_FILE_SIZE set too low, need to be at least "
          + maxHeaderSize);
    }

    headerBytes = new byte[maxHeaderSize];
  }

//  public String[] getExpectedNameExtensions() {
//    if( activeCodecs == null )
//      return null;
//    for(int i=0; i<activeCodecs.length; ++i) {
//      activeCodecs[0].
//    }
//  }
}

