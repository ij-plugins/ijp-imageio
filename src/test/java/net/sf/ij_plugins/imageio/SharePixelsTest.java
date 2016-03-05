/*
 * Image/J Plugins
 * Copyright (C) 2002-2016 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
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
import ij.io.Opener;
import ij.process.ByteProcessor;
import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

/**
 * Date: Jan 17, 2007
 * Time: 10:38:58 PM
 *
 * @author Jarek Sacha
 */
public final class SharePixelsTest extends TestCase {
    public SharePixelsTest(String test) {
        super(test);
    }

    /**
     * The fixture set up called before every test method.
     */
    protected void setUp() throws Exception {
    }

    /**
     * The fixture clean up called after every test method.
     */
    protected void tearDown() throws Exception {
    }

    public void testSomething() throws Exception {
        final File inputFile = new File("test/data/blobs.png");
        final File outputFile = new File("tmp/blobs_out.png");
        final ImagePlus imp = openImage(inputFile);
        final ByteProcessor bp = (ByteProcessor) imp.getProcessor();
        final int width = bp.getWidth();
        final int height = bp.getHeight();
        final byte[] pixels = (byte[]) bp.getPixels();
        // Create raster from byte array pixels
        final DataBufferByte dbb = new DataBufferByte(pixels, pixels.length);
        final SampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_BYTE, width, height, 1, width, new int[]{0});
        final WritableRaster wr = Raster.createWritableRaster(sm, dbb, new Point());
        // Create gray level color model
        final byte[] rLUT = new byte[256];
        final byte[] gLUT = new byte[256];
        final byte[] bLUT = new byte[256];
        for (int i = 0; i < 256; i++) {
            rLUT[i] = (byte) i;
            gLUT[i] = (byte) i;
            bLUT[i] = (byte) i;
        }
        final IndexColorModel icm = new IndexColorModel(8, 256, rLUT, gLUT, bLUT);
        // Put all together into a buffered image
        final BufferedImage bi = new BufferedImage(icm, wr, true, null);
        ImageIO.write(bi, "png", outputFile);
    }

    public static ImagePlus openImage(final File file) throws IOException {
        final Opener opener = new Opener();
        final ImagePlus imp = opener.openImage(file.getAbsolutePath());
        if (imp == null) {
            throw new IOException("Unable to open image file: " + file.getAbsolutePath());
        }

        return imp;
    }

}