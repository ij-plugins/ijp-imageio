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
package net.sf.ij.swing;

import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 * @author     Jarek Sacha
 * @created    January 9, 2001
 * @version
 */
public class IJImageFilePreviewer extends JComponent
     implements PropertyChangeListener {

  /*
   *  PROTECTED VARIABLES
   */
  /**
   *  Description of the Field
   */
  protected ImageIcon thumbnail;
  /**
   *  Description of the Field
   */
  protected File file;
  /**
   *  Description of the Field
   */
  protected int xSize = 200;
  /**
   *  Description of the Field
   */
  protected int ySize = 150;


  /**
   *  Constructor for the FilePreviewer object
   */
  public IJImageFilePreviewer() {
  }


  /**
   *  Creates new FilePreviewer
   *
   * @param  fc  Description of Parameter
   */
  public IJImageFilePreviewer(JFileChooser fc) {
    setSize(new Dimension(xSize + 10, ySize + 10));
    setPreferredSize(new Dimension(xSize + 10, ySize + 10));
    fc.addPropertyChangeListener(this);
  }


  /**
   */
  public void loadImage() {
    if (file == null) {
      return;
    }

    Opener o = new Opener();
    ImagePlus im = o.openImage(file.getParent() + File.separator, file.getName());
    if (im == null) {
      return;
    }

    if (im.getWidth() > xSize) {
      ImageIcon tmpIcon = new ImageIcon(im.getImage());
      thumbnail = new ImageIcon(
          tmpIcon.getImage().getScaledInstance(xSize, -1, Image.SCALE_DEFAULT));
    }
    else {
      thumbnail = new ImageIcon(im.getImage());
    }
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


  /**
   * @param  g
   */
  public void paint(Graphics g) {
    if (thumbnail == null) {
//      super.paint(g);
//      loadImage();
    }

    if (thumbnail != null) {
      int x = getWidth() / 2 - thumbnail.getIconWidth() / 2;
      int y = getHeight() / 2 - thumbnail.getIconHeight() / 2;

      if (y < 0) {
        y = 0;
      }
      if (x < 5) {
        x = 5;
      }

      thumbnail.paintIcon(this, g, x, y);
    }
  }
}

