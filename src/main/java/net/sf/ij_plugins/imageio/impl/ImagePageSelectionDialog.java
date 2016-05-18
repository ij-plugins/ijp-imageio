/*
 * Image/J Plugins
 * Copyright (C) 2002-2016 Jarek Sacha
 * Author's email: jpsacha at gmail.com
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

package net.sf.ij_plugins.imageio.impl;

import net.sf.ij_plugins.imageio.IJImageIOException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;

/**
 * Dialog for selecting pages/slices in multi-image files.
 *
 * @author Jarek Sacha
 */

class ImagePageSelectionDialog extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int numPages = 1;
    private int[] pageIndex = null;

    private final JPanel jPanel1 = new JPanel();
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final JTextField pageIncrementTF = new JTextField();
    private final JTextField lastPageTF = new JTextField();
    private final JTextField firstPageTF = new JTextField();
    private final JTextField numPagesTF = new JTextField();
    private final JPanel panel1 = new JPanel();
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel pageIncrementLabel = new JLabel();
    private final JLabel lastPageLabel = new JLabel();
    private final JLabel firstPageLabel = new JLabel();
    private final JLabel jLabel1 = new JLabel();
    private final JPanel jPanel2 = new JPanel();
    private final JButton cancelButton = new JButton();
    private final JButton okButton = new JButton();


    /**
     * Constructor for the ImagePageSelectionDialog object
     *
     * @param owner - the Frame from which the dialog is displayed
     * @param title - the String to display in the dialog's title bar
     * @param modal - specifies whether dialog blocks user input to other top-level windows when shown.
     *              If true, the modality type property is set to DEFAULT_MODALITY_TYPE otherwise the dialog is modeless
     */
    public ImagePageSelectionDialog(final Frame owner, final String title, final boolean modal) {
        super(owner, title, modal);
        try {
            initializeComponents();
            pack();
            final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            final Rectangle r = getBounds();
            r.x = d.width / 2 - r.width / 2;
            r.y = d.height / 2 - r.height / 2;
            setBounds(r);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Constructor for the ImagePageSelectionDialog object
     */
    public ImagePageSelectionDialog(final Frame owner) {
        this(owner, "", true);
    }


    /**
     * Set number of pages in the current image file. This method should be called before showing
     * the dialog.
     *
     * @param numPages Number of pages in the current image file.
     */
    public void setNumPages(final int numPages) {
        if (numPages < 1) {
            throw new RuntimeException("Number of pages cannot be less then 1 (got " + numPages + ").");
        }
        this.numPages = numPages;
    }


    /**
     * Gets number of pages.
     *
     * @return Number of pages.
     */
    public int getNumPages() {
        return numPages;
    }


    /**
     * Returns array of selected page indexes. First image in a file has index 0.
     *
     * @return Array of selected page indexes or null if selection was not made.
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


    /*
     *
     */
    void initializeComponents() {
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                okButtonAction();
            }
        });
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                cancelButtonAction();
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
        borderLayout1.setVgap(4);
        borderLayout1.setHgap(4);
        jPanel1.setLayout(borderLayout1);
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent e) {
                componentShownEvent();
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
        jPanel2.setLayout(new GridLayout(0, 2, 0, 0));
        jPanel2.add(okButton);
        jPanel2.add(cancelButton);
    }


    /*
     *
     */
    void okButtonAction() {
        int firstPage;
        int lastPage;
        int pageIncrement;
        try {
            firstPage = parseInt(firstPageTF.getText(),
                    firstPageLabel.getText(), 1, numPages);
            lastPage = parseInt(lastPageTF.getText(),
                    lastPageLabel.getText(), firstPage, numPages);
            pageIncrement = parseInt(pageIncrementTF.getText(),
                    pageIncrementLabel.getText(), 1, numPages);
        } catch (final IJImageIOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), this.getTitle(),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        final int indexSize = 1 + (lastPage - firstPage) / pageIncrement;
        if (indexSize < 1) {
            pageIndex = null;
        } else {
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
    void cancelButtonAction() {
        pageIndex = null;
        setVisible(false);
    }


    /*
     *
     */
    void componentShownEvent() {
        numPagesTF.setText("" + numPages);
        firstPageTF.setText("1");
        lastPageTF.setText("" + numPages);
        pageIncrementTF.setText("1");
    }


    /*
     *
     */
    private int parseInt(final String intString, final String name, final int min, final int max)
            throws IJImageIOException {
        int r;
        try {
            r = Integer.parseInt(intString);
        } catch (final NumberFormatException ex) {
            throw new IJImageIOException("Error parsing " + name + ". Value of '"
                    + intString + "' can not be parsed as integer");
        }
        if (r < min || r > max) {
            throw new IJImageIOException("Value of " + name + " cannot be smaller than " + min
                    + " or larger than " + max + " (got " + r + ").");
        }

        return r;
    }
}
