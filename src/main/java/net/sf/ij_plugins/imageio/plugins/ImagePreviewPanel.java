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

import ij.ImagePlus;
import ij.process.ByteProcessor;
import net.sf.ij_plugins.imageio.IJImageIO;
import net.sf.ij_plugins.imageio.IJImageIOException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * A utility for JFIleChooser that displays preview image, image file size, and image dimensions.
 *
 * @author Jarek Sacha
 */
class ImagePreviewPanel extends JPanel implements PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    static final String FILE_SIZE_PREFIX = "";
    static final long SIZE_KB = 1024;
    static final long SIZE_MB = SIZE_KB * 1024;
    static final long SIZE_GB = SIZE_MB * 1024;

    protected File file;
    protected int iconSizeX = 150;
    protected int iconSizeY = 100;

    private IJImageIO.ImageInfo imageInfo;
    private int[] pageIndex = null;
    private final ImagePageSelectionDialog imagePageSelectionDialog = new ImagePageSelectionDialog();
    /**
     * Interacts with parent determine number of selected files, to enable/disable page selection dialog.
     */
    private JFileChooser parentChooser;

    private final JPanel infoPanel = new JPanel();
    private final JLabel fileSizeLabel = new JLabel();
    private final JLabel ImageIconLabel = new JLabel();
    private final JButton selectPagesButton = new JButton();


    /**
     * Constructor for the FilePreviewer object
     */
    public ImagePreviewPanel() {
        initializeComponents();
        final ImagePlus imp = new ImagePlus("", new ByteProcessor(iconSizeX, iconSizeY));
        final ImageIcon imageIcon = new ImageIcon(imp.getImage());
        ImageIconLabel.setIcon(imageIcon);
        validate();
    }


    /**
     * Creates new FilePreviewer
     *
     * @param fc File chooser that this object is associated with.
     */
    public ImagePreviewPanel(final JFileChooser fc) {
        parentChooser = fc;
        initializeComponents();
        fc.addPropertyChangeListener(this);
    }


    /**
     * Gets the PageIndex attribute of the JAIFilePreviewer object
     *
     * @return The PageIndex value
     */
    public int[] getPageIndex() {
        final int[] r;
        if (pageIndex != null) {
            r = new int[pageIndex.length];
            System.arraycopy(pageIndex, 0, r, 0, pageIndex.length);
        } else {
            r = null;
        }

        return r;
    }


    /**
     * Updates image preview when received JFileChooser.SELECTED_FILE_CHANGED_PROPERTY event. This
     * method should not be called directly.
     *
     * @param e Event.
     */
    public void propertyChange(final PropertyChangeEvent e) {
        final String prop = e.getPropertyName();
        if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
            file = (File) e.getNewValue();
            if (isShowing()) {
                loadImage();
                repaint();
            }
        }
    }


    private void selectPagesButtonAction() {
        if (imageInfo == null) {
            return;
        }

        if (parentChooser.getSelectedFiles() != null
                && parentChooser.getSelectedFiles().length > 1) {
            selectPagesButton.setEnabled(false);
            JOptionPane.showMessageDialog(this,
                    "Cannot select pages when multiple files are selected.",
                    "Select pages...", JOptionPane.WARNING_MESSAGE);
            return;
        }

        imagePageSelectionDialog.setNumPages(imageInfo.numberOfPages);
        imagePageSelectionDialog.setVisible(true);
        pageIndex = imagePageSelectionDialog.getPageIndex();
    }


    /*
     *
     */
    private static String getFileSizeString(final long fileSize) {
        String fileSizeString;
        if (fileSize < SIZE_KB) {
            fileSizeString = FILE_SIZE_PREFIX + fileSize;
        } else if (fileSize < SIZE_MB) {
            fileSizeString = FILE_SIZE_PREFIX +
                    (int) ((double) fileSize / SIZE_KB + 0.5) + "KB";
        } else if (fileSize < SIZE_GB) {
            fileSizeString = FILE_SIZE_PREFIX +
                    (int) ((double) fileSize / SIZE_MB + 0.5) + "MB";
        } else {
            fileSizeString = FILE_SIZE_PREFIX +
                    (int) ((double) fileSize / SIZE_GB + 0.5) + "GB";
        }

        return fileSizeString;
    }


    /**
     * Load first image in the file.
     */
    private void loadImage() {
        pageIndex = null;

        if (file == null || file.isDirectory()) {
//      ImageIconLabel.setIcon(null);
            fileSizeLabel.setText(" ");
            selectPagesButton.setEnabled(false);
            return;
        }

        try {
            imageInfo = IJImageIO.readPreviewAndInfo(file);
        } catch (final RuntimeException e) {
            ImageIconLabel.setIcon(null);
            fileSizeLabel.setText("Error decoding image");
            return;
        } catch (final IJImageIOException e) {
            ImageIconLabel.setIcon(null);
            fileSizeLabel.setText("Error decoding image");
            return;
        }

        Image image = imageInfo.previewImage;

        // Set image size label
        final StringBuffer label = new StringBuffer(getFileSizeString(file.length()));
        if (image != null) {
            final int w = image.getWidth(null);
            final int h = image.getHeight(null);
            if (w > 0 && h > 0) {
                label.append("  [").append(w).append("x").append(h);
                if (imageInfo.numberOfPages > 1) {
                    label.append("x").append(imageInfo.numberOfPages).append("]");
                    final File[] selectedFiles = parentChooser.getSelectedFiles();
                    final File selectedFile = parentChooser.getSelectedFile();
                    if ((selectedFiles != null && selectedFiles.length == 1)
                            || ((selectedFiles == null || selectedFiles.length == 0)
                            && selectedFile != null)) {
                        selectPagesButton.setEnabled(true);
                    } else {
                        selectPagesButton.setEnabled(false);
                    }
                } else {
                    label.append("]");
                    selectPagesButton.setEnabled(false);
                }
            }

            final int xSizeBuffered = image.getWidth(null);
            final int ySizeBuffered = image.getHeight(null);
            if (xSizeBuffered > iconSizeX || ySizeBuffered > iconSizeY) {
                // Replace image by its scaled version
                final double scaleX = (double) iconSizeX / xSizeBuffered;
                final double scaleY = (double) iconSizeY / ySizeBuffered;
                if (scaleX < scaleY) {
                    image = image.getScaledInstance(iconSizeX, -1, Image.SCALE_DEFAULT);
                } else {
                    image = image.getScaledInstance(-1, iconSizeY, Image.SCALE_DEFAULT);
                }
            }

            final ImageIcon imageIcon = new ImageIcon(image);
            ImageIconLabel.setIcon(imageIcon);
        } else {
            ImageIconLabel.setIcon(null);
        }


        fileSizeLabel.setText(label.toString());
    }


    /*
     *
     */
    private void initializeComponents() {
        this.setLayout(new GridBagLayout());
        fileSizeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        fileSizeLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        fileSizeLabel.setText(" ");
        infoPanel.setLayout(new BorderLayout());
        ImageIconLabel.setMaximumSize(new Dimension(iconSizeX, iconSizeY));
        ImageIconLabel.setMinimumSize(new Dimension(iconSizeX, iconSizeY));
        ImageIconLabel.setPreferredSize(new Dimension(iconSizeX, iconSizeY));
        ImageIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        selectPagesButton.setEnabled(false);
        selectPagesButton.setText("Select pages...");
        selectPagesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                selectPagesButtonAction();
            }
        });
        this.add(infoPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        infoPanel.add(fileSizeLabel, BorderLayout.CENTER);
        this.add(ImageIconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(selectPagesButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                , GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
    }
}

