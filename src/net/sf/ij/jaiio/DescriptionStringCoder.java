/*
 * Image/J Plugins
 * Copyright (C) 2002-2008 Jarek Sacha
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
 *
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
 */

public class DescriptionStringCoder {

    private DescriptionStringCoder() {
    }


    /**
     * Creates an optional image description string for saving calibration data.
     * For stacks, also saves the stack size so ImageJ can open the stack
     * without decoding an IFD for each slice.
     *
     * @param imp Image for which the description string should be created.
     * @return Description string.
     */
    public static String encode(ImagePlus imp) {
        FileInfo fi = imp.getFileInfo();
        StringBuffer sb = new StringBuffer(100);
        sb.append("ImageJ=" + ij.ImageJ.VERSION + "\n");
        if (fi.nImages > 1) {
            sb.append("images=").append(fi.nImages).append("\n");
        }
        if (fi.unit != null) {
            sb.append("unit=").append(fi.unit).append("\n");
        }
        if (fi.valueUnit != null) {
            sb.append("cf=").append(fi.calibrationFunction).append("\n");
            if (fi.coefficients != null) {
                for (int i = 0; i < fi.coefficients.length; i++) {
                    sb.append("c").append(i).append("=").append(fi.coefficients[i]).append("\n");
                }
            }
            sb.append("vunit=").append(fi.valueUnit).append("\n");
        }
        if (fi.nImages > 1) {
            if (fi.pixelDepth != 0.0 && fi.pixelDepth != 1.0) {
                sb.append("spacing=").append(fi.pixelDepth).append("\n");
            }
            if (fi.frameInterval != 0.0) {
                double fps = 1.0 / fi.frameInterval;
                if ((int) fps == fps) {
                    sb.append("fps=").append((int) fps).append("\n");
                } else {
                    sb.append("fps=").append(fps).append("\n");
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
     *                    in the description string.
     * @throws Exception If information stored in the description string is
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
        } catch (IOException e) {
            throw new RuntimeException("Exception reading ByteArrayInputStream, this should never happen. Format error?");
        } finally {
            is.close();
        }

        FileInfo fi = new FileInfo();

        fi.unit = props.getProperty("unit", "");

        Integer n_cf = getInteger(props, "cf");
        if (n_cf != null) {
            fi.calibrationFunction = n_cf;
        }

        double c[] = new double[5];
        int coefficientsCount = 0;
        for (int i = 0; i < 5; i++) {
            Double n_ci = getDouble(props, "c" + i);
            if (n_ci == null) {
                break;
            }
            c[i] = n_ci;
            coefficientsCount++;
        }
        if (coefficientsCount >= 2) {
            fi.coefficients = new double[coefficientsCount];
            System.arraycopy(c, 0, fi.coefficients, 0, coefficientsCount);
        }
        fi.valueUnit = props.getProperty("vunit");

        // Verify number of images
        // [Comment out verification since it make sense only when very first image of a stack is read]
//        Integer n_images = getInteger(props, "images");
//        if (n_images != null && n_images.intValue() > 1) {
//            fi.nImages = n_images.intValue();
//            if (fi.nImages != imp.getStackSize()) {
//                throw new Exception("Number of images in description string ("
//                        + fi.nImages + ") does not match number if slices in the image ("
//                        + imp.getStackSize() + ").");
//            }
//        }

        Calibration calib = imp.getCalibration();
        if (calib == null) {
            calib = new Calibration(imp);
        }
        calib.setFunction(fi.calibrationFunction, fi.coefficients, fi.valueUnit);
        calib.setUnit(fi.unit);

        if (fi.nImages > 1) {
            Double n_spacing = getDouble(props, "spacing");
            if (n_spacing != null && n_spacing != 0.0) {
                calib.pixelDepth = n_spacing;
            }
            Double n_fps = getDouble(props, "fps");
            if (n_fps != null && n_fps != 0.0) {
                calib.frameInterval = 1.0 / n_fps;
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
                e.printStackTrace();
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
                e.printStackTrace();
            }
        }
        return null;
    }
}
