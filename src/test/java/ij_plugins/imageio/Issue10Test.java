/*
 *  IJ Plugins
 *  Copyright (C) 2002-2022 Jarek Sacha
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

package ij_plugins.imageio;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;


public class Issue10Test {

    @Test
    public void test10() throws Exception {

        var imageName = "test_35mm";

        // Read image using ImageJ
        var inFile = new File("test/data/issue_11/" + imageName + ".tif");
        var inImageIJ = TestUtils.readImageIJ(inFile);

        // Write image
        var outFile = new File("tmp/" + imageName + "_test10.tif");
        if (!outFile.getParentFile().exists()) {
            assertTrue(outFile.getParentFile().mkdirs());
        }

        IJImageIO.writeAsTiff(inImageIJ, outFile);

        // TODO: test that TIFF is written without color map (black-is-zero)
    }
}
