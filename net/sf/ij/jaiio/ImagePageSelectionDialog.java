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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *  Dialog for selecting pages in multi-image files.
 *
 * @author     Jarek Sacha
 * @created    June 16, 2002
 * @version    $Revision: 1.2 $
 */

public class ImagePageSelectionDialog extends JDialog {

  private int numPages = 1;
  private int[] pageIndex = null;

  private JPanel jPanel1 = new JPanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JTextField pageIncrementTF = new JTextField();
  private JTextField lastPageTF = new JTextField();
  private JTextField firstPageTF = new JTextField();
  private JTextField numPagesTF = new JTextField();
  private JPanel panel1 = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel pageIncrementLabel = new JLabel();
  private JLabel lastPageLabel = new JLabel();
  private JLabel firstPageLabel = new JLabel();
  private JLabel jLabel1 = new JLabel();
  private JPanel jPanel2 = new JPanel();
  private JButton cancelButton = new JButton();
  private JButton okButton = new JButton();


  /**
   *  Constructor for the ImagePageSelectionDialog object
   *
   * @param  frame  Description of Parameter
   * @param  title  Description of Parameter
   * @param  modal  Description of Parameter
   */
  public ImagePageSelectionDialog(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    try {
      jbInit();
      pack();
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
      Rectangle r = getBounds();
      r.x = d.width / 2 - r.width / 2;
      r.y = d.height / 2 - r.height / 2;
      setBounds(r);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  /**  Constructor for the ImagePageSelectionDialog object */
  public ImagePageSelectionDialog() {
    this(null, "", true);
  }


  /**
   *  Set number of pages in the current image file. This method should be
   *  called before showing the dialog.
   *
   * @param  numPages  Number of pages in the current image file.
   */
  public void setNumPages(int numPages) {
    if (numPages < 1) {
      throw new RuntimeException("Number of pages cannot be less then 1 (got "
          + numPages + ").");
    }
    this.numPages = numPages;
  }


  /**
   *  Gets number of pages.
   *
   * @return    Number of pages.
   */
  public int getNumPages() {
    return numPages;
  }


  /**
   *  Returns array of selected page indexes. First image in a file has index 0.
   *
   * @return   Array of selected page indexes or null if selection was not made.
   */
  public int[] getPageIndex() {
    return pageIndex;
  }


  /*
   *
   */
  void jbInit() throws Exception {
    okButton.setText("OK");
    okButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          okButton_actionPerformed(e);
        }
      });
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          cancelButton_actionPerformed(e);
        }
      });
    jLabel1.setText("Number of Pages");
    firstPageLabel.setText("First Page");
    lastPageLabel.setText("Last Page");
    pageIncrementLabel.setText("Page Increment");
    panel1.setLayout(gridBagLayout1);
    numPagesTF.setHorizontalAlignment(SwingConstants.CENTER);
    numPagesTF.setColumns(5);
    numPagesTF.setText("1");
    numPagesTF.setEditable(false);
    firstPageTF.setText("1");
    firstPageTF.setColumns(5);
    firstPageTF.setHorizontalAlignment(SwingConstants.CENTER);
    lastPageTF.setText("1");
    lastPageTF.setColumns(5);
    lastPageTF.setHorizontalAlignment(SwingConstants.CENTER);
    pageIncrementTF.setText("1");
    pageIncrementTF.setColumns(5);
    pageIncrementTF.setHorizontalAlignment(SwingConstants.CENTER);
    jPanel1.setLayout(borderLayout1);
    this.addComponentListener(
      new java.awt.event.ComponentAdapter() {
        public void componentShown(ComponentEvent e) {
          this_componentShown(e);
        }
      });
    this.setTitle("Page Selection");
    this.getContentPane().add(jPanel1, BorderLayout.NORTH);
    jPanel1.add(panel1, BorderLayout.CENTER);
    panel1.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    panel1.add(numPagesTF, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    panel1.add(firstPageLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    panel1.add(firstPageTF, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    panel1.add(lastPageLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    panel1.add(lastPageTF, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    panel1.add(pageIncrementLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
        , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    panel1.add(pageIncrementTF, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    jPanel1.add(jPanel2, BorderLayout.SOUTH);
    jPanel2.add(okButton, null);
    jPanel2.add(cancelButton, null);
  }


  /*
   *
   */
  void okButton_actionPerformed(ActionEvent e) {
    int firstPage = 1;
    int lastPage = numPages;
    int pageIncrement = 1;
    try {
      firstPage = parseInt(firstPageTF.getText(),
          firstPageLabel.getText(), 1, numPages);
      lastPage = parseInt(lastPageTF.getText(),
          lastPageLabel.getText(), firstPage, numPages);
      pageIncrement = parseInt(pageIncrementTF.getText(),
          pageIncrementLabel.getText(), 1, numPages);
    }
    catch (Exception ex) {
      JOptionPane.showMessageDialog(this, ex.getMessage(), this.getTitle(),
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    int indexSize = 1 + (lastPage - firstPage) / pageIncrement;
    if (indexSize < 1) {
      pageIndex = null;
    }
    else {
      if (pageIndex == null || pageIndex.length != indexSize) {
        pageIndex = new int[indexSize];
      }
      for (int i = 0; i < indexSize; ++i) {
        pageIndex[i] = firstPage - 1 + i * pageIncrement;
      }
    }
    setVisible(false);
  }


  /*
   *
   */
  void cancelButton_actionPerformed(ActionEvent e) {
    pageIndex = null;
    setVisible(false);
  }


  /*
   *
   */
  void this_componentShown(ComponentEvent e) {
    numPagesTF.setText("" + numPages);
    firstPageTF.setText("1");
    lastPageTF.setText("" + numPages);
    pageIncrementTF.setText("1");
  }


  /*
   *
   */
  private int parseInt(String intString, String name, int min, int max)
       throws Exception {
    int r = 0;
    try {
      r = Integer.parseInt(intString);
    }
    catch (NumberFormatException ex) {
      throw new Exception("Error parsing " + name + ". Value of '"
          + intString + "' can not be parsed as integer");
    }
    if (r < min || r > max) {
      throw new Exception("Value of " + name + " cannot be smaller than " + min
          + " or larger than " + max + " (got " + r + ").");
    }

    return r;
  }
}
