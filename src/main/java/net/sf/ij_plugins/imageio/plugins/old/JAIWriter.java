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
package net.sf.ij_plugins.imageio.plugins.old;

import com.sun.media.jai.codec.*;
import com.sun.media.jai.codecimpl.TIFFImageDecoder;
import com.sun.media.jai.codecimpl.TIFFImageEncoder;
import ij.ImagePlus;
import ij.io.TiffDecoder;
import ij.measure.Calibration;
import net.sf.ij_plugins.imageio.plugins.BufferedImageCreator;
import net.sf.ij_plugins.imageio.plugins.DescriptionStringCoder;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;


/**
 * Writes images to files using <a href="http://developer.java.sun.com/developer/sampsource/jai/">
 * JAI image I/O codec </a> . If writing to TIFF files, image resolution and Image/J's description
 * string containing calibration information are also saved.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.8 $
 */
public class JAIWriter {

    private static final double TIFF_RATIONAL_SCALE = 1000000;
    private static final String TIFF_FORMAT_NAME = "tiff";
    private static final String DEFAULT_FORMAT_NAME = TIFF_FORMAT_NAME;

    private String formatName = DEFAULT_FORMAT_NAME;
    private ImageEncodeParam encodeParam;


    /**
     * Constructor for the JAIWriter object
     */
    public JAIWriter() {
    }


    /**
     * Gets all supported format names.
     *
     * @return The FormatNames value
     */
    public static String[] getFormatNames() {
        final Enumeration codecs = ImageCodec.getCodecs();
        final ArrayList<String> l = new ArrayList<String>();
        while (codecs.hasMoreElements()) {
            final ImageCodec imageCodec = (ImageCodec) codecs.nextElement();
            l.add(imageCodec.getFormatName());
        }
        return l.toArray(new String[l.size()]);
    }


    /*
     *
     */
    private static long[][] toRational(final double x) {
        return new long[][]{{
                (long) (TIFF_RATIONAL_SCALE * x),
                (long) TIFF_RATIONAL_SCALE}};
    }


    /**
     * Sets the FormatName attribute of the JAIWriter object
     *
     * @param formatName The new FormatName value
     */
    public void setFormatName(final String formatName) {
        this.formatName = formatName;
    }


    /**
     * Gets the FormatName attribute of the JAIWriter object
     *
     * @return The FormatName value
     */
    public String getFormatName() {
        return formatName;
    }


    public void write(final String fileName, final ImagePlus im) throws IOException {
        write(fileName, im, false);
    }

    /**
     * Write image <code>im</code> to file <code>fileName</code>.
     *
     * @param fileName Image output file name.
     * @param im       Image to save.
     * @throws FileNotFoundException    If the file exists but is a directory rather than a regular
     *                                  file, does not exist but cannot be created, or cannot be
     *                                  opened for any other reason.
     * @throws IOException              If there were error writing to file.
     * @throws IllegalArgumentException When trying to save having multiple slices using file format
     *                                  different then TIFF.
     */
    public void write(final String fileName, final ImagePlus im, final boolean prefferBinary)
            throws IOException, IllegalArgumentException {

        boolean successfulWrite = false;
        final File imageFile = new File(fileName);
        final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(imageFile));
        try {
            final ImageEncoder imageEncoder = ImageCodec.createImageEncoder(formatName,
                    outputStream, null);

            if (imageEncoder instanceof TIFFImageEncoder) {
                TIFFEncodeParam param = (TIFFEncodeParam)
                        ((encodeParam instanceof TIFFEncodeParam)
                                ? encodeParam : null); //imageEncoder.getParam());
                if (param == null) {
                    param = new TIFFEncodeParam();
                }

                // Create list of extra images in the file
                final BufferedImage bi = BufferedImageCreator.create(im, 0, prefferBinary);
                final ArrayList<BufferedImage> list = new ArrayList<BufferedImage>();
                for (int i = 1; i < im.getStackSize(); ++i) {
                    list.add(BufferedImageCreator.create(im, i, prefferBinary));
                }
                if (list.size() > 0) {
                    param.setExtraImages(list.iterator());
                }

                // Construct extra TIFF tags
                final ArrayList<TIFFField> extraTags = new ArrayList<TIFFField>();
                final String[] description = {DescriptionStringCoder.encode(im)};
                extraTags.add(new TIFFField(TiffDecoder.IMAGE_DESCRIPTION,
                        TIFFField.TIFF_ASCII, 1, description));

                final Calibration calibration = im.getCalibration();
                if (calibration != null) {
                    if (calibration.pixelWidth != 0.0) {
                        extraTags.add(new TIFFField(TIFFImageDecoder.TIFF_X_RESOLUTION,
                                TIFFField.TIFF_RATIONAL, 1, toRational(1 / calibration.pixelWidth)));
                    }

                    if (calibration.pixelHeight != 0.0) {
                        extraTags.add(new TIFFField(TIFFImageDecoder.TIFF_Y_RESOLUTION,
                                TIFFField.TIFF_RATIONAL, 1, toRational(1 / calibration.pixelHeight)));
                    }

                    final String unitName = calibration.getUnit();
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
                        final int[] unit = {unitCode};
                        extraTags.add(new TIFFField(TIFFImageDecoder.TIFF_RESOLUTION_UNIT,
                                TIFFField.TIFF_SHORT, 1, intsToChars(unit)));
                    }
                }

                param.setExtraFields(extraTags.toArray(new TIFFField[extraTags.size()]));

                imageEncoder.setParam(param);
                imageEncoder.encode(bi);
            } else {
                if (im.getStackSize() > 1) {
                    throw new IllegalArgumentException(formatName.toUpperCase()
                            + " format does not support multi-image files. "
                            + "Image was not saved.");
                }
                final BufferedImage bi = BufferedImageCreator.create(im, 0, prefferBinary);
                imageEncoder.encode(bi);
            }
            successfulWrite = true;
        } finally {
            outputStream.close();
            // Attempt to remove incorrect file fragment
            if (!successfulWrite) {
                imageFile.delete();
            }
        }
    }


    /**
     * This is a copy of a private method of TIFFImageEncoder. Here it is used to convert USHORT to
     * TIFFImageEncoder representation.
     *
     * @param intArray Integer array representing unsigned short values.
     * @return Input array represented as char (16 bit).
     */
    private static char[] intsToChars(final int[] intArray) {
        final int arrayLength = intArray.length;
        final char[] charArray = new char[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            charArray[i] = (char) (intArray[i] & 0x0000ffff);
        }
        return charArray;
    }

    public void setImageEncodeParam(final ImageEncodeParam encodeParam) {
        this.encodeParam = encodeParam;
    }

}
