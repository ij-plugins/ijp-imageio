/***
 * Image/J Plugins
 * Copyright (C) 2002-2005 Jarek Sacha
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
package net.sf.ij_plugins.imageio;

import ij.IJ;
import ij.plugin.PlugIn;
import sun.misc.Service;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.IIOServiceProvider;
import java.util.Iterator;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class ImageIOScanPlugin implements PlugIn {
    public void run(String arg) {
        final ClassLoader classLoader = this.getClass().getClassLoader();

        IJ.log("Scanning for ImageIO plugin codecs");
        IJ.showStatus("Scanning for ImageIO plugin codecs ...");
        ImageIO.scanForPlugins();
        IJ.showStatus("");

        // Rescann for plugins
        IJ.log("Additional scanning for ImageIO plugin codecs");
        IJ.log("  Using class loader: " + classLoader.getClass().getName());
        final Iterator categories = IIORegistry.getDefaultInstance().getCategories();
        while (categories.hasNext()) {
            final Class c = (Class) categories.next();
            IJ.log("  Scanning category: " + c.getName());
            final Iterator riter = Service.providers(c, classLoader);
            if (riter.hasNext()) {
                while (riter.hasNext()) {
                    final IIOServiceProvider r = (IIOServiceProvider) riter.next();
                    IJ.log("    Registering service provider: " + r.getClass().getName());
                    IIORegistry.getDefaultInstance().registerServiceProvider(r);
                }
            } else {
                IJ.log("    No additional service providers.");
            }
        }

    }
}
