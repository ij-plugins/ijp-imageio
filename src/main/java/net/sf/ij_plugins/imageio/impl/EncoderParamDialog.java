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
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

/**
 */
public class EncoderParamDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private boolean accepted;
    private JPanel buttonPanel = new JPanel();
    private JButton okButton = new JButton();
    private JButton cancelButton = new JButton();
    private TIFFEncodeParamPanel tiffEncodeParamPanel;

    public EncoderParamDialog(final boolean useOneBitCompression) {
        this.setModal(true);
        tiffEncodeParamPanel = new TIFFEncodeParamPanel(useOneBitCompression);

        try {
            jbInit();
            pack();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        okButton.setText("OK");
        okButton.addActionListener(new EncoderParamDialog_okButton_actionAdapter(this));
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new EncoderParamDialog_cancelButton_actionAdapter(this));
        this.setTitle("ImageIO Save Options");
        this.addWindowListener(new EncoderParamDialog_this_windowAdapter(this));
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.add(okButton, null);
        buttonPanel.add(cancelButton, null);
        this.getContentPane().add(tiffEncodeParamPanel, BorderLayout.CENTER);
    }

    private void okButton_actionPerformed() {
        accepted = true;
        setVisible(false);
    }

    private void cancelButton_actionPerformed() {
        accepted = false;
        setVisible(false);
    }

    public static void main(final String[] args) {
        final EncoderParamDialog dialog = new EncoderParamDialog(true);
        dialog.setVisible(true);
        System.out.println("Accepted = " + dialog.isAccepted());
        System.exit(0);
    }

    private void this_windowOpened() {
        // Assume that dialog will be cancelled
        accepted = false;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public ImageWriteParam getImageWriteParam(final boolean binary) throws IJImageIOException {
        return tiffEncodeParamPanel.getImageWriteParam(binary);
    }

    private static class EncoderParamDialog_cancelButton_actionAdapter implements java.awt.event.ActionListener {
        EncoderParamDialog adaptee;

        EncoderParamDialog_cancelButton_actionAdapter(final EncoderParamDialog adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(final ActionEvent e) {
            adaptee.cancelButton_actionPerformed();
        }
    }

    private static class EncoderParamDialog_okButton_actionAdapter implements java.awt.event.ActionListener {
        EncoderParamDialog adaptee;

        EncoderParamDialog_okButton_actionAdapter(final EncoderParamDialog adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(final ActionEvent e) {
            adaptee.okButton_actionPerformed();
        }
    }

    private static class EncoderParamDialog_this_windowAdapter extends java.awt.event.WindowAdapter {
        EncoderParamDialog adaptee;

        EncoderParamDialog_this_windowAdapter(final EncoderParamDialog adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void windowOpened(final WindowEvent e) {
            adaptee.this_windowOpened();
        }
    }
}

