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

import ij.IJ;
import ij.plugin.PlugIn;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import java.util.Iterator;

/**
 * Displays information about available javax.imageio image reader and image writer service
 * providers.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.3 $
 */
public class ImageIOInfoPlugin implements PlugIn {
    public void run(final String arg) {
        String message =
                "--------------------------------------------\n"
                        + serviceProviderInfo(ImageReaderSpi.class, false)
                        + "--------------------------------------------\n"
                        + serviceProviderInfo(ImageWriterSpi.class, false)
                        + "--------------------------------------------\n";

        message += "Reader format names: ";
        final String[] readers = ImageIO.getReaderFormatNames();
        for (final String reader : readers) {
            message += reader + ", ";
        }
        message += "\n";

        message += "Writer format names: ";
        final String[] writers = ImageIO.getWriterFormatNames();
        for (final String writer : writers) {
            message += writer + ", ";
        }


        IJ.showMessage("ImageIO readers & writers", message);
    }


    private static String serviceProviderInfo(final Class category, final boolean useOrdering) {
        final Iterator categories = IIORegistry.getDefaultInstance().getServiceProviders(category, useOrdering);
        final StringBuilder buf = new StringBuilder();
        while (categories.hasNext()) {
            final Object o = categories.next();
            final IIOServiceProvider iioServiceProvider = (IIOServiceProvider) o;
            buf.append(iioServiceProvider.getDescription(null));
            buf.append(" v.");
            buf.append(iioServiceProvider.getVersion());
            buf.append(" - ");
            buf.append(iioServiceProvider.getVendorName());
            buf.append(" : ");
            buf.append(o.getClass().getName());
            buf.append("\n");
        }

        return buf.toString();
    }

}
