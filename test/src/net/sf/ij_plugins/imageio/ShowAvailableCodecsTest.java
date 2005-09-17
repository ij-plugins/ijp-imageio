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
package net.sf.ij_plugins.imageio;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.spi.ImageWriterSpi;
import java.util.Iterator;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class ShowAvailableCodecsTest extends TestCase {
    public ShowAvailableCodecsTest(String test) {
        super(test);
    }

    /**
     * The fixture set up called before every test method.
     */
    protected void setUp() throws Exception {
    }

    /**
     * The fixture clean up called after every test method.
     */
    protected void tearDown() throws Exception {
    }

    public void testSomething() throws Exception {
        String[] readerFormatNames = ImageIO.getReaderFormatNames();
        printStrings("readerFormatNames", readerFormatNames);

        String[] readerMIMETypes = ImageIO.getReaderMIMETypes();
        printStrings("readerMIMETypes", readerMIMETypes);

        for (int i = 0; i < readerMIMETypes.length; i++) {
            String readerMIMEType = readerMIMETypes[i];
            Iterator iterator = ImageIO.getImageReadersByMIMEType(readerMIMEType);
            System.out.println("Reader MIME Type: " + readerMIMEType);
            while (iterator.hasNext()) {
                ImageReader reader = (ImageReader) iterator.next();
                System.out.println("  format: " + reader.getFormatName());
                System.out.println("  class:  " + reader.getClass().getName());
            }
        }

        final Iterator categories = IIORegistry.getDefaultInstance().getServiceProviders(ImageWriterSpi.class, true);
        System.out.println("Categories: ");
        while (categories.hasNext()) {
            Object o = categories.next();
            System.out.println("  " + o);
            IIOServiceProvider iioServiceProvider = (IIOServiceProvider) o;
            System.out.println("  " + iioServiceProvider + " : " + iioServiceProvider.getDescription(null));

        }

        String[] writerFormatNames = ImageIO.getWriterFormatNames();
        printStrings("writerFormatNames", writerFormatNames);

        String[] writerMIMETypes = ImageIO.getWriterMIMETypes();
        printStrings("writerMIMETypes", writerMIMETypes);


    }

    private void printStrings(String message, String[] strings) {
        System.out.println(message);
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            System.out.println("    " + string);
        }
    }
}