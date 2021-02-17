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

package ij_plugins.imageio;

import org.junit.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * @author Jarek Sacha
 * @since Nov 3, 2010 8:49:06 PM
 */
public final class WriterCapabilityTest {

    @Test
    public void testPNG() throws Exception {
        final List<ImageWriter> writers = findWriters("PNG");
        for (final ImageWriter writer : writers) {
            Console.println("" + writer);
            Console.println(writersCapabilityDescription(writer) + "\n");
        }
    }


    @Test
    public void testTIFF() throws Exception {
        final List<ImageWriter> writers = findWriters("TIFF");
        for (final ImageWriter writer : writers) {
            Console.println("" + writer);
            Console.println(writersCapabilityDescription(writer) + "\n");
        }
    }


    public String writersCapabilityDescription(final ImageWriter writer) throws Exception {

        String r = "";
        r += "canWriteSequence: " + writer.canWriteSequence() + "\n";

        final ImageWriteParam writerParam = writer.getDefaultWriteParam();

        // Compression
        r += "canWriteCompressed: " + writerParam.canWriteCompressed() + "\n";
        if (writerParam.canWriteCompressed()) {
            writerParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            final String[] compressionTypes = writerParam.getCompressionTypes();
            r += "compressionTypes: " + Arrays.toString(compressionTypes) + "\n";
            for (final String compressionType : compressionTypes) {
                r += "compression: " + compressionType + "\n";
                writerParam.setCompressionType(compressionType);
                r += "compressionType: " + writerParam.getCompressionType() + "\n";
                r += "compressionQualityDescriptions: " + Arrays.toString(writerParam.getCompressionQualityDescriptions()) + "\n";
                r += "compressionQualityValues: " + Arrays.toString(writerParam.getCompressionQualityValues()) + "\n";
                r += "localizedCompressionTypeName: " + writerParam.getLocalizedCompressionTypeName() + "\n";
            }

//            r += "compressionMode: " + writerParam.getCompressionMode()+ "\n";
            r += "compressionQuality: " + writerParam.getCompressionQuality() + "\n";
            r += "losslessCompression: " + writerParam.isCompressionLossless() + "\n";
        }

        // Progressive
        r += "writeProgressive: " + writerParam.canWriteProgressive() + "\n";
        if (writerParam.canWriteProgressive()) {
            r += "progressiveMode: " + writerParam.getProgressiveMode() + "\n";
        }


//        System.out.println(": " + writerParam.canWriteTiles());
//        System.out.println(": " + writerParam.canOffsetTiles());
        r += "controller: " + writerParam.getController() + "\n";
        r += "defaultController: " + writerParam.getDefaultController() + "\n";

        return r;
    }


    private static ImageWriter findWriter(final String codecName) {

        {
            final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(codecName);
            while (writers.hasNext()) {
                System.out.println("Writer: " + writers.next());

            }
        }
        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(codecName);
        return writers.hasNext() ? writers.next() : null;

    }

    private static List<ImageWriter> findWriters(final String codecName) {

        final Iterator<ImageWriter> writersIterator = ImageIO.getImageWritersByFormatName(codecName);
        final List<ImageWriter> writers = new ArrayList<ImageWriter>();
        while (writersIterator.hasNext()) {
            writers.add(writersIterator.next());
        }

        return writers;

    }

}