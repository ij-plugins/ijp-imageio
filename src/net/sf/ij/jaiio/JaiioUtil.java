package net.sf.ij.jaiio;

import ij.process.ImageProcessor;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

/**
 * Created by IntelliJ IDEA.
 * User: jarek
 * Date: Jan 29, 2003
 * Time: 9:57:46 PM
 * To change this template use Options | File Templates.
 */
public class JaiioUtil {
    private JaiioUtil() {
    }

    /**
     * Return true if this image color map has only two entries.
     */
    public static boolean isBinary(ImageProcessor ip) {
        ColorModel cm = ip.getColorModel();
        if (cm instanceof IndexColorModel
                && ((IndexColorModel) cm).getMapSize() == 2) {
            return true;
        }

        return false;
    }
}
