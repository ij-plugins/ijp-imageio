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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.*;
import java.io.*;
import javax.swing.*;

import net.sf.ij.imageio.JAIReader;

/**
 *  Simple component to display ImageIcon.
 *
 * @author     Jarek Sacha
 * @created    February 10, 2002
 * @version    $Revision: 1.1 $
 */
public class IconCanvas extends JComponent {

  /**
   *  ImageIcon that is displayed by this component.
   */
  private ImageIcon imageIcon;


  /**
   *  Constructor for the FilePreviewer object
   */
  public IconCanvas() {
  }


  /**
   *  Constructor for the FilePreviewer object
   *
   * @param  imageIcon  Description of Parameter
   */
  public IconCanvas(ImageIcon imageIcon) {
    this.imageIcon = imageIcon;
  }


  /**
   *  Sets the ImageIcon attribute of the IconCanvas object
   *
   * @param  imageIcon  The new ImageIcon value
   */
  public void setImageIcon(ImageIcon imageIcon) {
    this.imageIcon = imageIcon;
  }


  /**
   *  Gets the ImageIcon attribute of the IconCanvas object
   *
   * @return    The ImageIcon value
   */
  public ImageIcon getImageIcon() {
    return imageIcon;
  }


  /**
   *  This method is invoked by Swing to draw components.
   *
   * @param  g  Graphics.
   */
  public void paintComponent(Graphics g) {
    if (imageIcon != null) {
      int x = getWidth() / 2 - imageIcon.getIconWidth() / 2;
      int y = getHeight() / 2 - imageIcon.getIconHeight() / 2;

      if (y < 0) {
        y = 0;
      }
      if (x < 0) {
        x = 0;
      }

      imageIcon.paintIcon(this, g, x, y);
    }
  }
}

