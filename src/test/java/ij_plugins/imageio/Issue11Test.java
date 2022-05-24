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


public class Issue11Test {


    /**
     * Test of writeAsTiff method, of class ImageIOUtils.
     *
     * @throws Exception when read or write fails.
     */
    @Test
    public void testWriteImagesPlusAsTiff() throws Exception {

        var imageName = "1001-1-L-T0-crop-8bit_ij";

        // Read image using ImageJ
        var inFile = new File("test/data/issue_x/" + imageName + ".tif");
        var inImageIJ = TestUtils.readImageIJ(inFile);
        //TIFF Directory at offset 0x8 (8)
        //  Subfile Type: (0 = 0x0)
        //  Image Width: 219 Image Length: 218
        //  Resolution: 31.25, 31.25 (unitless)
        //  Bits/Sample: 8
        //  Compression Scheme: None
        //  Photometric Interpretation: min-is-black
        //  Samples/Pixel: 1
        //  Rows/Strip: 218
        //  Planar Configuration: single image plane
        //  ImageDescription: ImageJ=1.52v
        //unit=mm

        // Write image
        var outFile = new File("test/out/" + imageName + ".tif");
        IJImageIO.writeAsTiff(inImageIJ, outFile);
        //TIFF Directory at offset 0x8 (8)
        //  Image Width: 219 Image Length: 218
        //  Resolution: 0.03125, 0.03125 pixels/cm
        //  Bits/Sample: 8
        //  Compression Scheme: AdobeDeflate
        //  Photometric Interpretation: palette color (RGB from colormap)
        //  Samples/Pixel: 1
        //  Rows/Strip: 37
        //  Planar Configuration: single image plane
        //  Color Map: (present)
        //  ImageDescription: ImageJ=1.53g
        //unit=mm

        //Validate
        var outImage = TestUtils.readImageIJ(outFile);
        var expCalib = inImageIJ.getCalibration();
        var actualCalib = outImage.getCalibration();
        TestUtils.assertImage(inImageIJ, outImage);
        TestUtils.assertCalibration(expCalib, actualCalib);
    }
}
