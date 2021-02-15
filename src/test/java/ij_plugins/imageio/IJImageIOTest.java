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

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import org.junit.Ignore;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

/**
 * @author Jarek Sacha
 */
public class IJImageIOTest {

    final private static File DATA_DIR = new File("test/data");


    @Test
    public void testRead() throws Exception {
        testRead("test/data/mri-stack.tif", 27, 186, 226);
        testRead("test/data/mri-stack-1.tif", 1, 186, 226);
        testRead("test/data/bug_1047736/otbm_C_131004_48_0048.png", 1, 300, 300);
        testRead("test/data/bug_1047736/totbm_C_131004_48_0048.png", 1, 300, 300);
        testRead("test/data/bug_1047736/totbm_C_131004_48_0048.tif", 1, 300, 300);
    }

    @Test
    public void testDataBufferFloat() throws Exception {
        testRead("test/data/bug_DataBufferFloat/blobs_smooth_float.tif", 1, 256, 254);
    }

    @Test
    public void testReadAdobeDeflate() throws Exception {
        // sun-jai-codec.1.1.1 cannot read TIFF images with compression: AdobeDeflate (created by
        // Adobe Photoshop 7). New jai-imageio can read it.
        testRead("test/data/bug_AdobeDeflate/baboon_AdobeDeflate.tif", 1, 512, 512);
    }

    @Test
    public void testReadTwoSequences() throws Exception {
        final File inFile = new File(DATA_DIR, "two_stacks.tif");
        assertTrue(inFile.exists());

        final ImagePlus[] images = IJImageIO.read(inFile);
        assertNotNull(images);
        assertEquals(2, images.length);
        assertEquals(2, images[0].getNSlices());
        assertEquals(3, images[1].getNSlices());
    }

    @Test
    public void testReadRGB48TIFF() throws Exception {
        ImagePlus imp = testRead("test/data/DeltaE_16bit_gamma1.0.tif", 3, 3072, 2048);
        assertEquals(ImagePlus.GRAY16, imp.getType());
        assertTrue(imp instanceof CompositeImage);

        CompositeImage ci = (CompositeImage) imp;
        assertEquals(CompositeImage.COMPOSITE, ci.getMode());
    }

    private ImagePlus testRead(final String fileName, final int stackSize, final int width, final int height)
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

        return imps[0];
    }

    @Ignore
    @Test
    public void testJPEG2000Writer() {
        final String formatName = "jpeg2000";
        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
        assertNotNull(writers);
        assertTrue(writers.hasNext());

        final ImageWriter writer = writers.next();
        final ImageWriteParam writerParam = writer.getDefaultWriteParam();

        writerParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        final String[] compressionTypes = writerParam.getCompressionTypes();
        print("compressionTypes", compressionTypes);

        writerParam.setCompressionType(compressionTypes[0]);
        final String[] compressionQualityDescriptions = writerParam.getCompressionQualityDescriptions();
        print("compressionQualityDescriptions", compressionQualityDescriptions);
    }

    /**
     * Do not assume unit 'pixels' if no unit is present
     */
    @Test
    public void testIssue5() throws Exception {
        // Read image using ImageJ
        final File inFile = new File("test/data/issue_5/0001A00M.tif");
        final ImagePlus expImage = IJ.openImage(inFile.getCanonicalPath());
        assertNotNull(expImage);

        final ImagePlus[] actualImages = IJImageIO.read(inFile);
        assertEquals(1, actualImages.length);

        final double tolerance = 0.000001;
        final Calibration expCalib = expImage.getCalibration();
        final Calibration actualCalib = actualImages[0].getCalibration();
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

    @Test
    public void testBug1434311() throws Exception {

        final String codecName = "TIFF";
        final String compression = "CCITT T.6"; // CCITT Group 4


        final String inFilePath = "test/data/bug_1434311/testG4.tif";
        final File inFile = new File(inFilePath);


        final ImagePlus imp;
        {
            final ImagePlus[] imps = IJImageIO.read(inFile);
            assertNotNull(imps);
            assertEquals(1, imps.length);
            imp = imps[0];
        }


        final List<ImageWriter> writers = IJImageOUtils.getImageWritersByFormatName(codecName);
        assertFalse("Assuming presence of ImageIO writer: " + codecName, writers.isEmpty());

        final ImageWriter writer = writers.get(0);
        final IIOMetadata metadata = TiffMetaDataFactory.createFrom(imp);
        final ImageWriteParam writerParam = writer.getDefaultWriteParam();
        if (writerParam.canWriteCompressed()) {
            writerParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writerParam.setCompressionType(compression);
        }

        final File outFile = File.createTempFile("testBug1434311_", ".tif");
        outFile.deleteOnExit();

        IJImageIO.write(imp, outFile, writer, metadata, writerParam, true);

        final ImagePlus[] imps2 = IJImageIO.read(outFile);
        assertNotNull(imps2);
        assertEquals(1, imps2.length);

        verifyEqual(imp, imps2[0]);
    }

    @Test
    public void testWritingOfTIFFMetadata() throws IJImageIOException, IOException {

        File file = new File("tmp", "metadata_test.tif");
        if (file.exists()) file.delete();
        assertFalse(file.exists());
        file.getParentFile().mkdirs();

        ImagePlus imp = new ImagePlus("my title", new ByteProcessor(64, 64));
        Calibration cal = imp.getCalibration();
        cal.setXUnit("a");
        cal.setYUnit("B");
        cal.xOrigin = 11;
        cal.yOrigin = 13;
        cal.pixelWidth = 17;
        cal.pixelHeight = 23;
        imp.setCalibration(cal);

        IJImageIO.writeAsTiff(imp, file);

        ImagePlus imp2 = IJ.openImage(file.getCanonicalPath());
        Calibration cal2 = imp2.getCalibration();

        assertEquals("a", cal2.getXUnit());
        assertEquals("B", cal2.getYUnit());
        assertEquals(11, cal2.xOrigin, 0.001);
        assertEquals(13, cal2.yOrigin, 0.001);
        assertEquals(17, cal2.pixelWidth, 0.001);
        assertEquals(23, cal2.pixelHeight, 0.001);
    }

    @Test
    public void testReadingOfTIFFMetadata() throws Exception {
        final File inFile = new File(DATA_DIR, "metadata_test.tif");
        assertTrue(inFile.exists());

        final ImagePlus[] images = IJImageIO.read(inFile);
        assertNotNull(images);
        assertEquals(1, images.length);

        ImagePlus imp0 = images[0];
        Calibration cal0 = imp0.getCalibration();

        assertEquals("a", cal0.getXUnit());
        assertEquals("B", cal0.getYUnit());
        assertEquals(11, cal0.xOrigin, 0.001);
        assertEquals(13, cal0.yOrigin, 0.001);
        assertEquals(17, cal0.pixelWidth, 0.001);
        assertEquals(23, cal0.pixelHeight, 0.001);
    }


    private void verifyEqual(final ImagePlus expected, final ImagePlus actual) {
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

    private void print(final String message, final String[] a) {
        System.out.print(message + ": [");
        if (a != null) {
            for (final String anA : a) {
                System.out.print(anA + ", ");
            }
        }
        System.out.println("]");
    }

}