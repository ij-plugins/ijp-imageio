/*
 * Image/J Plugins
 * Copyright (C) 2002-2008 Jarek Sacha
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
 *
 */

package net.sf.ij_plugins.imageio;

import ij.IJ;
import ij.ImagePlus;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Opens file chooser dialog and reads images using {@link IJImageIO}.
 *
 * @author Jarek Sacha
 */
public class IJImageIOOpenPlugin implements PlugIn {
    private static final String TITLE = "Image IO Open";

    /**
     * Main processing method for the IJImageIOOpenPlugin object.
     *
     * @param arg Not used.
     * @see net.sf.ij.plugin.ImageIOOpenPlugin
     */
    public void run(final String arg) {

        IJ.showStatus("Starting \"" + TITLE + "\" plugin...");

        final OpenDialog openDialog = new OpenDialog(TITLE, null);
        if (openDialog.getFileName() == null) {
            // No selection
            return;
        }

        final File file = new File(openDialog.getDirectory(), openDialog.getFileName());

        IJ.showStatus("Opening: " + file.getName());
        final List<ImagePlus> imageList = new ArrayList<ImagePlus>();
        try {
            final ImagePlus[] images = IJImageIO.read(file);
            if (images != null) {
                imageList.addAll(Arrays.asList(images));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            String message = "Error opening file: " + file.getName() + ".\n\n";
            message += (ex.getMessage() == null) ? ex.toString() : ex.getMessage();
            IJ.showMessage(TITLE, message);
        }

        // Display images.
        for (final ImagePlus anImageList : imageList) {
            anImageList.show();
        }
    }

}
