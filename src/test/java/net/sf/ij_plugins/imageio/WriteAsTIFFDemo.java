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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static net.sf.ij_plugins.imageio.ReadWrite48BitColorTIFFTest.assertReadRGB48;
import static org.junit.Assert.assertFalse;

public class WriteAsTIFFDemo {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) throws IOException, IJImageIOException {
        File srcFile = new File("test/data/DeltaE_16bit_gamma1.0.tif");
        CompositeImage src = assertReadRGB48(srcFile, 3072, 2048);

        File dstFile = new File("tmp/DeltaE_16bit_gamma1.0-jio-1.tif");
        dstFile.delete();
        assertFalse(dstFile.exists());

        IJImageIO.write(src, dstFile, "tif");
        assertReadRGB48(dstFile, 3072, 2048);

        IJImageIO.write(src, new File("tmp/DeltaE_16bit_gamma1.0-jio-1.png"), "png");

        System.out.println(Arrays.toString(IJImageIO.getTIFFCompressionTypes()));
        IJImageIO.writeAsTiff(src, new File("tmp/DeltaE_16bit_gamma1.0-jio-1_default.tif"));
        IJImageIO.writeAsTiff(src, new File("tmp/DeltaE_16bit_gamma1.0-jio-1_LZW.tif"), "LZW");
        IJImageIO.writeAsTiff(src, new File("tmp/DeltaE_16bit_gamma1.0-jio-1_ZLib.tif"), "ZLib");
    }

}
