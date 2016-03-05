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

package net.sf.ij_plugins.imageio;

import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @author Jarek Sacha
 */
final public class IJIOUtils {

    private IJIOUtils() {
    }

    /**
     * Check if image is marked as binary.
     *
     * @param ip image to check.
     * @return true if this image color map has only two entries.
     */
    public static boolean isBinary(final ImageProcessor ip) {
        final ColorModel cm = ip.getColorModel();
        if (cm instanceof IndexColorModel) {
            final int mapSize = ((IndexColorModel) cm).getMapSize();
            if (mapSize == 2) {
                return true;
            }
        }

        if (ip instanceof BinaryProcessor) {
            return true;
        }

        if (ip instanceof ByteProcessor) {
            final ByteProcessor bp = (ByteProcessor) ip;
            final long pixelCount = bp.getWidth() * bp.getHeight();
            final int[] hist = bp.getHistogram();

            return (hist[0] + hist[1] == pixelCount) || (hist[0] + hist[255] == pixelCount);
        }


        return false;
    }

    static public List<ImageWriterSpi> getImageWriterSpis() {
        List<ImageWriterSpi> spis = new ArrayList<>();

        final Iterator<ImageWriterSpi> categories = IIORegistry.getDefaultInstance().getServiceProviders(ImageWriterSpi.class, true);
        while (categories.hasNext()) {
            final ImageWriterSpi spi = categories.next();
            if (spi.getFileSuffixes().length > 0) {
                spis.add(spi);
            }
        }

//        // Sort by file suffix
//        Collections.sort(spis, (o1, o2) -> {
//            String[] f1 = o1.getFileSuffixes();
//            String[] f2 = o2.getFileSuffixes();
//            return f1[0].compareTo(f2[0]);
//        });
        return spis;
    }


    /**
     * Find the first {@link ImageWriterSpi} that has given {@code formatName}. Matches are done ignoring case.
     *
     * @param formatName desired writer format
     * @return first matching {@link ImageWriterSpi} or empty.
     */
    static public Optional<ImageWriterSpi> writerSpiByFormatName(final String formatName) {

        // Find first SPI matching the format name
        final List<ImageWriterSpi> spis = IJIOUtils.getImageWriterSpis();
        Optional<ImageWriterSpi> matchingSpi = Optional.empty();
        for (ImageWriterSpi spi : spis) {
            String[] formats = spi.getFormatNames();
            for (String thisFormatName : formats) {
                if (formatName.equalsIgnoreCase(thisFormatName)) {
                    matchingSpi = Optional.of(spi);
                    break;
                }
            }
            if (matchingSpi.isPresent()) {
                break;
            }
        }

        return matchingSpi;
    }

}
