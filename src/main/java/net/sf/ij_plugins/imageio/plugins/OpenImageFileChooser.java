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

package net.sf.ij_plugins.imageio.plugins;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Extension of JFileChooser with ability to return pages selected in multi-image files (e.g.
 * TIFF).
 *
 * @author Jarek Sacha
 */
class OpenImageFileChooser
        extends JFileChooser {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final ImagePreviewPanel previewer = new ImagePreviewPanel(this);


    /**
     * Constructor for the JAIImageFileChooser object
     *
     * @param currentDirectory initial directory.
     */
    public OpenImageFileChooser(final File currentDirectory) {
        super(currentDirectory);

        this.setAccessory(previewer);

        final String[] allFormatNames = ImageIO.getReaderFileSuffixes();
        final Set<String> lowerCaseFormatNames = new HashSet<>();
        for (String f : allFormatNames) {
            if (!f.trim().isEmpty()) lowerCaseFormatNames.add(f.toLowerCase());
        }

        String[] formatNameArray = lowerCaseFormatNames.toArray(new String[lowerCaseFormatNames.size()]);
        Arrays.sort(formatNameArray);

//
        // Add filter for all supported image types
//        final JAIFileFilter allSupportedFileFilter = new JAIFileFilter();
//        this.addChoosableFileFilter(allSupportedFileFilter);
//
//        // Set filters corresponding to each available codec
//        final Enumeration codecs = ImageCodec.getCodecs();
//
//        // Sort codec names
//        final TreeSet<String> codecSet = new TreeSet<String>();
//        while (codecs.hasMoreElements()) {
//            final ImageCodec thisCodec = (ImageCodec) codecs.nextElement();
//            codecSet.add(thisCodec.getFormatName());
//        }
//
//        for (final String aCodecSet : codecSet) {
//            this.addChoosableFileFilter(new JAIFileFilter(aCodecSet));
//        }
//
//        // Set selected filter
//        this.setFileFilter(allSupportedFileFilter);

        for (String format : formatNameArray) {
            this.addChoosableFileFilter(new FileNameExtensionFilter(format.toUpperCase() + " file", format, format.toUpperCase()));
        }

        this.validate();
    }


    /**
     * Return index of pages selected for current file using page selection dialog. This works only
     * multi-image files and and only when a single file is selected.
     *
     * @return An array containing indexes of selected pages.
     * @see ImagePageSelectionDialog
     */
    public int[] getPageIndex() {
        final File[] selection = getSelectedFiles();
        if (selection != null && selection.length == 1) {
            return previewer.getPageIndex();
        } else {
            return null;
        }
    }
}
