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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

/**
 */
public class EncoderParamDialog extends JDialog {
    private boolean accepted = false;
    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton();
    JButton cancelButton = new JButton();
    JTabbedPane optionsTabbedPane = new JTabbedPane();
    TIFFEncodeParamPanel tiffEncodeParamPanel = new TIFFEncodeParamPanel();

    public EncoderParamDialog() {
        this.setModal(true);
        try {
            jbInit();
            pack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        okButton.setText("OK");
        okButton.addActionListener(new EncoderParamDialog_okButton_actionAdapter(this));
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new EncoderParamDialog_cancelButton_actionAdapter(this));
        this.setTitle("ImageIO Save Options");
        this.addWindowListener(new EncoderParamDialog_this_windowAdapter(this));
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.add(okButton, null);
        buttonPanel.add(cancelButton, null);
        this.getContentPane().add(optionsTabbedPane, BorderLayout.CENTER);
        optionsTabbedPane.add(tiffEncodeParamPanel, "TIFF");
    }

    void okButton_actionPerformed(ActionEvent e) {
        accepted = true;
        hide();
    }

    void cancelButton_actionPerformed(ActionEvent e) {
        accepted = false;
        hide();
    }

    public static void main(String[] args) {
        EncoderParamDialog dialog = new EncoderParamDialog();
        dialog.show();
        System.out.println("Accepted = " + dialog.isAccepted());
        System.exit(0);
    }

    void this_windowOpened(WindowEvent e) {
        // Assume that dialog will be cancelled
        accepted = false;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public ImageEncodeParam getImageEncodeParam(boolean binary) {
        return tiffEncodeParamPanel.getImageEncodeParam(binary);
    }

}

class EncoderParamDialog_okButton_actionAdapter implements java.awt.event.ActionListener {
    EncoderParamDialog adaptee;

    EncoderParamDialog_okButton_actionAdapter(EncoderParamDialog adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.okButton_actionPerformed(e);
    }
}

class EncoderParamDialog_cancelButton_actionAdapter implements java.awt.event.ActionListener {
    EncoderParamDialog adaptee;

    EncoderParamDialog_cancelButton_actionAdapter(EncoderParamDialog adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.cancelButton_actionPerformed(e);
    }
}

class EncoderParamDialog_this_windowAdapter extends java.awt.event.WindowAdapter {
    EncoderParamDialog adaptee;

    EncoderParamDialog_this_windowAdapter(EncoderParamDialog adaptee) {
        this.adaptee = adaptee;
    }

    public void windowOpened(WindowEvent e) {
        adaptee.this_windowOpened(e);
    }
}
