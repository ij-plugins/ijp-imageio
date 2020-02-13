/*
 *  IJ Plugins
 *  Copyright (C) 2002-2020 Jarek Sacha
 *  Author's email: jpsacha at gmail.com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Latest release available at https://github.com/ij-plugins/ijp-imageio
 */

package net.sf.ij_plugins.imageio;

import com.github.jaiimageio.plugins.tiff.*;
import ij.ImagePlus;
import ij.measure.Calibration;

import javax.imageio.metadata.IIOMetadata;

/**
 * Convert ImagePLus metadata, like calibration, into TIFF metadata.
 *
 * @author Jarek Sacha
 */
public final class TiffMetaDataFactory {

    public static IIOMetadata createFrom(final ImagePlus image) {
        final Calibration calibration = image.getCalibration();

        final TIFFDirectory tIFFDirectory = new TIFFDirectory(new TIFFTagSet[]{BaselineTIFFTagSet.getInstance()}, null);
        final TIFFTagSet tagSet = BaselineTIFFTagSet.getInstance();
        if (calibration != null) {
            if (calibration.scaled()) {
                final long[][] xRes = new long[1][2];
                final long[][] yRes = new long[1][2];
                final double xScale = 1.0 / calibration.pixelWidth;
                final double yScale = 1.0 / calibration.pixelHeight;
                double scale = 1000000.0;
                if (xScale > 1000.0) {
                    scale = 1000.0;
                }
                xRes[0][1] = (long) scale;
                xRes[0][0] = (long) (xScale * scale);
                yRes[0][1] = (long) scale;
                yRes[0][0] = (long) (yScale * scale);

                // X resolution
                tIFFDirectory.addTIFFField(new TIFFField(
                        tagSet.getTag(BaselineTIFFTagSet.TAG_X_RESOLUTION), TIFFTag.TIFF_RATIONAL, 1, xRes));

                // Y resolution
                tIFFDirectory.addTIFFField(new TIFFField(
                        tagSet.getTag(BaselineTIFFTagSet.TAG_Y_RESOLUTION), TIFFTag.TIFF_RATIONAL, 1, yRes));
                // Resolution unit
                switch (calibration.getUnit()) {
                    case "inch":
                        tIFFDirectory.addTIFFField(new TIFFField(
                                tagSet.getTag(BaselineTIFFTagSet.TAG_RESOLUTION_UNIT), BaselineTIFFTagSet.RESOLUTION_UNIT_INCH));
                        break;
                    case "cm":
                        tIFFDirectory.addTIFFField(new TIFFField(
                                tagSet.getTag(BaselineTIFFTagSet.TAG_RESOLUTION_UNIT), BaselineTIFFTagSet.RESOLUTION_UNIT_CENTIMETER));
                        break;
                    default:
                        tIFFDirectory.addTIFFField(new TIFFField(
                                tagSet.getTag(BaselineTIFFTagSet.TAG_RESOLUTION_UNIT), BaselineTIFFTagSet.RESOLUTION_UNIT_NONE));
                        break;

                    // TODO treat also: "mm", "micron", "um", "\u00B5m", m
                }

            }

            //all other calibration information into image description
            tIFFDirectory.addTIFFField(new TIFFField(
                    tagSet.getTag(BaselineTIFFTagSet.TAG_IMAGE_DESCRIPTION),
                    TIFFTag.TIFF_ASCII,
                    1,
                    new String[]{DescriptionStringCoder.encode(image)}));

            tIFFDirectory.addTIFFField(new TIFFField(
                    tagSet.getTag(BaselineTIFFTagSet.TAG_SOFTWARE),
                    TIFFTag.TIFF_ASCII, 1, new String[]{"ij-plugins/ijp-imageio"}));

        }

        return tIFFDirectory.getAsMetadata();

    }

//    static private String encodeDescriptionString(final ImagePlus image) {
//        final FileSaver fileSaver = new FileSaver(image);
//        final String desc = fileSaver.getDescriptionString();
//        // ImageJ may append a null char et the end, check and remove.
//        if (desc.charAt(desc.length() - 1) != (char) 0) {
//            return desc;
//        } else {
//            return desc.substring(0, desc.length() - 1);
//        }
//    }
}
