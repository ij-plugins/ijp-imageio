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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static net.sf.ij_plugins.imageio.IJImageIO.PREFERRED_SPI_VENDOR;

/**
 * @author Jarek Sacha
 */
final public class IJImageOUtils {

    private IJImageOUtils() {
    }


    /**
     * Check if image is marked as binary. Image is assumed binary if  for each slice one of the following is true:
     * <ul>
     * <li>all pixels are only 0 and 255 (similar to ImageJ test),</li>
     * <li>all pixels are only 0 and 1,</li>
     * <li>slice is an instance of a {@link BinaryProcessor},</li>
     * <li>slice has an {@link IndexColorModel} with map size of 2. </li>
     * </ul>
     *
     * @param imp image to check.
     * @return true if this image color map has only two entries.
     */
    public static boolean isBinary(final ImagePlus imp) {
        ImageStack stack = imp.getStack();
        boolean isBinary = true;
        for (int i = 1; i <= stack.getSize(); i++) {
            isBinary &= isBinary(stack.getProcessor(i));
            if (!isBinary) break;
        }

        return isBinary;
    }

    /**
     * Check if image is marked as binary. Image is assumed binary here is one of the following is true:
     * <ul>
     * <li>all pixels are only 0 and 255 (similar to ImageJ test),</li>
     * <li>all pixels are only 0 and 1,</li>
     * <li>ip is an instance of a {@link BinaryProcessor},</li>
     * <li>ip has an {@link IndexColorModel} with map size of 2. </li>
     * </ul>
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

        List<ImageWriterSpi> prefferedSPIs = new ArrayList<>();
        List<ImageWriterSpi> otherSPIs = new ArrayList<>();

        final Iterator<ImageWriterSpi> categories = IIORegistry.getDefaultInstance().getServiceProviders(ImageWriterSpi.class, true);
        while (categories.hasNext()) {
            final ImageWriterSpi spi = categories.next();
            if (spi.getFileSuffixes().length > 0) {
                if(spi.getVendorName().toLowerCase().contains(PREFERRED_SPI_VENDOR)) {
                    prefferedSPIs.add(spi);
                } else {
                    otherSPIs.add(spi);
                }
            }
        }

        prefferedSPIs.addAll(otherSPIs);

        return prefferedSPIs;
    }


    /**
     * Find the first {@link ImageWriterSpi} that has given {@code formatName}. Matches are done ignoring case.
     *
     * @param formatName desired writer format
     * @return first matching {@link ImageWriterSpi} or empty.
     */
    static public List<ImageWriterSpi> writerSpiByFormatName(final String formatName) {

        // Find first SPI matching the format name
        final List<ImageWriterSpi> spis = IJImageOUtils.getImageWriterSpis();
        List<ImageWriterSpi> matchingSpis = new ArrayList<>();
        for (ImageWriterSpi spi : spis) {
            String[] formats = spi.getFormatNames();
            for (String thisFormatName : formats) {
                if (formatName.equalsIgnoreCase(thisFormatName)) {
                    matchingSpis.add(spi);
                }
            }
        }

        return matchingSpis;
    }

    /**
     * Find a writer for given format name. If there is more that one writer it may select a preferred one.
     * The preferred vendor for the writer is gicen by {@link net.sf.ij_plugins.imageio.IJImageIO#PREFERRED_SPI_VENDOR}.
     *
     * @param formatName format name.
     * @return a writer for given format name or null if not found.
     */
    public static List<ImageWriter> getImageWritersByFormatName(final String formatName) {

        if (IJ.debugMode) {
            final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
            while (writers.hasNext()) {
                IJ.log("Writer: " + writers.next());
            }
        }
        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);

        List<ImageWriter> preferredWriters = new ArrayList<>();
        List<ImageWriter> otherWriters = new ArrayList<>();
        while (writers.hasNext()) {
            ImageWriter writer = writers.next();
            ImageWriterSpi spi = writer.getOriginatingProvider();
            if (spi != null && spi.getVendorName().contains(PREFERRED_SPI_VENDOR)) {
                preferredWriters.add(writer);
            } else {
                otherWriters.add(writer);
            }
        }

        preferredWriters.addAll(otherWriters);

        return preferredWriters;
    }


}
