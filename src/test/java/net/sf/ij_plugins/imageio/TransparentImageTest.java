/*
 * Image/J Plugins
 * Copyright (C) 2002-2011 Jarek Sacha
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
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;

import static org.junit.Assert.*;


/**
 * @author Jarek Sacha
 * @since 2/17/11 9:20 AM
 */
public final class TransparentImageTest {

    /**
     * The fixture set up called before every test method.
     */
    @Before
    public void setUp() {
    }


    /**
     * The fixture clean up called after every test method.
     */
    @After
    public void tearDown() {
    }


    @Test
    public void testSomething() throws Exception {
        final ImagePlus[] imps = IJImageIO.read(new File("test/data/clown.png"));
        assertNotNull(imps);
        assertEquals(1, imps.length);
        assertTrue(imps[0].getProcessor() instanceof ColorProcessor);

        final ColorProcessor cp = (ColorProcessor) imps[0].getProcessor();

        final ByteProcessor transparencyMap = new ByteProcessor(cp.getWidth(), cp.getHeight() - 1);
        transparencyMap.setColor(128);
        transparencyMap.fill();

        final BufferedImage bi = create(cp, transparencyMap);

        ImageIO.write(bi, "PNG", new File("test/data/clown-alpha.png"));
        ImageIO.write(bi, "BMP", new File("test/data/clown-alpha.bmp"));
//        ImageIO.write(bi, "GIF", new File("test/data/clown-alpha.gif"));
    }


    public static BufferedImage create(final ColorProcessor src, final ByteProcessor transparencyMap) {
        if (src == null || transparencyMap == null || src.getWidth() != transparencyMap.getWidth()
                || src.getHeight() != transparencyMap.getHeight()) {
            throw new IllegalArgumentException("Input parameters are not valid: src="
                    + src + ", transparencyMap=" + transparencyMap);
        }

        final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        final int[] bits = {8, 8, 8, 8};
        final ColorModel cm = new ComponentColorModel(cs, bits, true, false,
                Transparency.BITMASK, DataBuffer.TYPE_BYTE);
        final WritableRaster raster = cm.createCompatibleWritableRaster(src.getWidth(), src.getHeight());
        final DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();

        final byte[] data = dataBuffer.getData();
        final int n = ((int[]) src.getPixels()).length;
        final byte[] r = new byte[n];
        final byte[] g = new byte[n];
        final byte[] b = new byte[n];
        final byte[] a = (byte[]) transparencyMap.getPixels();
        src.getRGB(r, g, b);
        for (int i = 0; i < n; ++i) {
            final int offset = i * 4;
            data[offset] = r[i];
            data[offset + 1] = g[i];
            data[offset + 2] = b[i];
            data[offset + 3] = a[i];
        }

        return new BufferedImage(cm, raster, false, null);
    }
}