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

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class IJImageIOInfoPlugin implements PlugIn {
    public void run(String arg) {
        String message =
                "--------------------------------------------\n"
                + serviceProviderInfo(ImageReaderSpi.class, false)
                + "--------------------------------------------\n"
                + serviceProviderInfo(ImageWriterSpi.class, false)
                + "--------------------------------------------\n";

        //        ClassLoader loader = new ij.io.PluginClassLoader(Menus.getPlugInsPath());
        //        Iterator providers = IIORegistry.lookupProviders(ImageReaderSpi.class, loader);
        //        int count = 0;
        //        while (providers.hasNext()) {
        //            Object o = providers.next();
        //            message += o.toString() + "\n";
        //            ++count;
        //        }
        //        message += count;

        IJ.showMessage("ImageIO readers & writers", message);
    }

    public String readerInfo() {
        StringBuffer buf = new StringBuffer();
        buf.append("ImageIO readers (format:class):\n");
        final String[] readerMIMETypes = ImageIO.getReaderMIMETypes();

        for (int i = 0; i < readerMIMETypes.length; i++) {
            String readerMIMEType = readerMIMETypes[i];
            Iterator iterator = ImageIO.getImageReadersByMIMEType(readerMIMEType);
            while (iterator.hasNext()) {
                ImageReader reader = (ImageReader) iterator.next();
                try {
                    buf.append("  ").append(reader.getFormatName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                buf.append(": ").append(reader.getClass().getName()).append("\n");
            }
        }

        return buf.toString();
    }

    public String writersInfo() {
        StringBuffer buf = new StringBuffer();
        buf.append("ImageIO writers (format:class):\n");
        final String[] writerMIMETypes = ImageIO.getWriterMIMETypes();

        for (int i = 0; i < writerMIMETypes.length; i++) {
            String writerMIMEType = writerMIMETypes[i];
            Iterator iterator = ImageIO.getImageWritersByMIMEType(writerMIMEType);
            while (iterator.hasNext()) {
                ImageWriter writer = (ImageWriter) iterator.next();
                buf.append("  ").append(writerMIMEType);
                buf.append(": ").append(writer.getClass().getName()).append("\n");
            }
        }

        return buf.toString();
    }

    public static String serviceProviderInfo(Class category, boolean useOrdering) {
        final Iterator categories = IIORegistry.getDefaultInstance().getServiceProviders(category, useOrdering);
        final StringBuffer buf = new StringBuffer();
        while (categories.hasNext()) {
            Object o = categories.next();
            IIOServiceProvider iioServiceProvider = (IIOServiceProvider) o;
            buf.append(iioServiceProvider.getDescription(null));
            buf.append(" : ");
            buf.append(o.getClass().getName());
            buf.append("\n");
        }

        return buf.toString();
    }

}
