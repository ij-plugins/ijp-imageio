/*
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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.*;
import java.io.*;
import javax.swing.*;
import net.sf.ij.swing.IconCanvas;

/**
 * @author     Jarek Sacha
 * @created    January 9, 2001
 * @version
 */
public class JAIFilePreviewer extends JPanel
     implements PropertyChangeListener {

//  final static String FILE_SIZE_PREFIX = "File Size: ";
  final static String FILE_SIZE_PREFIX = "";
  final static long SIZE_KB = 1024;
  final static long SIZE_MB = SIZE_KB * 1024;
  final static long SIZE_GB = SIZE_MB * 1024;

  /**  Description of the Field */
  protected File file;
  /**  Description of the Field */
  protected int iconSizeX = 200;
  /**  Description of the Field */
  protected int iconSizeY = 150;

  private JPanel infoPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel fileSizeLabel = new JLabel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JLabel ImageIconLabel = new JLabel();


  /**  Constructor for the FilePreviewer object */
  public JAIFilePreviewer() {
    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   *  Creates new FilePreviewer
   *
   * @param  fc  Description of Parameter
   */
  public JAIFilePreviewer(JFileChooser fc) {
    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
//    setSize(new Dimension(xSize + 10, ySize + 10));
//    setPreferredSize(new Dimension(xSize + 10, ySize + 10));
    fc.addPropertyChangeListener(this);
  }


  /**
   * @param  e
   */
  public void propertyChange(PropertyChangeEvent e) {
    String prop = e.getPropertyName();
    if (prop == JFileChooser.SELECTED_FILE_CHANGED_PROPERTY) {
      file = (File) e.getNewValue();
      if (isShowing()) {
        loadImage();
        repaint();
      }
    }
  }


  private String getFileSizeString(long fileSize) {
    String fileSizeString = null;
    if (fileSize < SIZE_KB) {
      fileSizeString = FILE_SIZE_PREFIX + fileSize;
    }
    else if (fileSize < SIZE_MB) {
      fileSizeString = FILE_SIZE_PREFIX +
          (int) ((double) fileSize / SIZE_KB + 0.5) + "KB";
    }
    else if (fileSize < SIZE_GB) {
      fileSizeString = FILE_SIZE_PREFIX +
          (int) ((double) fileSize / SIZE_MB + 0.5) + "MB";
    }
    else {
      fileSizeString = FILE_SIZE_PREFIX +
          (int) ((double) fileSize / SIZE_GB + 0.5) + "GB";
    }

    return fileSizeString;
  }


  /**
   *  Load first image in the file.
   *
   * @return    image info.
   */
  private JAIReader.ImageInfo loadImage() {
    if (file == null || file.isDirectory()) {
      ImageIconLabel.setIcon(null);
//      iconCanvas1.setImageIcon(null);
      fileSizeLabel.setText(" ");
      return null;
    }

    try {

      JAIReader.ImageInfo imageInfo = JAIReader.readFirstImageAndInfo(file);
      Image image = imageInfo.previewImage;

      // Set image size label
      StringBuffer label = new StringBuffer(getFileSizeString(file.length()));
      if (image != null) {
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        if (w > 0 && h > 0) {
          label.append("  [" + w + "x" + h);
          if (imageInfo.numberOfPages > 1) {
            label.append("x" + imageInfo.numberOfPages + "]");
          }
          else {
            label.append("]");
          }
        }
      }
      fileSizeLabel.setText(label.toString());

      int xSizeBuffered = image.getWidth(null);
      int ySizeBuffered = image.getHeight(null);
      if (xSizeBuffered > iconSizeX || ySizeBuffered > iconSizeY) {
        // Replace image by its scaled version
        double scaleX = (double) iconSizeX / xSizeBuffered;
        double scaleY = (double) iconSizeY / ySizeBuffered;
        Image scaledImage = null;
        if (scaleX < scaleY) {
          image = image.getScaledInstance(iconSizeX, -1, Image.SCALE_DEFAULT);
        }
        else {
          image = image.getScaledInstance(-1, iconSizeY, Image.SCALE_DEFAULT);
        }
      }

      ImageIcon imageIcon = new ImageIcon(image);
      ImageIconLabel.setIcon(imageIcon);

      return imageInfo;
    }
    catch (Throwable t) {
      ImageIconLabel.setIcon(null);
      return null;
    }
  }


  private void jbInit() throws Exception {
    this.setLayout(gridBagLayout1);
    fileSizeLabel.setHorizontalAlignment(SwingConstants.CENTER);
    fileSizeLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    fileSizeLabel.setText(" ");
    infoPanel.setLayout(borderLayout1);
    ImageIconLabel.setPreferredSize(new Dimension(200, 150));
    ImageIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
    this.add(infoPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    infoPanel.add(fileSizeLabel, BorderLayout.CENTER);
    this.add(ImageIconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
  }
}

