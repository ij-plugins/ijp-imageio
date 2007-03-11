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
