/*
 * Image/J Plugins
 * Copyright (C) 2002-2016 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
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

package net.sf.ij_plugins.imageio.plugins;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author Jarek Sacha
 */
final public class SaveImageFileChooserDemo {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SaveImageFileChooser chooser = new SaveImageFileChooser(new File("."));
            int ok = chooser.showSaveDialog(null);
            if (ok == JFileChooser.APPROVE_OPTION) {
                FileFilter fileFilter = chooser.getFileFilter();
                ImageFileFilter imageFileFilter = (ImageFileFilter) fileFilter;
                System.out.println("SPI: " + imageFileFilter.getSPI());
            }
        });
    }
}
