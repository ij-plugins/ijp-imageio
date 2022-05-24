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

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;


public class Issue11Test {


    /**
     * Test of writeAsTiff method, of class ImageIOUtils.
     *
     * @throws Exception when read or write fails.
     */
    @Test
    public void testWriteImagesPlusAsTiff() throws Exception {

        var imageName = "test_35mm";

        // Read image using ImageJ
        var inFile = new File("test/data/issue_11/" + imageName + ".tif");
        var inImageIJ = TestUtils.readImageIJ(inFile);
        //TIFF Directory at offset 0x8 (8)
        //  Subfile Type: (0 = 0x0)
        //  Image Width: 256 Image Length: 256
        //  Resolution: 31.25, 31.25 (unitless)
        //  Bits/Sample: 8
        //  Compression Scheme: None
        //  Photometric Interpretation: min-is-black
        //  Samples/Pixel: 1
        //  Rows/Strip: 256
        //  Planar Configuration: single image plane
        //  ImageDescription: ImageJ=1.53s
        //unit=mm

        // Write image
        var outFile = new File("tmp/" + imageName + ".tif");
        if (!outFile.getParentFile().exists()) {
            assertTrue(outFile.getParentFile().mkdirs());
        }
        IJImageIO.writeAsTiff(inImageIJ, outFile);

        //Validate
        var outImage = TestUtils.readImageIJ(outFile);
        var expCalib = inImageIJ.getCalibration();
        var actualCalib = outImage.getCalibration();
        TestUtils.assertImage(inImageIJ, outImage);
        TestUtils.assertCalibration(expCalib, actualCalib);
    }
}
