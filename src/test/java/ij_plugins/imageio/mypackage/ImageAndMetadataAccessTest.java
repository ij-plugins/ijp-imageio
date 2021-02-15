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

package ij_plugins.imageio.mypackage;

import ij_plugins.imageio.IJImageIO;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class ImageAndMetadataAccessTest {


    @Test
    public void readAndAccess() throws Exception {

        // Test if ImageAndMetadata is accessible outside of its own package
        // This test must be in package different from package containing ImageAndMetadata

        final File file = new File("test/data/blobs.png");

        assertTrue("Exist: " + file.getAbsolutePath(), file.exists());

        final List<IJImageIO.ImageAndMetadata> images = IJImageIO.readAsBufferedImages(file);
        assertTrue(images.size() == 1);

        assertNotNull(images.get(0));
        assertNotNull(images.get(0).image);

    }
}
