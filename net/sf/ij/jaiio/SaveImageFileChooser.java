/***
 * Image/J Plugins
 * Copyright (C) 2002,2003 Jarek Sacha
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import non_com.media.jai.codec.ImageCodec;
import non_com.media.jai.codec.ImageEncoder;

/**
 *  Extension of JFileChooser with ability to return pages selected in
 *  multi-image files (e.g. TIFF).
 *
 * @author     Jarek Sacha
 * @created    June 16, 2002
 * @version    $Revision: 1.2 $
 */

public class SaveImageFileChooser
     extends JFileChooser
     implements PropertyChangeListener {

  private String selectedFilePath = null;


  /**  Constructor for the ImageFileChooser object */
  public SaveImageFileChooser(File currentDirectory) {
    super(currentDirectory);
//    this.setAccessory(previewer);
//    this.addPropertyChangeListener(this);

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
      }
      catch (Throwable t) {
        // Ignore ImageEncoders that cannot be instantiated
      }
    }

    JAIFileFilter defaultFilter = null;
    for (Iterator i = codecSet.iterator(); i.hasNext(); ) {
      try {
        String cadecName = (String) i.next();
        JAIFileFilter jaiFileFilter = new JAIFileFilter(cadecName);
        addChoosableFileFilter(jaiFileFilter);
        if (cadecName.toUpperCase().indexOf("TIFF") > -1) {
          defaultFilter = jaiFileFilter;
        }
      }
      catch (Throwable t) {
        t.printStackTrace();
      }
    }

    // Set selected filter
    if (defaultFilter != null) {
      setFileFilter(defaultFilter);
    }

    this.setMultiSelectionEnabled(false);
    this.setDialogType(JFileChooser.SAVE_DIALOG);

    this.validate();
  }


  /**
   *  Description of the Method
   *
   * @param  evt  Description of the Parameter
   */
  public void propertyChange(PropertyChangeEvent evt) {
    System.out.println("Property change : " + evt.getPropertyName());
    System.out.println("Property soource: " + evt.getSource());
    if (evt.getPropertyName().equals(
        JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
      FileFilter fileFilter = this.getFileFilter();
      System.out.println("New file filter: " + fileFilter);
      if (fileFilter instanceof JAIFileFilter) {
        JAIFileFilter jaiFileFilter = (JAIFileFilter) fileFilter;
        System.out.println("Changin file extension to: "
            + jaiFileFilter.getCodecName());
        if (selectedFilePath != null) {
          File selectedFile = new File(selectedFilePath + "."
              + jaiFileFilter.getCodecName().toLowerCase());
          setSelectedFile(selectedFile);
          this.invalidate();
          this.repaint();
        }
      }
    }
    else if (evt.getPropertyName().equals(
        JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
      File selectedFile = getSelectedFile();
      if (selectedFile != null) {
        selectedFilePath = selectedFile.getAbsolutePath();
        int lastSeparator = selectedFilePath.lastIndexOf(File.separator);
        int lastDot = selectedFilePath.lastIndexOf(".");
        if (lastDot > lastSeparator) {
          selectedFilePath = selectedFilePath.substring(0, lastDot);
        }
      }
      System.out.println("Selected file: " + selectedFilePath);
      this.invalidate();
      this.repaint();
    }
  }
}
