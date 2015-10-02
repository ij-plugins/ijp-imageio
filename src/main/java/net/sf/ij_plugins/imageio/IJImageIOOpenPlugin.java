/*
 * Image/J Plugins
 * Copyright (C) 2002-2015 Jarek Sacha
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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Opens file chooser dialog and reads images using {@link IJImageIO}.
 *
 * @author Jarek Sacha
 */
public class IJImageIOOpenPlugin implements PlugIn {
    private static final String TITLE = "Image IO Open";

    /**
     * Argument passed to <code>run</code> method to use open dialog with an image preview.
     */
    public static final String ARG_IMAGE_PREVIEW = "preview";

    private static ImageFileChooser jaiChooser;


    /**
     * Main processing method for the IJImageIOOpenPlugin object. Type of the file dialog is
     * determined by value of <code>arg</code>. If it is equal "{@value #ARG_IMAGE_PREVIEW}" then
     * file chooser with image preview will be used. By default standard Image/J's open dialog is
     * used.
     *
     * @param arg Can be user to specify type of the open dialog.
     */
    public void run(final String arg) {

        IJ.showStatus("Starting \"" + TITLE + "\" plugin...");

        final FilesAndPageIndex fpi = ARG_IMAGE_PREVIEW.equalsIgnoreCase(arg)
                ? selectFilesWithPreview()
                : selectFile();


        if (fpi.files.length < 1) {
            return;
        }

        final boolean combineIntoStack = fpi.files.length > 1
                && IJ.showMessageWithCancel(TITLE,
                "" + fpi.files.length + " files selected.\n"
                        + "Should the images be combined into a stack?");


        if (combineIntoStack) {
            // Open images
//            open(files, pageIndex, combineIntoStack);
            final List<ImagePlus> imageList = new ArrayList<>();
            for (final File file : fpi.files) {
                final ImagePlus[] images = open(file, fpi.pageIndex);
                imageList.addAll(Arrays.asList(images));
            }

            if (imageList.size() == 1) {
                imageList.get(0).show();
            } else if (imageList.size() > 1) {
                // Attempt to combine
                final ImagePlus stackImage = combineImages(imageList);
                // Show
                if (stackImage != null) {
                    stackImage.show();
                } else {
                    if (imageList.size() > 1) {
                        IJ.showMessage(TITLE, "Unable to combine images into a stack.\n" +
                                "Loading each separately.");
                    }
                    for (final ImagePlus anImageList : imageList) {
                        anImageList.show();
                    }
                }
            }
        } else {
            for (final File file : fpi.files) {
                final ImagePlus[] images = open(file, fpi.pageIndex);
                for (final ImagePlus imp : images) {
                    imp.show();
                }
            }
        }

        IJ.showStatus("");
    }


    private FilesAndPageIndex selectFilesWithPreview() {
        if (jaiChooser == null) {
            jaiChooser = JAIFileChooserFactory.createJAIOpenChooser();
            final String dirName = OpenDialog.getDefaultDirectory();
            final File dirFile = new File(dirName != null ? dirName : ".");
            jaiChooser.setCurrentDirectory(dirFile);
            jaiChooser.setMultiSelectionEnabled(true);
        }

        if (jaiChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return new FilesAndPageIndex(jaiChooser.getSelectedFiles(), jaiChooser.getPageIndex());
        } else {
            return new FilesAndPageIndex(new File[0], null);
        }

    }

    /*
    *
    */
    private FilesAndPageIndex selectFile() {

        final OpenDialog openDialog = new OpenDialog(TITLE, null);
        if (openDialog.getFileName() == null) {
            return new FilesAndPageIndex(new File[0], null);
        }

        final File[] files = new File[1];
        files[0] = new File(openDialog.getDirectory(), openDialog.getFileName());
        return new FilesAndPageIndex(files, null);
    }


    private ImagePlus[] open(final File file, int[] pageIndex) {
        IJ.showStatus("Opening: " + file.getName());
        try {
            return IJImageIO.read(file, true, pageIndex);
        } catch (final Exception ex) {
            ex.printStackTrace();
            String message = "Error opening file: " + file.getName() + ".\n\n";
            message += (ex.getMessage() == null) ? ex.toString() : ex.getMessage();
            IJ.showMessage(TITLE, message);
            return new ImagePlus[0];
        }
    }


//    /*
//    *
//    */
//    private void open(final File[] files, final int[] pageIndex, final boolean combineIntoStack) {
//
//        IJ.showStatus("Opening selected image file...");
//
//        List<ImagePlus> imageList = null;
//        if (combineIntoStack) {
//            imageList = new ArrayList<ImagePlus>();
//        }
//
//        for (final File file : files) {
//            IJ.showStatus("Opening: " + file.getName());
//            try {
//                final ImagePlus[] images = JAIReader.read(file, pageIndex);
//                if (images != null) {
//                    for (final ImagePlus image : images) {
//                        if (imageList != null) {
//                            imageList.add(image);
//                        } else {
//                            image.show();
//                        }
//                    }
//                }
//            } catch (final Exception ex) {
//                ex.printStackTrace();
//                String msg = "Error opening file: " + file.getName() + ".\n\n";
//                msg += (ex.getMessage() == null) ? ex.toString() : ex.getMessage();
//                IJ.showMessage(TITLE, msg);
//            }
//        }
//
//        if (imageList != null && imageList.size() > 0) {
//            final ImagePlus stackImage = combineImages(imageList);
//            if (stackImage != null) {
//                stackImage.show();
//            } else {
//                if (imageList.size() > 1) {
//                    IJ.showMessage(TITLE, "Unable to combine images into a stack.\n" +
//                            "Loading each separately.");
//                }
//                for (final ImagePlus anImageList : imageList) {
//                    anImageList.show();
//                }
//            }
//        }
//    }


    /**
     * Attempts to combine images on the list into a stack. If successful return the combined image,
     * otherwise return null. Images cannot be combined if they are of different types,  different
     * sizes, or have more then single slice.
     *
     * @param imageList List of images to combine into a stack.
     * @return Combined image if successful, otherwise null.
     */
    private static ImagePlus combineImages(final List<ImagePlus> imageList) {
        // TODO: in unable to combine throw exception with error message, do not return null.
        if (imageList == null || imageList.size() < 1) {
            return null;
        }

        if (imageList.size() == 1) {
            return imageList.get(0);
        }

        final ImagePlus firstImage = imageList.get(0);
        if (firstImage.getStackSize() != 1) {
            return null;
        }

        final int fileType = firstImage.getFileInfo().fileType;
        final int w = firstImage.getWidth();
        final int h = firstImage.getHeight();
        final ImageStack stack = firstImage.getStack();
        for (int i = 1; i < imageList.size(); ++i) {
            final ImagePlus im = imageList.get(i);
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

    private static class FilesAndPageIndex {
        final File[] files;
        final int[] pageIndex;

        public FilesAndPageIndex(File[] files, int[] pageIndex) {
            this.files = files;
            this.pageIndex = pageIndex;
        }
    }

}
