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

import ij.ImagePlus;
import ij.io.FileInfo;
import ij.measure.Calibration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Decodes and encodes description strings used by ImageJ to store extra image
 * info in TIFF description field.
 * 
 * @author Jarek Sacha
 * @version $Revision: 1.3 $
 */

public class DescriptionStringCoder {

    private DescriptionStringCoder() {
    }


    /**
     * Creates an optional image description string for saving calibration data.
     * For stacks, also saves the stack size so ImageJ can open the stack without
     * decoding an IFD for each slice.
     * 
     * @param imp Image for which the decription string should be created.
     * @return Description string.
     */
    public static String encode(ImagePlus imp) {
        FileInfo fi = imp.getFileInfo();
        StringBuffer sb = new StringBuffer(100);
        sb.append("ImageJ=" + ij.ImageJ.VERSION + "\n");
        if (fi.nImages > 1) {
            sb.append("images=" + fi.nImages + "\n");
        }
        if (fi.unit != null) {
            sb.append("unit=" + fi.unit + "\n");
        }
        if (fi.valueUnit != null) {
            sb.append("cf=" + fi.calibrationFunction + "\n");
            if (fi.coefficients != null) {
                for (int i = 0; i < fi.coefficients.length; i++) {
                    sb.append("c" + i + "=" + fi.coefficients[i] + "\n");
                }
            }
            sb.append("vunit=" + fi.valueUnit + "\n");
        }
        if (fi.nImages > 1) {
            if (fi.pixelDepth != 0.0 && fi.pixelDepth != 1.0) {
                sb.append("spacing=" + fi.pixelDepth + "\n");
            }
            if (fi.frameInterval != 0.0) {
                double fps = 1.0 / fi.frameInterval;
                if ((int) fps == fps) {
                    sb.append("fps=" + (int) fps + "\n");
                } else {
                    sb.append("fps=" + fps + "\n");
                }
            }
        }
        sb.append("");
        return sb.toString();
    }


    /**
     * Decode the ImageDescription tag. ImageJ saves spatial and density
     * calibration data in this string. For stacks, it also saves the number of
     * images to avoid having to decode an IFD for each image.
     * 
     * @param description Description string.
     * @param imp         Image that will be adjusted using information stored
     *                    in the decription string.
     * @throws Exception If information stored in the decription string is
     *                   inconsistent with image size.
     */
    public static void decode(String description, ImagePlus imp)
            throws Exception {

        if (description == null || !description.startsWith("ImageJ")) {
            return;
        }

        Properties props = new Properties();
        InputStream is = new ByteArrayInputStream(description.getBytes());
        try {
            props.load(is);
            is.close();
        } catch (IOException e) {
            return;
        }

        FileInfo fi = new FileInfo();

        fi.unit = props.getProperty("unit", "");

        Integer n_cf = getInteger(props, "cf");
        if (n_cf != null) {
            fi.calibrationFunction = n_cf.intValue();
        }

        double c[] = new double[5];
        int coefficientsCount = 0;
        for (int i = 0; i < 5; i++) {
            Double n_ci = getDouble(props, "c" + i);
            if (n_ci == null) {
                break;
            }
            c[i] = n_ci.doubleValue();
            coefficientsCount++;
        }
        if (coefficientsCount >= 2) {
            fi.coefficients = new double[coefficientsCount];
            for (int i = 0; i < coefficientsCount; i++) {
                fi.coefficients[i] = c[i];
            }
        }
        fi.valueUnit = props.getProperty("vunit");

        Integer n_images = getInteger(props, "images");
        if (n_images != null && n_images.intValue() > 1) {
            fi.nImages = n_images.intValue();
            if (fi.nImages != imp.getStackSize()) {
                throw new Exception("Number of images in description string ("
                        + fi.nImages + ") does not match number if slices in the image ("
                        + imp.getStackSize() + ").");
            }
        }

        Calibration calib = imp.getCalibration();
        if (calib == null) {
            calib = new Calibration(imp);
        }
        calib.setFunction(fi.calibrationFunction, fi.coefficients, fi.valueUnit);
        calib.setUnit(fi.unit);

        if (fi.nImages > 1) {
            Double n_spacing = getDouble(props, "spacing");
            if (n_spacing != null && n_spacing.doubleValue() != 0.0) {
                calib.pixelDepth = n_spacing.doubleValue();
            }
            Double n_fps = getDouble(props, "fps");
            if (n_fps != null && n_fps.doubleValue() != 0.0) {
                calib.frameInterval = 1.0 / n_fps.doubleValue();
            }
        }

        imp.setCalibration(calib);
    }


    /*
     *
     */
    private static Double getDouble(Properties props, String key) {
        String s = props.getProperty(key);
        if (s != null) {
            try {
                return Double.valueOf(s);
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }


    /*
     *
     */
    private static Integer getInteger(Properties props, String key) {
        String s = props.getProperty(key);
        if (s != null) {
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }
}
