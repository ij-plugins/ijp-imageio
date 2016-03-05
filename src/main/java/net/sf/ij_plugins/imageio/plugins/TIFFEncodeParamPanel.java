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

import com.sun.media.imageio.plugins.tiff.TIFFImageWriteParam;

import javax.imageio.ImageWriteParam;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;

//
// TODO: Selection of deflation level.
// TODO: Selection of JPEG options
//


/**
 * Component for editing TIFF encoding options represented by {@code non_com.media.jai.codec.TIFFEncodeParam}.
 *
 * @author Jarek Sacha
 */

public class TIFFEncodeParamPanel extends JPanel {

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
    JPanel jPanel1 = new JPanel();
    GridBagLayout gridBagLayout5 = new GridBagLayout();


    /**
     * Constructor for the TIFFEncodeParamBox object
     */
    public TIFFEncodeParamPanel() {

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
    public ImageWriteParam getImageWriteParam(final boolean blackWhite) {
        final TIFFImageWriteParam param = new TIFFImageWriteParam(Locale.US);
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        if (blackWhite) {
            if (rleRadioButton.isSelected()) {
                param.setCompressionType("CCITT RLE");
            } else if (faxT4RadioButton.isSelected()) {
                param.setCompressionType("CCITT T.4");
            } else if (faxT6RadioButton.isSelected()) {
                param.setCompressionType("CCITT T.6");
            }
        } else {
            if (noneRadioButton.isSelected()) {
                param.setCompressionMode(ImageWriteParam.MODE_DISABLED);
            } else if (packbitRadioButton.isSelected()) {
                param.setCompressionType("PackBits");
            } else if (lzwRadioButton.isSelected()) {
                param.setCompressionType("LZW");
            } else if (jpegRadioButton.isSelected()) {
                param.setCompressionType("JPEG");
                param.setCompressionQuality(1);
            } else if (zipRadioButton.isSelected()) {
                param.setCompressionType("ZLib");
            } else if (deflateRadioButton.isSelected()) {
                param.setCompressionType("Deflate");
            }
        }
        return param;
    }


    /**
     * The main program for the TIFFEncodeParamBox class
     *
     * @param args The command line arguments
     */
    public static void main(final String[] args) {
        final TIFFEncodeParamPanel panel = new TIFFEncodeParamPanel();

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
        final TitledBorder titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), "Compression");
        this.setLayout(gridBagLayout3);
        optionsPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Compression", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        optionsPanel.setLayout(gridBagLayout4);
        zipRadioButton.setText("ZLib");
        deflateRadioButton.setText("Deflate");
        jpegRadioButton.setText("JPEG");
        lzwRadioButton.setText("LZW");
        packbitRadioButton.setToolTipText("");
        packbitRadioButton.setText("PackBits");
        compressionPanel.setLayout(gridBagLayout1);
        noneRadioButton.setToolTipText("");
        noneRadioButton.setSelected(true);
        noneRadioButton.setText("None");
        bwCompressionPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "For black/white only", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        bwCompressionPanel.setLayout(gridBagLayout2);
        faxT6RadioButton.setText("CCITT T.6");
        faxT4RadioButton.setText("CCITT T.4");
        rleRadioButton.setSelected(true);
        rleRadioButton.setText("CCITT RLE");
        jPanel1.setLayout(gridBagLayout5);
        this.add(optionsPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
        optionsPanel.add(compressionPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        optionsPanel.add(bwCompressionPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
        bwCompressionPanel.add(rleRadioButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        bwCompressionPanel.add(faxT4RadioButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        bwCompressionPanel.add(faxT6RadioButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        optionsPanel.add(jPanel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
        jPanel1.add(noneRadioButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        jPanel1.add(lzwRadioButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        jPanel1.add(deflateRadioButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        jPanel1.add(packbitRadioButton, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        jPanel1.add(jpegRadioButton, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        jPanel1.add(zipRadioButton, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
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
