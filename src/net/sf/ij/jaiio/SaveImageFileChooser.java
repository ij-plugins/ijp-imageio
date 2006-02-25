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
import non_com.media.jai.codec.ImageEncoder;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Extension of JFileChooser with ability to return pages selected in multi-image files (e.g.
 * TIFF).
 *
 * @author Jarek Sacha
 * @version $Revision: 1.4 $
 */

public class SaveImageFileChooser
        extends JFileChooser
        implements PropertyChangeListener {

    private String selectedFileDirectory;
    private String selectedFileRootName;

    /**
     * Constructor for the ImageFileChooser object
     */
    public SaveImageFileChooser(File currentDirectory) {
        super(currentDirectory);
        this.addPropertyChangeListener(this);

        this.setMultiSelectionEnabled(false);
        this.setDialogType(JFileChooser.SAVE_DIALOG);
        this.setAcceptAllFileFilterUsed(false);

        // Set filters corresponding to each available codec
        Enumeration codecs = ImageCodec.getCodecs();

        // Sort codec names
        TreeSet codecSet = new TreeSet();
        while (codecs.hasMoreElements()) {
            ImageCodec thisCodec = (ImageCodec) codecs.nextElement();
            String formatName = thisCodec.getFormatName();
            try {
                // Test if ImageEncoder can be instantiated.
                ImageEncoder imageEncoder = ImageCodec.createImageEncoder(formatName,
                        null, null);
                if (imageEncoder != null) {
                    codecSet.add(formatName);
                }
            } catch (Throwable t) {
                // Ignore ImageEncoders that cannot be instantiated
            }
        }

        JAIFileFilter defaultFilter = null;
        for (Iterator i = codecSet.iterator(); i.hasNext();) {
            try {
                String cadecName = (String) i.next();
                JAIFileFilter jaiFileFilter = new JAIFileFilter(cadecName);
                addChoosableFileFilter(jaiFileFilter);
                if (cadecName.toUpperCase().indexOf("TIFF") > -1) {
                    defaultFilter = jaiFileFilter;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        // Set selected filter
        if (defaultFilter != null) {
            setFileFilter(defaultFilter);
        }


        this.validate();
    }


    /**
     * This method gets called when a bound property is changed.
     * <p/>
     * JFileChooser have a tendency to reset name of selected file when a file filter is changed.
     * However, we would like to keep the name of current selection but change it extension that
     * matches current filter.
     * <p/>
     * This method is a hack that should enable to maintain a name of the selected file when file
     * filter is changed at the same time changing only extension of the file to match currently
     * selected filter.
     * <p/>
     * For instance, when the SaveImageFileChooser starts up a TIFF filter is selected and file name
     * is that of the Image that is being saved. When now a user selects a different file filter
     * JFileChooser parent class removes the name of the selected file from file name box (file name
     * selection box is empty). To put back the name of the file in to the selection box we monitor
     * notification about JFileChooser property changes and attempt to set proper value of the file
     * name in the selection box. </p>
     *
     * @param evt A PropertyChangeEvent object describing the event source and the property that has
     *            changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        final String propertyName = evt.getPropertyName();

        // File filter changed
        if (propertyName.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
            FileFilter fileFilter = this.getFileFilter();

            if (fileFilter instanceof JAIFileFilter) {
                JAIFileFilter jaiFileFilter = (JAIFileFilter) fileFilter;

                if (selectedFileDirectory != null) {
                    final String fileName = selectedFileDirectory + File.separator
                            + selectedFileRootName + "." + jaiFileFilter.getCodecName().toLowerCase();
                    final File selectedFile = new File(fileName);

                    // Hack to delate execution of setSelectedFile()
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setSelectedFile(selectedFile);
                        }
                    });
                }
            }
        }
        // Selected file changed
        else if (evt.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            // Trick here is to distinguish legitimate changes of the file selected 
            // from ones caused by changes of file filter. Not having much to
            // relay on e assume that legitimate changes never set the selected
            // file to null.

            File selectedFile = getSelectedFile();
            if (selectedFile == null) {
                // Attemt to set file name to null, try to recover using preserved 
                // inforamtion, if any.
                if (selectedFileDirectory != null) {
                    FileFilter fileFilter = this.getFileFilter();
                    JAIFileFilter jaiFileFilter = (JAIFileFilter) fileFilter;
                    String name = selectedFileDirectory + File.separator + selectedFileRootName + "."
                            + jaiFileFilter.getCodecName().toLowerCase();
                    final File recoveredSelectedFile = new File(name);

                    // Hack to delate execution of setSelectedFile()
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setSelectedFile(recoveredSelectedFile);
                        }
                    });
                }
            } else {
                // Preserve information about selected file
                selectedFileDirectory = getCurrentDirectory().getAbsolutePath();
                String name = selectedFile.getName();
                int lastDot = name.lastIndexOf(".");
                if (lastDot > -1) {
                    selectedFileRootName = name.substring(0, lastDot);
                }
            }
        }
        // Track changes in current directory
        else if (evt.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
            File newValue = (File) evt.getNewValue();
            if (newValue != null) {
                selectedFileDirectory = newValue.getAbsolutePath();
            }
            if (selectedFileDirectory != null) {
                FileFilter fileFilter = this.getFileFilter();
                JAIFileFilter jaiFileFilter = (JAIFileFilter) fileFilter;
                final String fileName = selectedFileDirectory + File.separator
                        + selectedFileRootName + "." + jaiFileFilter.getCodecName().toLowerCase();
                final File selectedFile = new File(fileName);

                // Hack to delate execution of setSelectedFile()
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setSelectedFile(selectedFile);
                    }
                });
            }
        }
    }
}
