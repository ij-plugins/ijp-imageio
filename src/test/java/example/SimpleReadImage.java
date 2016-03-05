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
package example;

import ij.ImagePlus;
import net.sf.ij_plugins.imageio.IJImageIO;

import java.io.File;

/**
 * Show a simple example of reading an image from file using {@link IJImageIO}.
 * A name of an image file can be specified at the command line, if not
 * specified image will be read from <code>DEFAULT_FILE_NAME</code>
 *
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class SimpleReadImage {
    // Default file name
    public static final String DEFAULT_FILE_NAME = "test/data/clown_LZW.tif";

    /**
     * Read an image or images from a file using {@link IJImageIO}.
     *
     * @param args first element of the array is a name of the file to read. If
     *             <code>args</code> is null or empty image will be read from
     *             the <code>DEFAULT_FILE_NAME</code>.
     */
    public static void main(final String[] args) {

        // Define file from which to read images
        final File file;
        if (args != null && args.length > 0) {
            file = new File(args[1]);
        } else {
            file = new File(DEFAULT_FILE_NAME);
        }

        // Read images
        final ImagePlus[] images;
        try {
            images = IJImageIO.read(file);
        } catch (Exception e) {
            System.out.println("Unable to open image file: "
                    + file.getAbsolutePath() + "\n" + e.getMessage());
            System.exit(-1);
            return;
        }

        System.out.println("Read " + images.length + " images from file: "
                + file.getAbsolutePath());
        for (int i = 0; i < images.length; i++) {
            final ImagePlus image = images[i];
            System.out.println("Image " + (i + 1) + ": " + image.getHeight()
                    + "x" + image.getWidth());
        }

        System.exit(0);
    }
}
