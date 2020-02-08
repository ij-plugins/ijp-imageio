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
package net.sf.ij_plugins.imageio;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.File;

/**
 *
 */
public class Read48BitColorTIFFTest extends TestCase {
    public Read48BitColorTIFFTest(String test) {
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
}