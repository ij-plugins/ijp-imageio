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

package net.sf.ij_plugins.imageio.impl;

//import com.sun.media.jai.codec.ImageCodec;

import javax.imageio.spi.ImageWriterSpi;
import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * File filter using file name extensions from provided {@link ImageWriterSpi}.
 *
 * @author Jarek Sacha
 */

public class ImageIOWriterFileFilter extends FileFilter {

    //    private String codecName;
    private String description = "???";

    private ImageWriterSpi spi;

    /**
     * Create file filter accepting images supported by given extension.
     */
    ImageIOWriterFileFilter(final ImageWriterSpi spi, final String description) {
        this.spi = spi;
        this.description = description;
    }

    /**
     * Gets the Description attribute of the ExtensionFileFilter object
     *
     * @return The Description value
     */
    @Override
    public String getDescription() {
        return description;
    }

    public ImageWriterSpi getSPI() {
        return spi;
    }

    /**
     * Whether the given file is accepted by this filter.
     *
     * @param file File.
     * @return true is it is a directory or a file that can be accessed by
     * associated codec.
     */
    @Override
    public boolean accept(final File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return false;
        }
        if (file.isDirectory()) {
            return true;
        }

        String[] extensions = spi.getFileSuffixes();
        for (String extension : extensions) {
            if (file.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}

