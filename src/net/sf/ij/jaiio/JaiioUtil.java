package net.sf.ij.jaiio;

import ij.process.ImageProcessor;

import java.awt.*;
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

    /**
     * Center window on screen.
     *
     * @param window
     * @param packFrame if <code>true</code> call window's <code>pack()</code>
     *                  method before centering.
     */
    public static void centerOnScreen(Window window, boolean packFrame) {
        //Validate frames that have preset sizes
        //Pack frames that have useful preferred size info, e.g. from their layout
        if (packFrame) {
            window.pack();
        } else {
            window.validate();
        }

        //Center the frame window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = window.getSize();

        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }

        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }

        window.setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2);
    }
}
