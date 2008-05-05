/*
 * Image/J Plugins
 * Copyright (C) 2002-2008 Jarek Sacha
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
 *
 */

package net.sf.ij.jaiio;

import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

/**
 */
public class JaiioUtil {
    private JaiioUtil() {
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
}
