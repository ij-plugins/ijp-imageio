/***
 * Image/J Plugins
 * Copyright (C) 2002-2004 Jarek Sacha
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
package net.sf.ij.jaiio;

import non_com.media.jai.codec.ImageCodec;

import javax.swing.*;
import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Extension of JFileChooser with ability to return pages selected in multi-image files (e.g. TIFF).
 * 
 * @author Jarek Sacha
 * @version $Revision: 1.2 $
 */

public class ImageFileChooser
        extends JFileChooser {

    JAIFilePreviewer previewer = new JAIFilePreviewer(this);


    /**
     * Constructor for the ImageFileChooser object
     */
    public ImageFileChooser(File currentDirectory) {
        super(currentDirectory);

        this.setAccessory(previewer);

        // Add filter for all supported image types
        JAIFileFilter allSupportedFileFilter = new JAIFileFilter();
        this.addChoosableFileFilter(allSupportedFileFilter);

        // Set filters corresponding to each available codec
        Enumeration codecs = ImageCodec.getCodecs();

        // Sort codec names
        TreeSet codecSet = new TreeSet();
        while (codecs.hasMoreElements()) {
            ImageCodec thisCodec = (ImageCodec) codecs.nextElement();
            codecSet.add(thisCodec.getFormatName());
        }

        for (Iterator i = codecSet.iterator(); i.hasNext();) {
            try {
                this.addChoosableFileFilter(new JAIFileFilter((String) i.next()));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        // Set selected filter
        this.setFileFilter(allSupportedFileFilter);

        this.validate();
    }


    /**
     * Return index of pages selected for current file using page selection dialog. This works only mulit-image files
     * and and only when a single file is selected.
     * 
     * @return An array containing indexes of selected pages.
     * @see net.sf.ij.jaiio.ImagePageSelectionDialog
     */
    public int[] getPageIndex() {
        File[] selection = getSelectedFiles();
        if (selection != null && selection.length == 1) {
            return previewer.getPageIndex();
        } else {
            return null;
        }
    }
}
