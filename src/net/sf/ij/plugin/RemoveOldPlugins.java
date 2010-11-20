/*
 * Image/J Plugins
 * Copyright (C) 2002-2010 Jarek Sacha
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
package net.sf.ij.plugin;

import ij.IJ;
import ij.Menus;
import ij.plugin.PlugIn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Plugin for uninstalling JAI Image IO plugin bundle version 1.0.5 or earlier.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.3 $
 * @deprecated
 */
public class RemoveOldPlugins implements PlugIn {
    private List<File> foundComponents;

    private static final String CAPTION = "Uninstall Obsolete JAI ImageIO Plugins";
    private static final String CANCELLED_MESSAGE
            = "Operation canceled, no changes made to installation.";

    private static final String[] ROOT_FILES = {
            "Readme - JAI Image IO.txt",
            "Changes - JAI Image IO.txt",
            "ij-jai-imageio.jar",
            "JAI Image IO",
    };
    private static final String SUBDIR = "JAI Image IO";
    private static final String[] SUBDIR_FILES = {
            "JAI_Reader.class",
            "JAI_Reader_with_Preview.class",
            "JAI_Writer.class",
            "JarClassLoader.class",
            "JarPluginProxy.class"
    };


    public void run(final String string) {
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

        foundComponents = new ArrayList<File>();
        final String pluginsDir = Menus.getPlugInsPath();

        // Search for files in plugins folder
        for (final String aROOT_FILES : ROOT_FILES) {
            lookFor(pluginsDir, aROOT_FILES);
        }

        // Search for files in plugins folder subdirectory
        final File subdir = new File(pluginsDir, SUBDIR);
        if (subdir.exists() && subdir.isDirectory()) {
            for (final String aSUBDIR_FILES : SUBDIR_FILES) {
                lookFor(subdir.getPath(), aSUBDIR_FILES);

            }
        }

        // Check if any files found.
        if (foundComponents.size() == 0) {
            IJ.showMessage(CAPTION, "Obsolete installation not found.");
            return;
        }

        // Prepare message listing files to be removed
        final StringBuffer messageBuffer = new StringBuffer();
        messageBuffer.append("Following files will be uninstalled: \n");
        for (final File foundComponent : foundComponents) {
            messageBuffer.append(foundComponent.getAbsolutePath()).append("\n");
        }

        // Confirm removal
        ok = IJ.showMessageWithCancel(CAPTION, messageBuffer.toString());
        if (!ok) {
            IJ.showMessage(CAPTION, CANCELLED_MESSAGE);
            return;
        }

        // Remove files
        final StringBuffer errorBuffer = new StringBuffer();
        for (final File file : foundComponents) {
            if (!file.delete()) {
                errorBuffer.append(file.getAbsolutePath()).append("\n");
            }
        }

        if (subdir.exists() || subdir.isDirectory()) {
            final String[] list = subdir.list();
            if (list != null && list.length == 0) {
                if (!subdir.delete()) {
                    errorBuffer.append(subdir.getAbsolutePath()).append("\n");
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
     * Look for given file in directory <code>dir</code>. If found, add it to
     * <code>foundComponents</code> list. Directories are ignored to prevent accidental removal.
     *
     * @param dir      directory to search.
     * @param fileName file to look for.
     */
    private void lookFor(final String dir, final String fileName) {
        final File file = new File(dir, fileName);
        if (file.exists() && !file.isDirectory()) {
            foundComponents.add(file);
        }
    }

}