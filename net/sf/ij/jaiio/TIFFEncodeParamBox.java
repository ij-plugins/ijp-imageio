/***
 * Image/J Plugins
 * Copyright (C) 2002 Jarek Sacha
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

import java.awt.event.*;
import java.util.zip.Deflater;

import javax.swing.*;

import non_com.media.jai.codec.TIFFEncodeParam;

/**
 *  Title: Description: Copyright: GPL 2002 Company:
 *
 * @author
 * @created    September 27, 2002
 * @version    $Revision: 1.1 $
 */

public class TIFFEncodeParamBox extends Box {

  private Box deflateBox;
  private JCheckBox deflateCheckBox;
  private JComboBox deflateComboBox;
  private String[] deflateLevels = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};


  /**  Constructor for the TIFFEncodeParamBox object */
  public TIFFEncodeParamBox() {
    super(BoxLayout.Y_AXIS);

    deflateBox = Box.createHorizontalBox();

    deflateCheckBox = new JCheckBox(" ZIP ");
    deflateBox.add(deflateCheckBox);

    deflateComboBox = new JComboBox(deflateLevels);
    deflateComboBox.setEnabled(deflateCheckBox.isSelected());
    deflateComboBox.setSelectedIndex(8);
    deflateBox.add(deflateComboBox);


    add(deflateBox);
  }


  /**
   *  The main program for the TIFFEncodeParamBox class
   *
   * @param  args  The command line arguments
   */
  public static void main(String[] args) {
    TIFFEncodeParamBox box = new TIFFEncodeParamBox();

    JFrame frame = new JFrame("TIFFEncodeParamBox");
    frame.addWindowListener(
      new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      });
    frame.getContentPane().add("Center", box);
    frame.pack();
    frame.setVisible(true);
  }
}
