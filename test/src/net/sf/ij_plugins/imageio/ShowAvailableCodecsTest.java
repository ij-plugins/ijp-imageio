/*
 * Image/J Plugins
 * Copyright (C) 2002-2011 Jarek Sacha
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
package net.sf.ij_plugins.imageio;

import org.junit.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.spi.ImageWriterSpi;
import java.util.Iterator;

import static net.sf.ij_plugins.imageio.Console.println;

/**
 * @author Jarek Sacha
 */
public class ShowAvailableCodecsTest {


    @Test
    public void testReaders() throws Exception {
        final String[] readerFormatNames = ImageIO.getReaderFormatNames();
        printStrings("readerFormatNames", readerFormatNames);

        final String[] readerMIMETypes = ImageIO.getReaderMIMETypes();
        printStrings("readerMIMETypes", readerMIMETypes);

        for (String readerMIMEType : readerMIMETypes) {
            final Iterator iterator = ImageIO.getImageReadersByMIMEType(readerMIMEType);
            println("Reader MIME Type: " + readerMIMEType);
            while (iterator.hasNext()) {
                ImageReader reader = (ImageReader) iterator.next();
                println("  format: " + reader.getFormatName());
                println("  class:  " + reader.getClass().getName());
            }
        }
    }

    @Test
    public void testWriters() throws Exception {
        final String[] writerFormatNames = ImageIO.getWriterFormatNames();
        printStrings("writerFormatNames", writerFormatNames);

        final String[] writerMIMETypes = ImageIO.getWriterMIMETypes();
        printStrings("writerMIMETypes", writerMIMETypes);
    }


    @Test
    public void testServiceProviders() throws Exception {

        final Iterator categories = IIORegistry.getDefaultInstance().getServiceProviders(ImageWriterSpi.class, true);
        println("Categories: ");
        while (categories.hasNext()) {
            final Object o = categories.next();
            println("  " + o);
            IIOServiceProvider iioServiceProvider = (IIOServiceProvider) o;
            println("  " + iioServiceProvider + " : " + iioServiceProvider.getDescription(null));

        }
    }


    private void printStrings(final String message, final String[] strings) {
        println(message);
        for (final String string : strings) {
            println("    " + string);
        }
    }


}