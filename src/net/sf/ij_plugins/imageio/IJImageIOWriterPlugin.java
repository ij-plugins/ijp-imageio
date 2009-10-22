/*
 * Image/J Plugins
 * Copyright (C) 2002-2009 Jarek Sacha
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
package net.sf.ij_plugins.imageio;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;

import java.io.File;

/**
 * Opens file chooser dialog and reads images using {@link net.sf.ij_plugins.imageio.IJImageIO}.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.2 $
 */
public class IJImageIOWriterPlugin implements PlugIn {
    private static final String TITLE = "Image IO Save As...";

    /**
     * Main processing method for the IJImageIOWriterPlugin object.
     */
    public void run(final String codecName) {

        IJ.showStatus("Starting \"" + TITLE + "\" plugin...");

        final ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.showMessage(TITLE, "No images are open.");
            return;
        }

        final SaveDialog saveDialog = new SaveDialog("Save As " + codecName + "...", imp.getTitle(), "." + codecName);
        final String saveDialogFileName = saveDialog.getFileName();
        if (saveDialogFileName == null) {
            return;
        }

        final String fileName;
        if (saveDialog.getDirectory() != null) {
            fileName = saveDialog.getDirectory() + File.separator + saveDialogFileName;
        } else {
            fileName = saveDialogFileName;
        }

        try {
            final boolean ok = IJImageIO.write(imp, codecName, new File(fileName), true);
            if (!ok) {
                throw new IJImageIOException("Writer for format '" + codecName + "' not available.");
            }
        } catch (final IJImageIOException e) {
            IJ.error(TITLE, e.getMessage());
        }
    }
}
