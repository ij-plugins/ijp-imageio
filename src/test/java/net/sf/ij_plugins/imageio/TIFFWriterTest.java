/*
 * Image/J Plugins
 * Copyright (C) 2002-2010 Jarek Sacha
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.util.Arrays;
import java.util.Iterator;


/**
 * @author Jarek Sacha
 * @since Nov 3, 2010 8:49:06 PM
 */
public final class TIFFWriterTest {

    /**
     * The fixture set up called before every test method.
     */
    @Before
    public void setUp() {
    }


    /**
     * The fixture clean up called after every test method.
     */
    @After
    public void tearDown() {
    }


    @Test
    public void testSomething() throws Exception {
        final ImageWriter writer = findWriter("TIFF");
        final ImageWriteParam writerParam = writer.getDefaultWriteParam();

        System.out.println("");
        System.out.println("canWriteCompressed: " + writerParam.canWriteCompressed());
        if (writerParam.canWriteCompressed()) {
            writerParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            String[] compressionTypes = writerParam.getCompressionTypes();
            System.out.println("getCompressionTypes: " + Arrays.toString(compressionTypes));
            for (String compressionType : compressionTypes) {
                System.out.println("compression: " + compressionType);
                writerParam.setCompressionType(compressionType);
                System.out.println("getCompressionType: " + writerParam.getCompressionType());
                System.out.println("getCompressionQualityDescriptions: " + Arrays.toString(writerParam.getCompressionQualityDescriptions()));
                System.out.println("getCompressionQualityValues: " + Arrays.toString(writerParam.getCompressionQualityValues()));
                System.out.println("getLocalizedCompressionTypeName: " + writerParam.getLocalizedCompressionTypeName());
            }

//            System.out.println("getCompressionMode: " + writerParam.getCompressionMode());
//            System.out.println("getCompressionQuality: " + writerParam.getCompressionQuality());
        }
        System.out.println("canWriteProgressive: " + writerParam.canWriteProgressive());
        if (writerParam.canWriteProgressive()) {
            System.out.println("getProgressiveMode: " + writerParam.getProgressiveMode());
        }
//        System.out.println(": " + writerParam.canWriteTiles());
//        System.out.println(": " + writerParam.canOffsetTiles());
        System.out.println("getController: " + writerParam.getController());
        System.out.println("getDefaultController: " + writerParam.getDefaultController());
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

}