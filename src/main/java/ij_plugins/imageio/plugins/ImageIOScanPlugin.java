/*
 *  IJ-Plugins ImageIO
 *  Copyright (C) 2002-2021 Jarek Sacha
 *  Author's email: jpsacha at gmail dot com
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
 *  Latest release available at https://github.com/ij-plugins/ijp-imageio/
 */
package ij_plugins.imageio.plugins;

import ij.IJ;
import ij.plugin.PlugIn;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.IIOServiceProvider;
import java.util.Iterator;


/**
 * @author Jarek Sacha
 */
public class ImageIOScanPlugin implements PlugIn {

    public void run(final String arg) {
        final ClassLoader classLoader = this.getClass().getClassLoader();

        IJ.log("Scanning for ImageIO plugins codecs");
        IJ.showStatus("Scanning for ImageIO plugins codecs ...");
        ImageIO.scanForPlugins();
        IJ.showStatus("");

        // Rescan for plugins
        IJ.log("Additional scanning for ImageIO plugins codecs");
        IJ.log("  Using class loader: " + classLoader.getClass().getName());
        final Iterator<Class<?>> categories = IIORegistry.getDefaultInstance().getCategories();
        while (categories.hasNext()) {
            final Class<?> c = categories.next();
            IJ.log("  Scanning category: " + c.getName());
            final Iterator<?> iterator = java.util.ServiceLoader.load(c, classLoader).iterator();
            if (iterator.hasNext()) {
                while (iterator.hasNext()) {
                    final IIOServiceProvider r = (IIOServiceProvider) iterator.next();
                    IJ.log("    Registering service provider: " + r.getClass().getName());
                    IIORegistry.getDefaultInstance().registerServiceProvider(r);
                }
            } else {
                IJ.log("    No additional service providers.");
            }
        }

    }
}
