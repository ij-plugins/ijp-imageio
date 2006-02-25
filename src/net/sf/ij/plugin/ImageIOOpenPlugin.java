/***
 * Image/J Plugins
 * Copyright (C) 2002-2004 Jarek Sacha
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
package net.sf.ij.plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import net.sf.ij.jaiio.ImageFileChooser;
import net.sf.ij.jaiio.JAIFileChooserFactory;
import net.sf.ij.jaiio.JAIReader;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Opens file chooser dialog and open the image using JAI codec.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.6 $
 */
public class ImageIOOpenPlugin implements PlugIn {
    private static final String TITLE = "Image IO Open";

    /**
     * Argument passed to <code>run</code> method to use standard Image/J open dialog.
     */
    public static final String ARG_SIMPLE = "simple";
    /**
     * Argument passed to <code>run</code> method to use open dialog with an image preview.
     */
    public static final String ARG_IMAGE_PREVIEW = "preview";

    private static ImageFileChooser jaiChooser;
    private File[] files;
    private boolean combineIntoStack = true;


    /*
     *
     */
    private void open(File[] files, int[] pageIndex) {
        ArrayList imageList = null;
        if (combineIntoStack) {
            imageList = new ArrayList();
        }

        for (int i = 0; i < files.length; ++i) {
            IJ.showStatus("Opening: " + files[i].getName());
            try {
                ImagePlus[] images = JAIReader.read(files[i], pageIndex);
                if (images != null) {
                    for (int j = 0; j < images.length; ++j) {
                        if (imageList != null)
                            imageList.add(images[j]);
                        else
                            images[j].show();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                String msg = "Error opening file: " + files[i].getName() + ".\n\n";
                msg += (ex.getMessage() == null) ? ex.toString() : ex.getMessage();
                IJ.showMessage(TITLE, msg);
            }
        }

        if (imageList != null && imageList.size() > 0) {
            ImagePlus stackImage = combineImages(imageList);
            if (stackImage != null) {
                imageList = null;
                stackImage.show();
            } else {
                if (imageList.size() > 1) {
                    IJ.showMessage(TITLE, "Unable to combine images into a stack.\n" +
                            "Loading each separately.");
                }
                for (int i = 0; i < imageList.size(); ++i) {
                    ((ImagePlus) imageList.get(i)).show();
                }
            }
        }
    }

    /**
     * Attempts to combine images on the list into a stack. If successful return the combined image,
     * otherwise return null. Images cannot be combined if they are of different types,  different
     * sizes, or have more then single slice.
     *
     * @param imageList List of images to combine into a stack.
     * @return Combined image if successful, otherwise null.
     */
    private static ImagePlus combineImages(ArrayList imageList) {
        // TODO: in unable to combine throw exception with error message, do not return null.
        if (imageList == null || imageList.size() < 1)
            return null;

        if (imageList.size() == 1) {
            return (ImagePlus) imageList.get(0);
        }

        ImagePlus firstImage = (ImagePlus) imageList.get(0);
        if (firstImage.getStackSize() != 1) {
            return null;
        }

        int fileType = firstImage.getFileInfo().fileType;
        int w = firstImage.getWidth();
        int h = firstImage.getHeight();
        ImageStack stack = firstImage.getStack();
        for (int i = 1; i < imageList.size(); ++i) {
            ImagePlus im = (ImagePlus) imageList.get(i);
            if (im.getStackSize() != 1) {
                return null;
            }
            if (fileType == im.getFileInfo().fileType
                    && w == im.getWidth() && h == im.getHeight()) {
                stack.addSlice(im.getTitle(), im.getProcessor().getPixels());
            } else {
                return null;
            }
        }

        firstImage.setStack(firstImage.getTitle(), stack);
        return firstImage;
    }


    /**
     * Main processing method for the ImageIOOpenPlugin object. Type of the file dialog is
     * determined by value of <code>arg</code>. If it is equal <code>ARG_IMAGE_PREVIEW</code> then
     * file chooser with image preview will be used. By default standard Image/J's open dialog is
     * used.
     *
     * @param arg Can be user to specify type of the open dialog.
     */
    public void run(String arg) {

        IJ.showStatus("Starting \"" + TITLE + "\" plugin...");

        String type = (arg == null) ? ARG_SIMPLE : arg.trim().toLowerCase();

        files = null;
        int[] pageIndex = null;
        if (type.equals(ARG_IMAGE_PREVIEW)) {
            selectFilesWithImagePreview();
        } else {
            selectFiles();
        }

        if (files == null)
            return;

        if (files.length > 1) {
            combineIntoStack = IJ.showMessageWithCancel(TITLE,
                    "" + files.length + " files selected.\n"
                            + "Should the images be combined into a stack?");
        }

        if (files != null) {
            IJ.showStatus("Opening seleced image file...");
            open(files, pageIndex);
        }

        IJ.showStatus("");
    }


    /*
     *
     */
    private void selectFilesWithImagePreview() {
        if (jaiChooser == null) {
            jaiChooser = JAIFileChooserFactory.createJAIOpenChooser();
            final String dirName = OpenDialog.getDefaultDirectory();
            final File dirFile = new File(dirName != null ? dirName : ".");
            jaiChooser.setCurrentDirectory(dirFile);
            jaiChooser.setMultiSelectionEnabled(true);
        }

        if (jaiChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            files = jaiChooser.getSelectedFiles();
        } else {
            files = null;
        }
    }


    /*
     *
     */
    private void selectFiles() {

        OpenDialog openDialog = new OpenDialog(TITLE, null);
        if (openDialog.getFileName() == null) {
            // No selection
            files = null;
            return;
        }

        if (files == null || files.length != 1) {
            files = new File[1];
        }
        files[0] = new File(openDialog.getDirectory(), openDialog.getFileName());
    }
}
