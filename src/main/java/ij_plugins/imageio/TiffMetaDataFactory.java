/*
 *  IJ Plugins
 *  Copyright (C) 2002-2022 Jarek Sacha
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

package ij_plugins.imageio;

import ij.ImagePlus;
import ij.measure.Calibration;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.tiff.*;

/**
 * Convert ImagePLus metadata, like calibration, into TIFF metadata.
 *
 * @author Jarek Sacha
 */
public final class TiffMetaDataFactory {

    /**
     * Create IIOMetadata from an image and its calibration information
     *
     * @param image source image
     * @return metadata corresponding to the source image
     */
    public static IIOMetadata createFrom(final ImagePlus image) {

        final Calibration calibration = image.getCalibration();

        var dir = new TIFFDirectory(new TIFFTagSet[]{BaselineTIFFTagSet.getInstance()}, null);
        var tagSet = BaselineTIFFTagSet.getInstance();
        if (calibration != null) {
            if (calibration.scaled()) {
                // Resolution unit
                var resTag = tagSet.getTag(BaselineTIFFTagSet.TAG_RESOLUTION_UNIT);
                switch (calibration.getUnit().toLowerCase()) {
                    case "inch":
                        dir.addTIFFField(new TIFFField(resTag, BaselineTIFFTagSet.RESOLUTION_UNIT_INCH));
                        break;
                    case "cm":
                    case "centimeter":
                        dir.addTIFFField(new TIFFField(resTag, BaselineTIFFTagSet.RESOLUTION_UNIT_CENTIMETER));
                        break;

                    // To maintain compatibility with the ImageJ do not translate units not supported by TIFF,
                    //   use no unit and as unit in comments
                    default:
                        dir.addTIFFField(new TIFFField(resTag, BaselineTIFFTagSet.RESOLUTION_UNIT_NONE));
                }

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
                dir.addTIFFField(new TIFFField(
                        tagSet.getTag(BaselineTIFFTagSet.TAG_X_RESOLUTION), TIFFTag.TIFF_RATIONAL, 1, xRes));

                // Y resolution
                dir.addTIFFField(new TIFFField(
                        tagSet.getTag(BaselineTIFFTagSet.TAG_Y_RESOLUTION), TIFFTag.TIFF_RATIONAL, 1, yRes));


            }

            //all other calibration information into image description
            dir.addTIFFField(new TIFFField(
                    tagSet.getTag(BaselineTIFFTagSet.TAG_IMAGE_DESCRIPTION),
                    TIFFTag.TIFF_ASCII,
                    1,
                    new String[]{DescriptionStringCoder.encode(image)}));

            dir.addTIFFField(new TIFFField(
                    tagSet.getTag(BaselineTIFFTagSet.TAG_SOFTWARE),
                    TIFFTag.TIFF_ASCII, 1, new String[]{"ij-plugins/ijp-imageio"}));

        }

        return dir.getAsMetadata();
    }
}
