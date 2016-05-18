/*
 * Image/J Plugins
 * Copyright (C) 2002-2016 Jarek Sacha
 * Author's email: jpsacha at gmail.com
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

package net.sf.ij_plugins.imageio;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

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
     * Creates an optional ImageJ's image description string for saving calibration data.
     * For stacks, also saves the stack size so ImageJ can open the stack
     * without decoding an IFD for each slice.
     *
     * @param imp Image for which the description string should be created.
     * @return Description string.
     */
    public static String encode(final ImagePlus imp) {
        final FileSaver fileSaver = new FileSaver(imp);
        final String desc = fileSaver.getDescriptionString();
        // ImageJ may append a null char et the end, check and remove.
        if (desc.charAt(desc.length() - 1) != (char) 0) {
            return desc;
        } else {
            return desc.substring(0, desc.length() - 1);
        }
    }

    /**
     * Decode the `description` tag saved by ImageJ in TIFF images.
     * ImageJ saves there things like spatial and intensity calibration data.
     *
     * @param description Description string.
     * @param imp         Image that will be adjusted using information stored
     *                    in the description string.
     */
    @SuppressWarnings("ConstantConditions")
    public static void decode(final String description, final ImagePlus imp) {

        if (description == null || !description.startsWith("ImageJ")) {
            return;
        }

        final Properties props = new Properties();
        try (InputStream is = new ByteArrayInputStream(description.getBytes())) {
            props.load(is);
        } catch (final IOException e) {
            throw new RuntimeException("Exception reading ByteArrayInputStream, this should never happen. Format error?");
        }

        Calibration cal = imp.getCalibration();

        // Load units
        String dsUnit = props.getProperty("unit", "");
        if ("cm".equals(cal.getUnit()) && "um".equals(dsUnit)) {
            cal.pixelWidth *= 10000;
            cal.pixelHeight *= 10000;
        }
        cal.setUnit(dsUnit);

        if (props.containsKey("tunit")) cal.setTimeUnit(props.getProperty("tunit"));
        if (props.containsKey("yunit")) cal.setYUnit(props.getProperty("yunit"));
        if (props.containsKey("zunit")) cal.setZUnit(props.getProperty("zunit"));
        if (props.containsKey("xorigin")) cal.xOrigin = getDouble(props, "xorigin");
        if (props.containsKey("yorigin")) cal.yOrigin = getDouble(props, "yorigin");
        if (props.containsKey("zorigin")) cal.zOrigin = getDouble(props, "zorigin");
        if (props.containsKey("finterval")) cal.frameInterval = getDouble(props, "finterval");
        if (props.containsKey("fps")) cal.fps = getDouble(props, "fps");
        if (props.containsKey("loop")) cal.loop = getBoolean(props, "loop");

        // Load calibration function
        Integer calibrationFunction = getInteger(props, "cf");
        if (calibrationFunction != null) {
            double c[] = new double[5];
            int count = 0;
            for (int i = 0; i < 5; i++) {
                Double n = getDouble(props, "c" + i);
                if (n == null) break;
                c[i] = n;
                count++;
            }
            if (count >= 2) {
                double[] coefficients = new double[count];
                System.arraycopy(c, 0, coefficients, 0, count);

                Boolean zeroclip = getBoolean(props, "zeroclip");
                if (zeroclip != null) {
                    cal.setFunction(calibrationFunction, coefficients, props.getProperty("vunit"), zeroclip);
                } else {
                    cal.setFunction(calibrationFunction, coefficients, props.getProperty("vunit"));
                }

            }
        }

        Double spacing = getDouble(props, "spacing");
        if (spacing != null) {
            if (spacing < 0) spacing = -spacing;
            cal.pixelDepth = spacing;
        }

        imp.setCalibration(cal);

        setMinAndMax(imp, props);
    }


    private static Boolean getBoolean(final Properties props, final String key) {
        final String s = props.getProperty(key);
        if (s != null) {
            try {
                return Boolean.valueOf(s);
            } catch (final NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Double getDouble(final Properties props, final String key) {
        final String s = props.getProperty(key);
        if (s != null) {
            try {
                return Double.valueOf(s);
            } catch (final NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Integer getInteger(final Properties props, final String key) {
        final String s = props.getProperty(key);
        if (s != null) {
            try {
                return Integer.valueOf(s);
            } catch (final NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static void setMinAndMax(final ImagePlus imp, final Properties props) {
        final Double displayMin = getDouble(props, "min");
        final Double displayMax = getDouble(props, "max");
        if (displayMin != null && displayMax != null) {
            final int type = imp.getType();
            final ImageProcessor ip = imp.getProcessor();
            if (type == ImagePlus.GRAY8 || type == ImagePlus.COLOR_256) {
                ip.setMinAndMax(displayMin, displayMax);
            } else if (type == ImagePlus.GRAY16 || type == ImagePlus.GRAY32) {
                if (ip.getMin() != displayMin || ip.getMax() != displayMax) {
                    ip.setMinAndMax(displayMin, displayMax);
                }
            }
        }
    }
}
