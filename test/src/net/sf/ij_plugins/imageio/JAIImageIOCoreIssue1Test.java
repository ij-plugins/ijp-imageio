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
import java.io.File;

/**
 * jai-imageio-core had bug preventig it from reading uncompressed TIFF images. It was reported to
 * https://jai-imageio-core.dev.java.net as Issue 1. For more info see
 * https://jai-imageio-core.dev.java.net/issues/show_bug.cgi?id=1
 *
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class JAIImageIOCoreIssue1Test extends TestCase {
    public JAIImageIOCoreIssue1Test(String test) {
        super(test);
    }

    public void testRead() throws Exception {
        BufferedImage bi = ImageIO.read(new File("test/data/mri-stack-1.tif"));
        assertNotNull(bi);
        assertEquals(186, bi.getWidth());
        assertEquals(226, bi.getHeight());
    }
}