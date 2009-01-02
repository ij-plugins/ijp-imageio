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
package net.sf.ij_plugins.imageio;

import ij.ImagePlus;
import junit.framework.TestCase;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.io.File;
import java.util.Iterator;

/**
 * @author Jarek Sacha
 */
public class IJImageIOTest extends TestCase {

    final private static File DATA_DIR = new File("test/data");

    public IJImageIOTest(String test) {
        super(test);
    }

    public void testRead() throws Exception {
        testRead("test/data/mri-stack.tif", 27, 186, 226);
        testRead("test/data/mri-stack-1.tif", 1, 186, 226);
        testRead("test/data/bug_1047736/otbm_C_131004_48_0048.png", 1, 300, 300);
        testRead("test/data/bug_1047736/totbm_C_131004_48_0048.png", 1, 300, 300);
        testRead("test/data/bug_1047736/totbm_C_131004_48_0048.tif", 1, 300, 300);
    }

    public void testDataBufferFloat() throws Exception {
        testRead("test/data/bug_DataBufferFloat/blobs_smooth_float.tif", 1, 256, 254);
    }

    public void testReadAdobeDeflate() throws Exception {
        // sun-jai-codec.1.1.1 cannot read TIFF images with compression: AdobeDeflate (created by
        // Adobe Photoshop 7). New jai-imageio can read it.
        testRead("test/data/bug_AdobeDeflate/baboon_AdobeDeflate.tif", 1, 512, 512);
    }

    public void testReadTwoSequences() throws Exception {
        final File inFile = new File(DATA_DIR, "two_stacks.tif");
        assertTrue(inFile.exists());

        final ImagePlus[] images = IJImageIO.read(inFile);
        assertNotNull(images);
        assertEquals(2, images.length);
        assertEquals(2, images[0].getNSlices());
        assertEquals(3, images[1].getNSlices());
    }

    private void testRead(final String fileName, final int stackSize, final int width, final int height)
            throws Exception {
        final File file = new File(fileName);

        assertTrue("Exist: " + file.getAbsolutePath(), file.exists());

        ImagePlus[] imps = IJImageIO.read(file);

        // Assume that all images in the file were of the same type ans size.
        assertEquals(1, imps.length);

        ImagePlus imp = imps[0];
        assertEquals("Image width", width, imp.getWidth());
        assertEquals("Image height", height, imp.getHeight());
        assertEquals("Image stack size", stackSize, imp.getStackSize());
        assertEquals("Image name", file.getName(), imp.getTitle());
        //        assertNotNull("Image title should not be null", imp.getTitle());
        //        assertTrue("Image name length larger than 0", imp.getTitle().trim().length() > 0);

        //        imp.show();
    }

    public void testJPEG2000Writer() {
        final String formatName = "jpeg2000";
        final Iterator writers = ImageIO.getImageWritersByFormatName(formatName);
        assertNotNull(writers);
        assertTrue(writers.hasNext());

        final ImageWriter writer = (ImageWriter) writers.next();
        final ImageWriteParam writerParam = writer.getDefaultWriteParam();

        writerParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        final String[] compressionTypes = writerParam.getCompressionTypes();
        print("compressionTypes", compressionTypes);

        writerParam.setCompressionType(compressionTypes[0]);
        final String[] compressionQualityDescriptions = writerParam.getCompressionQualityDescriptions();
        print("compressionQualityDescriptions", compressionQualityDescriptions);
    }

    void print(final String message, final String[] a) {
        System.out.print(message + ": [");
        if (a != null) {
            for (final String anA : a) {
                System.out.print(anA + ", ");
            }
        }
        System.out.println("]");
    }

}