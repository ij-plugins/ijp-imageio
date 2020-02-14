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

package net.sf.ij_plugins.imageio.impl;

import ij.IJ;
import net.sf.ij_plugins.imageio.IJImageOUtils;

import javax.imageio.spi.ImageWriterSpi;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Extension of JFileChooser with ability to return pages selected in multi-image files (e.g. TIFF).
 *
 * @author Jarek Sacha
 */
public class SaveImageFileChooser
        extends JFileChooser
        implements PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String selectedFileDirectory;
    private String selectedFileRootName;
    private String preferredSelectionExtension = "tif";

    /**
     * Constructor for the JAIImageFileChooser object
     *
     * @param currentDirectory initial directory.
     */
    SaveImageFileChooser(final File currentDirectory) {
        super(currentDirectory);
        this.addPropertyChangeListener(this);

        this.setMultiSelectionEnabled(false);
        this.setDialogType(JFileChooser.SAVE_DIALOG);
        this.setAcceptAllFileFilterUsed(false);

        final List<ImageWriterSpi> spis = IJImageOUtils.getImageWriterSpis();

        ImageIOWriterFileFilter defaultFilter = null;
        final ArrayList<ImageIOWriterFileFilter> filters = new ArrayList<>();
        for (ImageWriterSpi spi : spis) {
            try {
                final String extension = defaultExtension(spi);
                String spiDescription = spi.getDescription(null);
                if (spiDescription.endsWith(" Image Writer")) {
                    spiDescription = spiDescription.substring(0, spiDescription.length() - " Image Writer".length());
                }
                final ImageIOWriterFileFilter fileFilter = new ImageIOWriterFileFilter(spi, extension.toUpperCase() + " - " + spiDescription);
                filters.add(fileFilter);
                if (extension.equalsIgnoreCase(preferredSelectionExtension)) {
                    defaultFilter = fileFilter;
                }
            } catch (final Throwable t) {
                t.printStackTrace();
            }
        }

        filters.sort(Comparator.comparing(ImageIOWriterFileFilter::getDescription));
        filters.forEach(this::addChoosableFileFilter);

        // Set selected filter
        if (defaultFilter != null) {
            setFileFilter(defaultFilter);
        }


        this.validate();
    }


    private String defaultExtension(ImageWriterSpi spi) {
        String[] extensions = spi.getFileSuffixes();
        final String extension;
        if (extensions == null || extensions.length < 1 || (extensions[0].trim().isEmpty())) {
            extension = "raw";
        } else {
            extension = extensions[0];
        }
        return extension;
    }


    /**
     * This method gets called when a bound property is changed.
     * <br>
     * JFileChooser have a tendency to reset name of selected file when a file filter is changed.
     * However, we would like to keep the name of current selection but change it extension that
     * matches current filter.
     * <br>
     * This method is a hack that should enable to maintain a name of the selected file when file
     * filter is changed at the same time changing only extension of the file to match currently
     * selected filter.
     * <br>
     * For instance, when the SaveImageFileChooser starts up a TIFF filter is selected and file name
     * is that of the Image that is being saved. When now a user selects a different file filter
     * JFileChooser parent class removes the name of the selected file from file name box (file name
     * selection box is empty). To put back the name of the file in to the selection box we monitor
     * notification about JFileChooser property changes and attempt to set proper value of the file
     * name in the selection box.
     *
     * @param evt A PropertyChangeEvent object describing the event source and the property that has
     *            changed.
     */
    public void propertyChange(final PropertyChangeEvent evt) {
        final String propertyName = evt.getPropertyName();
        if (IJ.debugMode) {
            System.out.println("propertyName = " + propertyName);
        }

        // File filter changed
        if (propertyName.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
            final FileFilter fileFilter = this.getFileFilter();

            if (fileFilter instanceof ImageIOWriterFileFilter) {
                final ImageIOWriterFileFilter imageFileFilter = (ImageIOWriterFileFilter) fileFilter;
                possiblyRecoverRecoveredFile(imageFileFilter);
            }
        } else if (evt.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            // Trick here is to distinguish legitimate changes of the file selected
            // from ones caused by changes of file filter. Not having much to
            // relay on assume that legitimate changes never set the selected
            // file to null.

            final File selectedFile = getSelectedFile();
            if (selectedFile != null) {
                // Preserve information about selected file
                selectedFileDirectory = getCurrentDirectory().getAbsolutePath();
                final String name = selectedFile.getName();
                final int lastDot = name.lastIndexOf(".");
                if (lastDot > -1) {
                    selectedFileRootName = name.substring(0, lastDot);
                } else {
                    selectedFileRootName = name;
                }
            }
            possiblyRecoverRecoveredFile();
        } else
            // Track changes in current directory
            if (evt.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                final File newValue = (File) evt.getNewValue();
                if (newValue != null) {
                    selectedFileDirectory = newValue.getAbsolutePath();
                }
                possiblyRecoverRecoveredFile();
            }
    }

    private void possiblyRecoverRecoveredFile() {
        final FileFilter fileFilter = this.getFileFilter();
        if (fileFilter instanceof ImageIOWriterFileFilter) {
            final ImageIOWriterFileFilter imageFileFilter = (ImageIOWriterFileFilter) fileFilter;
            possiblyRecoverRecoveredFile(imageFileFilter);
        }
    }

    private void possiblyRecoverRecoveredFile(final ImageIOWriterFileFilter imageFileFilter) {
        if (selectedFileDirectory != null && selectedFileRootName != null) {
            final String extension = defaultExtension(imageFileFilter.getSPI());
            final String name = selectedFileDirectory + File.separator + selectedFileRootName + "." + extension;
            // Hack to delay execution of setSelectedFile()
            SwingUtilities.invokeLater(() -> setSelectedFile(new File(name)));
        }
    }
}
