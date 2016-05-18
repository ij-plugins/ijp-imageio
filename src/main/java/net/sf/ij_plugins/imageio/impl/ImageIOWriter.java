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

import ij.gui.GenericDialog;

import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.util.Optional;

/**
 * @author Jarek Sacha
 */
public class ImageIOWriter {

    public static Optional<ImageWriteParam> askForCompressionParams(final ImageWriter writer,
                                                                    final String title,
                                                                    final String defaultCompression) {
        final ImageWriteParam writerParam = writer.getDefaultWriteParam();
        if (writerParam.canWriteCompressed()) {
            writerParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            final String[] compressionTypes = writerParam.getCompressionTypes();
            if (compressionTypes != null && compressionTypes.length > 1) {
                GenericDialog dialog = new GenericDialog(title);
                dialog.addChoice("Compression type",
                        compressionTypes,
                        (defaultCompression != null) ? defaultCompression : compressionTypes[0]);
                dialog.showDialog();

                if (dialog.wasCanceled()) {
                    return Optional.empty();
                }

                writerParam.setCompressionType(dialog.getNextChoice());
            }
        }

        return Optional.of(writerParam);
    }

}
