/***
 * Image/J Plugins
 * Copyright (C) 2002-2005 Jarek Sacha
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

import com.sun.media.jai.codec.TIFFEncodeParam;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import junit.framework.TestCase;

import java.io.File;
import java.util.Arrays;

/**
 * Date: Mar 10, 2007
 * Time: 6:51:38 PM
 *
 * @author Jarek Sacha
 */
public final class JAIWriterTest extends TestCase {
    public JAIWriterTest(String test) {
        super(test);
    }


    public void testBug1434311() throws Exception {
        final String inFilePath = "test/data/bug_1434311/testG4.tif";
        final File inFile = new File(inFilePath);

        final ImagePlus[] imps = JAIReader.read(inFile);
        assertNotNull(imps);
        assertEquals(1, imps.length);

        final TIFFEncodeParam encodeParam = new TIFFEncodeParam();
        encodeParam.setCompression(TIFFEncodeParam.COMPRESSION_GROUP4);
        encodeParam.setWriteTiled(false);

        final JAIWriter writer = new JAIWriter();
        writer.setFormatName("TIFF");
        writer.setImageEncodeParam(encodeParam);

        final File outFile = File.createTempFile("testBug1434311_", ".tif");
        outFile.deleteOnExit();
        writer.write(outFile.getAbsolutePath(), imps[0], true);

        final ImagePlus[] imps2 = JAIReader.read(outFile);
        assertNotNull(imps2);
        assertEquals(1, imps2.length);

        verifyEqual(imps[0], imps2[0]);

    }

    void verifyEqual(final ImagePlus expected, final ImagePlus actual) {
        if (expected == actual) {
            return;
        }

        if (expected == null) {
            assertNull(actual);
        }

        assertEquals(expected.getType(), actual.getType());

        verifyEquals(expected.getProcessor(), actual.getProcessor());

    }

    private void verifyEquals(final ImageProcessor expected, final ImageProcessor actual) {
        if (expected == actual) {
            return;
        }

        if (expected == null) {
            assertNull(actual);
        }

        assertEquals(expected.getClass(), actual.getClass());

        if (expected instanceof ByteProcessor) {
            final byte[] expectedPixels = (byte[]) expected.getPixels();
            final byte[] actualPixels = (byte[]) actual.getPixels();
            assertTrue("Equal pixels", Arrays.equals(expectedPixels, actualPixels));
        } else {
            throw new UnsupportedOperationException("Comparison for image processor of type: "
                    + expected.getClass() + " not implemented.");
        }
    }
}