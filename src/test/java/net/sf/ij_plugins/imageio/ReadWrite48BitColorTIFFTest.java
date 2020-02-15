/*
 *  IJ Plugins
 *  Copyright (C) 2002-2020 Jarek Sacha
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
package net.sf.ij_plugins.imageio;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import static net.sf.ij_plugins.imageio.IJImageOUtils.isRGB48;

/**
 *
 */
public class ReadWrite48BitColorTIFFTest extends TestCase {
    public ReadWrite48BitColorTIFFTest(String test) {
        super(test);
    }

    public void testRead() throws Exception {
        BufferedImage bi = ImageIO.read(new File("test/data/DeltaE_16bit_gamma1.0.tif"));
        assertNotNull(bi);
        assertEquals(3072, bi.getWidth());
        assertEquals(2048, bi.getHeight());

        WritableRaster wr = bi.getRaster();
        assertEquals(3, wr.getNumBands());

        DataBuffer db = wr.getDataBuffer();
        assertEquals(1, db.getNumBanks());

        assertEquals(DataBuffer.TYPE_USHORT, db.getDataType());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testWrite() throws Exception {
        File srcFile = new File("test/data/DeltaE_16bit_gamma1.0.tif");
        CompositeImage src = assertReadRGB48(srcFile, 3072, 2048);

        File dstFile = new File("tmp/DeltaE_16bit_gamma1.0-jio-1.tif");
        dstFile.delete();
        assertFalse(dstFile.exists());

        IJImageIO.write(src, dstFile, "tif");
        assertReadRGB48(dstFile, 3072, 2048);
    }

    public static CompositeImage assertReadRGB48(File file, int width, int height) throws IOException {
        ImagePlus imp = IJ.openImage(file.getCanonicalPath());
        assertTrue(isRGB48(imp));
        assertEquals(width, imp.getWidth());
        assertEquals(height, imp.getHeight());
        assertEquals(ImagePlus.GRAY16, imp.getType());
        return (CompositeImage) imp;
    }
}