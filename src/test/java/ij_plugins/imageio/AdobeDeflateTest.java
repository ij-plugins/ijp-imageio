/*
 *  IJ-Plugins ImageIO
 *  Copyright (C) 2002-2021 Jarek Sacha
 *  Author's email: jpsacha at gmail dot com
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
 *  Latest release available at https://github.com/ij-plugins/ijp-imageio/
 */
package ij_plugins.imageio;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * ImageJ 1.33 and ij-imageio v.1.2.4 cannot read TIFF images created by Adobe Photshop 7 using
 * AdobeDeflate compression. The problem was reported by Albert on 2005-02-09.
 * <p/>
 * This test is to verify that current JAI ImageIO (2005-02-26) can handle AdobeDeflate compression.
 * JAI and JAI ImageIO must be in the classpath for this test to work (proper codecs are loaded bu
 * javax.imageio).
 *
 * @author Jarek Sacha
 * @version $Revision: 1.3 $
 */
public class AdobeDeflateTest extends TestCase {
    public AdobeDeflateTest(String test) {
        super(test);
    }

    /**
     * Test if javax.imageio can access can read TIFF image with AdobeDeflation compression.
     *
     * @throws Exception
     */
    public void testImageIORead() throws Exception {
        final String inFilePath = "test/data/bug_AdobeDeflate/baboon_AdobeDeflate.tif";
        final File inFile = new File(inFilePath);

        // Check if file exists
        assertTrue("Input file exists: " + inFile.getAbsolutePath(), inFile.exists());

        // Load image
        BufferedImage bi = ImageIO.read(new File(inFilePath));

        assertNotNull("Image loaded not null", bi);

        assertEquals("Width", 512, bi.getWidth());
        assertEquals("Heighr", 512, bi.getWidth());

    }


    /**
     * Test if javax.imageio can access can read TIFF image with AdobeDeflation compression.
     *
     * @throws Exception
     */
    public void testImageIOReadInfo() throws Exception {
        final String inFilePath = "test/data/bug_AdobeDeflate/baboon_AdobeDeflate.tif";
        final File inFile = new File(inFilePath);

        // Check if file exists
        assertTrue("Input file exists: " + inFile.getAbsolutePath(), inFile.exists());

        ImageInputStream iis = new FileImageInputStream(inFile);
        List<ImageReader> readers = IJImageIO.getImageReaders(iis);
        assertFalse(readers.isEmpty());

        // Use the first reader
        ImageReader reader = readers.get(0);
        reader.setInput(iis);
        // Find out how many images are in the file and what is the first index.
        assertEquals("Min index", 0, reader.getMinIndex());
        assertEquals("Number of images", 1, reader.getNumImages(true));
        assertEquals("Width", 512, reader.getWidth(0));
        assertEquals("Heighr", 512, reader.getHeight(0));
    }

}