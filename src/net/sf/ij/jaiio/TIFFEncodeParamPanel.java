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

import non_com.media.jai.codec.ImageEncodeParam;
import non_com.media.jai.codec.TIFFEncodeParam;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//
// TODO: Selection of deflation level.
// TODO: Selection of JPEG options
//


/**
 * Component for editing TIFF encoding options represented by <code>non_com.media.jai.codec.TIFFEncodeParam</code>.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.4 $
 */

public class TIFFEncodeParamPanel extends JPanel {

    private GridBagLayout gridBagLayout3 = new GridBagLayout();
    private ButtonGroup compressionGroup = new ButtonGroup();
    private ButtonGroup bwCompressionGroup = new ButtonGroup();
    private JPanel optionsPanel = new JPanel();
    private GridBagLayout gridBagLayout4 = new GridBagLayout();
    private JRadioButton zipRadioButton = new JRadioButton();
    private JRadioButton jpegRadioButton = new JRadioButton();
    private JRadioButton packbitRadioButton = new JRadioButton();
    private JPanel compressionPanel = new JPanel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JRadioButton noneRadioButton = new JRadioButton();
    private JPanel bwCompressionPanel = new JPanel();
    private JRadioButton fax4RadioButton = new JRadioButton();
    private JRadioButton fax3RadioButton = new JRadioButton();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private JRadioButton rleRadioButton = new JRadioButton();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Gets the imageEncodeParam attribute of the TIFFEncodeParamPanel object
     *
     * @param blackWhite Description of the Parameter
     * @return The imageEncodeParam value
     */
    public ImageEncodeParam getImageEncodeParam(boolean blackWhite) {
        TIFFEncodeParam param = new TIFFEncodeParam();
        if (blackWhite) {
            if (rleRadioButton.isSelected()) {
                param.setCompression(TIFFEncodeParam.COMPRESSION_GROUP3_1D);
            } else if (fax3RadioButton.isSelected()) {
                param.setCompression(TIFFEncodeParam.COMPRESSION_GROUP3_2D);
            } else if (fax4RadioButton.isSelected()) {
                param.setCompression(TIFFEncodeParam.COMPRESSION_GROUP4);
            }
        } else {
            if (noneRadioButton.isSelected()) {
                param.setCompression(TIFFEncodeParam.COMPRESSION_NONE);
            } else if (packbitRadioButton.isSelected()) {
                param.setCompression(TIFFEncodeParam.COMPRESSION_PACKBITS);
            } else if (jpegRadioButton.isSelected()) {
                param.setCompression(TIFFEncodeParam.COMPRESSION_JPEG_TTN2);
            } else if (zipRadioButton.isSelected()) {
                param.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
            }
        }
        param.setDeflateLevel(9);
        param.setWriteTiled(false);
        return param;
    }


    /**
     * The main program for the TIFFEncodeParamBox class
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        TIFFEncodeParamPanel panel = new TIFFEncodeParamPanel();

        JFrame frame = new JFrame("TIFFEncodeParamPanel");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.getContentPane().add("Center", panel);
        frame.pack();
        frame.setVisible(true);
    }


    private void jbInit() throws Exception {
        TitledBorder titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), "Compression");
        TitledBorder titledBorder2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), "Black/White Images Only");
        this.setLayout(gridBagLayout3);
        optionsPanel.setBorder(titledBorder1);
        optionsPanel.setLayout(gridBagLayout4);
        zipRadioButton.setText("ZIP");
        jpegRadioButton.setText("JPEG");
        packbitRadioButton.setToolTipText("");
        packbitRadioButton.setText("Packbits");
        compressionPanel.setLayout(gridBagLayout1);
        noneRadioButton.setToolTipText("");
        noneRadioButton.setSelected(true);
        noneRadioButton.setText("None");
        bwCompressionPanel.setBorder(titledBorder2);
        bwCompressionPanel.setLayout(gridBagLayout2);
        fax4RadioButton.setText("CCITT Fax 4");
        fax3RadioButton.setText("CCITT Fax 3");
        rleRadioButton.setSelected(true);
        rleRadioButton.setText("Huffman RLE");
        jPanel1.setLayout(gridBagLayout5);
        this.add(optionsPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        optionsPanel.add(compressionPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        optionsPanel.add(bwCompressionPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        bwCompressionPanel.add(rleRadioButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        bwCompressionPanel.add(fax3RadioButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        bwCompressionPanel.add(fax4RadioButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        optionsPanel.add(jPanel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(noneRadioButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
        jPanel1.add(packbitRadioButton, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        jPanel1.add(jpegRadioButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        jPanel1.add(zipRadioButton, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        compressionGroup.add(noneRadioButton);
        compressionGroup.add(packbitRadioButton);
        compressionGroup.add(jpegRadioButton);
        compressionGroup.add(zipRadioButton);
        bwCompressionGroup.add(rleRadioButton);
        bwCompressionGroup.add(fax3RadioButton);
        bwCompressionGroup.add(fax4RadioButton);
    }
}
