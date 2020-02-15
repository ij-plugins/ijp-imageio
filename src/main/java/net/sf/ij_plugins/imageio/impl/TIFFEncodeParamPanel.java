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

import net.sf.ij_plugins.imageio.IJImageIOException;

import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static net.sf.ij_plugins.imageio.IJImageIO.getTIFFWriter;

//
// TODO: Selection of deflation level.
// TODO: Selection of JPEG options
//


/**
 * Component for editing TIFF encoding options represented by {@code non_com.media.jai.codec.TIFFEncodeParam}.
 *
 * @author Jarek Sacha
 */

class TIFFEncodeParamPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final GridBagLayout gridBagLayout3 = new GridBagLayout();
    private final ButtonGroup compressionGroup = new ButtonGroup();
    private final ButtonGroup bwCompressionGroup = new ButtonGroup();
    private final JPanel optionsPanel = new JPanel();
    private final GridBagLayout gridBagLayout4 = new GridBagLayout();
    private final JRadioButton zipRadioButton = new JRadioButton();
    private final JRadioButton jpegRadioButton = new JRadioButton();
    private final JRadioButton deflateRadioButton = new JRadioButton();
    private final JRadioButton lzwRadioButton = new JRadioButton();
    private final JRadioButton packbitRadioButton = new JRadioButton();
    private final JPanel compressionPanel = new JPanel();
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JRadioButton noneRadioButton = new JRadioButton();
    private final JPanel bwCompressionPanel = new JPanel();
    private final JRadioButton faxT6RadioButton = new JRadioButton();
    private final JRadioButton faxT4RadioButton = new JRadioButton();
    private final GridBagLayout gridBagLayout2 = new GridBagLayout();
    private final JRadioButton rleRadioButton = new JRadioButton();
    //    private JPanel jPanel1 = new JPanel();
    private GridBagLayout gridBagLayout5 = new GridBagLayout();
    final private boolean useOneBitCompression;

    /**
     * Constructor for the TIFFEncodeParamBox object
     */
    TIFFEncodeParamPanel(final boolean useOneBitCompression) {
        this.useOneBitCompression = useOneBitCompression;

//    deflateBox = Box.createHorizontalBox();
//
//    deflateCheckBox = new JCheckBox(" ZIP ");
//    deflateBox.add(deflateCheckBox);
//
//    deflateComboBox = new JComboBox(deflateLevels);
//    deflateComboBox.setEnabled(deflateCheckBox.isSelected());
//    deflateComboBox.setSelectedIndex(8);
//    deflateBox.add(deflateComboBox);
        try {
            jbInit();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Gets the imageEncodeParam attribute of the TIFFEncodeParamPanel object
     *
     * @param blackWhite Description of the Parameter
     * @return The imageEncodeParam value
     */
    ImageWriteParam getImageWriteParam(final boolean blackWhite) throws IJImageIOException {
        final ImageWriter imageWriter = getTIFFWriter();

        // Set compression parameters
        final ImageWriteParam writerParam = imageWriter.getDefaultWriteParam();
        writerParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        if (blackWhite) {
            if (rleRadioButton.isSelected()) {
                writerParam.setCompressionType("CCITT RLE");
            } else if (faxT4RadioButton.isSelected()) {
                writerParam.setCompressionType("CCITT T.4");
            } else if (faxT6RadioButton.isSelected()) {
                writerParam.setCompressionType("CCITT T.6");
            }
        } else {
            if (noneRadioButton.isSelected()) {
                writerParam.setCompressionMode(ImageWriteParam.MODE_DISABLED);
            } else if (packbitRadioButton.isSelected()) {
                writerParam.setCompressionType("PackBits");
            } else if (lzwRadioButton.isSelected()) {
                writerParam.setCompressionType("LZW");
            } else if (jpegRadioButton.isSelected()) {
                writerParam.setCompressionType("JPEG");
                writerParam.setCompressionQuality(1);
            } else if (zipRadioButton.isSelected()) {
                writerParam.setCompressionType("ZLib");
            } else if (deflateRadioButton.isSelected()) {
                writerParam.setCompressionType("Deflate");
            }
        }
        return writerParam;
    }


    /**
     * The main program for the TIFFEncodeParamBox class
     *
     * @param args The command line arguments
     */
    public static void main(final String[] args) {
        final TIFFEncodeParamPanel panel = new TIFFEncodeParamPanel(false);

        final JFrame frame = new JFrame("TIFFEncodeParamPanel");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                System.exit(0);
            }
        });
        frame.getContentPane().add("Center", panel);
        frame.pack();
        frame.setVisible(true);
    }


    private void jbInit() {
        this.setLayout(gridBagLayout3);
        optionsPanel.setBorder(
                new TitledBorder(UIManager.getBorder("TitledBorder.border"),
                        "TIFF Compression", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        optionsPanel.setLayout(gridBagLayout4);

        zipRadioButton.setText("ZLib");
        zipRadioButton.setEnabled(!useOneBitCompression);
        deflateRadioButton.setText("Deflate");
        deflateRadioButton.setEnabled(!useOneBitCompression);
        jpegRadioButton.setText("JPEG");
        jpegRadioButton.setEnabled(!useOneBitCompression);
        lzwRadioButton.setText("LZW");
        lzwRadioButton.setEnabled(!useOneBitCompression);
        packbitRadioButton.setText("PackBits    ");
        packbitRadioButton.setEnabled(!useOneBitCompression);
        noneRadioButton.setSelected(true);
        noneRadioButton.setText("None");
        noneRadioButton.setEnabled(!useOneBitCompression);

        compressionPanel.setLayout(gridBagLayout1);
        compressionPanel.setEnabled(!useOneBitCompression);

        bwCompressionPanel.setBorder(
                new TitledBorder(UIManager.getBorder("TitledBorder.border"),
                        "For binary only", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        bwCompressionPanel.setLayout(gridBagLayout2);
        bwCompressionPanel.setEnabled(useOneBitCompression);

        faxT6RadioButton.setText("CCITT T.6          ");
        faxT6RadioButton.setEnabled(useOneBitCompression);
        faxT4RadioButton.setText("CCITT T.4          ");
        faxT4RadioButton.setEnabled(useOneBitCompression);
        rleRadioButton.setSelected(true);
        rleRadioButton.setText("CCITT RLE          ");
        rleRadioButton.setEnabled(useOneBitCompression);

        this.add(optionsPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

        optionsPanel.add(compressionPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

        optionsPanel.add(bwCompressionPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

        bwCompressionPanel.add(rleRadioButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        bwCompressionPanel.add(faxT4RadioButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        bwCompressionPanel.add(faxT6RadioButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));

        compressionPanel.setLayout(gridBagLayout5);
        compressionPanel.add(noneRadioButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        compressionPanel.add(zipRadioButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        compressionPanel.add(lzwRadioButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        compressionPanel.add(deflateRadioButton, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        compressionPanel.add(packbitRadioButton, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        compressionPanel.add(jpegRadioButton, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));


        compressionGroup.add(noneRadioButton);
        compressionGroup.add(lzwRadioButton);
        compressionGroup.add(deflateRadioButton);
        compressionGroup.add(packbitRadioButton);
        compressionGroup.add(jpegRadioButton);
        compressionGroup.add(zipRadioButton);
        bwCompressionGroup.add(rleRadioButton);
        bwCompressionGroup.add(faxT4RadioButton);
        bwCompressionGroup.add(faxT6RadioButton);
    }
}
