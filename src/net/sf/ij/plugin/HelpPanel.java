/*
 * Image/J Plugins
 * Copyright (C) 2002-2008 Jarek Sacha
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
 *
 */
package net.sf.ij.plugin;

import net.sf.ij.swing.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

/**
 * A panel displaying help for the Image I/O plugin bundle.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.5 $
 */
public final class HelpPanel extends JPanel {

    /**
     * Default constructor.
     *
     * @throws IOException If help content cannot be loaded.
     */
    public HelpPanel() throws IOException {
        URL helpURL = HelpPanel.class.getResource("/docs/index.html");
        if (helpURL == null) {
            throw new IOException("Couldn't find Image IO help file.");
        }
        System.out.println("Help file URL: " + helpURL);
//        InputStream is = HelpPanel.class.getResourceAsStream("/docs/index.html");
//        if (is == null) {
//            throw new IOException("Couldn't find Image I/O help file.");
//        }
//
//        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
//        HTMLDocument doc = new HTMLDocument();
//        doc.setBase(helpURL);
//        try {
//            htmlEditorKit.read(is, doc, 0);
//        } catch (BadLocationException e) {
//            throw new RuntimeException(e);
//        }

        JEditorPane editorPane = new JEditorPane();
//        editorPane.setEditorKit(htmlEditorKit);
//        editorPane.read(is, "Image IO Help file");
        editorPane.setPage(helpURL);
        editorPane.setEditable(false);
        // TODO: Enable following of hyperlinks after adding previous/next buttons for navigation.
//        editorPane.addHyperlinkListener(new HyperlinkListener() {
//            public void hyperlinkUpdate(HyperlinkEvent e) {
//                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//                    JEditorPane pane = (JEditorPane) e.getSource();
//                    if (e instanceof HTMLFrameHyperlinkEvent) {
//                        HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
//                        HTMLDocument doc = (HTMLDocument) pane.getDocument();
//                        doc.processHTMLFrameHyperlinkEvent(evt);
//                    } else {
//                        try {
//                            pane.setPage(e.getURL());
//                        } catch (Throwable t) {
//                            t.printStackTrace();
//                        }
//                    }
//                }
//            }
//        });

        //Put the editor pane in a scroll pane.
        JScrollPane editorScrollPane = new JScrollPane(editorPane);
        editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(new Dimension(600, 400));

        setLayout(new BorderLayout());
        add(editorScrollPane, BorderLayout.CENTER);
    }

    /**
     * Create and display help window.
     *
     * @param exitOnClose if <code>true</code> closing the help window will exit the application.
     */
    static void showHelpWindow(boolean exitOnClose) {
        // Create window to host help panel
        final JFrame frame = new JFrame("About Image IO plugins");

        // Load icon for close button
        String iconResourceName = "exit16.png";
        URL iconURL = HelpPanel.class.getResource(iconResourceName);
        ImageIcon icon = null;
        if (iconURL != null) {
            System.out.println("Button icon URL: " + iconURL);
            icon = new ImageIcon(iconURL);
        } else {
            System.out.println("Unable to locate resource for icon: " + iconResourceName);
        }

        JButton closeButton = new JButton("Close", icon);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        // Create help panel
        final HelpPanel aboutImageIO;
        try {
            aboutImageIO = new HelpPanel();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Image IO help window.", e);
        }

        frame.getContentPane().add(aboutImageIO);

        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);


        frame.setDefaultCloseOperation(exitOnClose ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);

        // Display help window, to ensure thread safety run it in the Event Dispatch Thread.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SwingUtils.centerOnScreen(frame, true);
                frame.setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        HelpPanel.showHelpWindow(true);
    }

}
