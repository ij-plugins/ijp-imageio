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
package net.sf.ij.plugin;

import ij.IJ;
import ij.Menus;
import ij.plugin.PlugIn;

import java.io.File;
import java.util.ArrayList;

/**
 * Plugin for uninstalling JAI Image IO plugin bundle version 1.0.5 or ealier.
 * 
 * @author Jarek Sacha
 * @version $Revision: 1.2 $
 */
public class RemoveOldPlugins implements PlugIn {
    private ArrayList foundComponents;

    private static final String CAPTION = "Uninstall Obsolete JAI ImageIO Plugins";
    private static final String CANCELLED_MESSAGE
            = "Operation canceled, no changes made to installation.";

    private final static String[] ROOT_FILES = {
        "Readme - JAI Image IO.txt",
        "Changes - JAI Image IO.txt",
        "ij-jai-imageio.jar",
        "JAI Image IO",
    };
    private final static String SUBDIR = "JAI Image IO";
    private final static String[] SUBDIR_FILES = {
        "JAI_Reader.class",
        "JAI_Reader_with_Preview.class",
        "JAI_Writer.class",
        "JarClassLoader.class",
        "JarPluginProxy.class"
    };


    public void run(String string) {
        // Notify about intent to uninstall obsolete files, give
        // option to cancel.
        boolean ok = IJ.showMessageWithCancel(CAPTION,
                "This plugin will attempt to find and uninstall "
                + "obsolete JAI Image IO plugins.\n" +
                "To proceed with uninstall press OK.");

        if (!ok) {
            IJ.showMessage(CAPTION, CANCELLED_MESSAGE);
            return;
        }

        // Search for obsolete installation
        IJ.showStatus("Searching for obsolete components.");

        foundComponents = new ArrayList();
        String pluginsDir = Menus.getPlugInsPath();

        // Search for files in plugins folder
        for (int i = 0; i < ROOT_FILES.length; i++) {
            lookFor(pluginsDir, ROOT_FILES[i]);
        }

        // Search for files in plugins folder sundirectory
        File subdir = new File(pluginsDir, SUBDIR);
        if (subdir.exists() && subdir.isDirectory()) {
            for (int i = 0; i < SUBDIR_FILES.length; i++) {
                lookFor(subdir.getPath(), SUBDIR_FILES[i]);

            }
        }

        // Check if any files found.
        if (foundComponents.size() == 0) {
            IJ.showMessage(CAPTION, "Obsolete installation not found.");
            return;
        }

        // Prepare message listing files to be removed
        StringBuffer messageBuffer = new StringBuffer();
        messageBuffer.append("Following files will be uninstalled: \n");
        for (int i = 0; i < foundComponents.size(); i++) {
            File file = (File) foundComponents.get(i);
            messageBuffer.append(file.getAbsolutePath() + "\n");
        }

        // Confirm removal
        ok = IJ.showMessageWithCancel(CAPTION, messageBuffer.toString());
        if (!ok) {
            IJ.showMessage(CAPTION, CANCELLED_MESSAGE);
            return;
        }

        // Remove files
        StringBuffer errorBuffer = new StringBuffer();
        for (int i = 0; i < foundComponents.size(); i++) {
            File file = (File) foundComponents.get(i);
            if (!file.delete()) {
                errorBuffer.append(file.getAbsolutePath() + "\n");
            }
        }

        if (subdir.exists() || subdir.isDirectory()) {
            String[] list = subdir.list();
            if (list != null && list.length == 0) {
                if (!subdir.delete()) {
                    errorBuffer.append(subdir.getAbsolutePath() + "\n");
                }
            }
        }

        if (errorBuffer.length() > 0) {
            IJ.showMessage(CAPTION, "Unable to uninstall following files:\n"
                    + errorBuffer.toString()
                    + "Restart ImageJ to complete uninstall.");
        } else {
            IJ.showMessage(CAPTION, "Uninstall completed successfully.\n"
                    + "Please restart ImageJ.");
        }
    }


    /**
     * Look for given file in directory <code>dir</code>. If found, add it to <code>foundComponents</code> list.
     * Directories are ignored to prevent accidental removal.
     * 
     * @param dir      directory to search.
     * @param fileName file to look for.
     */
    private void lookFor(String dir, String fileName) {
        File file = new File(dir, fileName);
        if (file.exists() && !file.isDirectory()) {
            foundComponents.add(file);
        }
    }

}