/*
 * Image/J Plugins
 * Copyright (C) 2002-2016 Jarek Sacha
 * Author's email: jpsacha at gmail.com
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

import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Date: Apr 20, 2006
 * Time: 10:40:54 AM
 *
 * @author Jarek Sacha
 */
public class ImagePlusFactoryTest extends TestCase {

    public ImagePlusFactoryTest(final String test) {
        super(test);
    }


    public void testCreateProcessor1() throws Exception {
        final File inFile = new File("test/data/clown.png");
        assertTrue("Input file exist: " + inFile.getAbsolutePath(), inFile.exists());

        final BufferedImage src = ImageIO.read(inFile);
        assertNotNull(src);

        final ImageProcessor dest = ImagePlusFactory.createProcessor(src);
        assertNotNull(dest);

        assertTrue(dest instanceof ColorProcessor);
        assertEquals(src.getWidth(), dest.getWidth());
        assertEquals(src.getHeight(), dest.getHeight());
    }
}