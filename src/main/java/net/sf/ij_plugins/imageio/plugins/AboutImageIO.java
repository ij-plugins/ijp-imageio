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
package net.sf.ij_plugins.imageio.plugins;

import ij.IJ;
import ij.plugin.PlugIn;

/**
 * @author Jarek Sacha
 */
public class AboutImageIO implements PlugIn {

    private static final String TITLE = "About Image IO Plugin Bundle";
    private static final String MESSAGE = "<html>" +
            "<p>" +
            "The IJP-ImageIO plugins add to ImageJ support for additional image " +
            "file formats and their variants, including BMP, PCX, PNG, PNM, JPEG, " +
            "JPEG 2000, TIFF. Added TIFF support provides for handling of compressed, tiled, " +
            "1bit, 16bit, and 32bit images." +
            "</p>" +
            "<p>" +
            "For more detailed information see IJP-ImageIO home page at:\n" +
            "<a href=\"http://ij-plugins.sf.net/plugins/imageio\">http://ij-plugins.sf.net/plugins/imageio</a>" +
            "</p>" +
            "<br>" +
            "<br>" +
            "<br>" +
            "</html>";

    public void run(final String string) {
        IJ.showMessage(TITLE, MESSAGE);
    }

}
