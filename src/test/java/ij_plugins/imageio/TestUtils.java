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
import ij.io.Opener;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class TestUtils {

    final static double tolerance = 0.000001;

    static void assertImage(final ImagePlus expImage, final ImagePlus actualImage) {
        assertEquals(expImage.getStackSize(), actualImage.getStackSize());
        assertEquals(expImage.getWidth(), actualImage.getWidth());
        assertEquals(expImage.getHeight(), actualImage.getHeight());

        final ImageProcessor expIp = expImage.getProcessor();
        final ImageProcessor actualIp = actualImage.getProcessor();

        assertEquals(expIp.getPixelValue(0, 0), actualIp.getPixelValue(0, 0), tolerance);
        assertEquals(expIp.getPixelValue(expImage.getWidth(), expImage.getHeight()), actualIp.getPixelValue(expImage.getWidth(), expImage.getHeight()), tolerance);
        assertEquals(expIp.getPixelValue(expImage.getWidth() / 2, expImage.getHeight() / 2), actualIp.getPixelValue(expImage.getWidth() / 2, expImage.getHeight() / 2), tolerance);
    }

    static void assertCalibration(final Calibration expCalib, final Calibration actualCalib) {
        assertEquals("pixelHeight", expCalib.pixelHeight, actualCalib.pixelHeight, tolerance);
        assertEquals("pixelWidth", expCalib.pixelWidth, actualCalib.pixelWidth, tolerance);
        assertEquals("Unit", expCalib.getUnit(), actualCalib.getUnit());
        assertEquals("function", expCalib.getFunction(), actualCalib.getFunction());
        if (expCalib.getFunction() != Calibration.NONE) {

            final double[] expCoeff = expCalib.getCoefficients();
            final double[] actualCoeff = actualCalib.getCoefficients();

            assertEquals("Number of coefficients", expCoeff.length, actualCoeff.length);
            for (int i = 0; i < expCoeff.length; i++) {
                assertEquals("coefficients " + i, expCoeff[i], actualCoeff[i], tolerance);
            }

            assertEquals("valueUnit", expCalib.getValueUnit(), actualCalib.getValueUnit());
        }
    }

    /**
     * Read image using ImageJ Opener, throw exception when not successful.
     * <p>
     * This is a wrapper around ij.io.Opener to throw exception on failure
     * rather than return null.
     *
     * @param file file to load an image from.
     * @return Loaded image.
     * @throws IJImageIOException if image was not loaded.
     * @see ij.io.Opener
     */
    static ImagePlus readImageIJ(final File file) throws IJImageIOException {
        Validate.notNull(file, "Argument 'file' cannot be null.");

        if (!file.exists()) {
            throw new IJImageIOException("Image file does not exist: " + file.getAbsolutePath());
        }

        final Opener opener = new Opener();
        final ImagePlus imp = opener.openImage(file.getAbsolutePath());
        if (imp == null) {
            throw new IJImageIOException("Cannot read image from file: " + file.getAbsolutePath());
        }

        return imp;
    }
}
