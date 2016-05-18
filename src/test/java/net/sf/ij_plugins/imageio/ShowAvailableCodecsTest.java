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
package net.sf.ij_plugins.imageio;

import org.junit.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import java.util.*;

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
            final Iterator<ImageReader> iterator = ImageIO.getImageReadersByMIMEType(readerMIMEType);
            println("Reader MIME Type: " + readerMIMEType);
            while (iterator.hasNext()) {
                ImageReader reader = iterator.next();
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
    public void testWriterServiceProviders() throws Exception {

        List<ImageWriterSpi> spis = IJImageOUtils.getImageWriterSpis();

        println("Categories: ");
        for (ImageWriterSpi spi : spis) {
            println("  " + Arrays.toString(spi.getFileSuffixes()) + " : " + spi.getDescription(null));
        }
    }


    @Test
    public void testWriterServiceProvidersInfo() throws Exception {

        List<ImageWriterSpi> spis = IJImageOUtils.getImageWriterSpis();

        for (ImageWriterSpi spi : spis) {
            ImageWriter writer = spi.createWriterInstance();
            println("  " + Arrays.toString(spi.getFileSuffixes()) + " : " + spi.getDescription(null));
            println("    canWriteSequence   : " + writer.canWriteSequence());
            ImageWriteParam param = writer.getDefaultWriteParam();
            println("    canWriteCompressed : " + param.canWriteCompressed());
            if (param.canWriteCompressed()) {
                String[] types = param.getCompressionTypes();
                println("      CompressionTypes               : " + Arrays.toString(types));
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                for (String type : types) {
                    param.setCompressionType(type);
                    println("        CompressionType   : " + type);
                    println("        CompressionQuality: " + param.getCompressionQuality());
                    if (param.getCompressionQualityDescriptions() != null) {
                        println("          CompressionQualityDescriptions : " + Arrays.toString(param.getCompressionQualityDescriptions()));
                        println("          CompressionQualityValues       : " + Arrays.toString(param.getCompressionQualityValues()));
                    }
                }
            }
            println("    canWriteProgressive: " + param.canWriteProgressive());
        }
    }


    @Test
    public void testGroupingWriters() {
        String[] formats = ImageIO.getWriterFormatNames();
        HashMap<ImageWriter, List<String>> writerMap = new HashMap<>();
        for (String format : formats) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
            while (writers.hasNext()) {
                ImageWriter writer = writers.next();
                if (writerMap.containsKey(writer)) {
                    List<String> names = writerMap.get(writer);
                    names.add(format);
                } else {
                    List<String> l = new ArrayList<>();
                    l.add(format);
                    writerMap.put(writer, l);
                }
            }
        }

        // Print
        for (ImageWriter writer : writerMap.keySet()) {
            List<String> l = writerMap.get(writer);
            System.out.println("Writer: " + l.size() + ": " + writer);
        }
    }


    private void printStrings(final String message, final String[] strings) {
        println(message);
        for (final String string : strings) {
            println("    " + string);
        }
    }
}