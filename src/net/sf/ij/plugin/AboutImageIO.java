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

import ij.plugin.PlugIn;
import ij.IJ;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.3 $
 */
public class AboutImageIO implements PlugIn {
    
    private static final String TITLE = "About Image IO Plugin Bundle";
    private static final String MESAGE =
            "ij-ImageIO plugins add to ImageJ support for additional image\n" +
            "file formats and their variants, including BMP, PNG, PNM, JPEG,\n" +
            "TIFF. Added support provides for handling of compressed, tiled,\n" +
            "1bit, 16bit, and 32bit images.\n" +
            "For more detailed informations see ij-ImageIO home page at:\n" +
            "http://ij-plugins.sf.net/plugins/imageio";

    public void run(String string) {
        try {
        HelpPanel.showHelpWindow(false);
        }
        catch(RuntimeException e) {
            String msg = MESAGE +"\n"+
                    "*****************************************************************\n"+
                    "Regular Image IO help failed to load content from HTML resource.\n"+
                    "This may be a problem with the installation of current version\n"+
                    "of ImageJ. Check Image IO home page (see below) for more details,\n"+
                    "look for section \"Known Issues\"\n" +
                    "________________________________________________________________\n"+
                    "Original error message:\n" + e;
            IJ.showMessage(TITLE, msg);
        }
    }

}
