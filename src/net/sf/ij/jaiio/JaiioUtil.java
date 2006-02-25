package net.sf.ij.jaiio;

import ij.process.ImageProcessor;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

/**
 */
public class JaiioUtil {
    private JaiioUtil() {
    }

    /**
     * Return true if this image color map has only two entries.
     */
    public static boolean isBinary(ImageProcessor ip) {
        ColorModel cm = ip.getColorModel();
        if (cm instanceof IndexColorModel) {
            int mapSize = ((IndexColorModel) cm).getMapSize();
            if (mapSize == 2) {
                return true;
            }
        }

        return false;
    }
}
