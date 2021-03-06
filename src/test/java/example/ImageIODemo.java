/*
 *  IJ Plugins
 *  Copyright (C) 2002-2020 Jarek Sacha
 *  Author's email: jpsacha at gmail.com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Latest release available at https://github.com/ij-plugins/ijp-imageio
 */
package example;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Jarek Sacha
 */
public class ImageIODemo {
    public static void main(String[] args) {
        describeWriters();
        System.out.println("\n---------------------------------\n");
        listAll();
    }

    public static void listAll() {
        String[] readerNames = ImageIO.getReaderFormatNames();
        printList("Reader Names:", readerNames);

        String[] readerMimes = ImageIO.getReaderMIMETypes();
        printList("Reader MIME types:", readerMimes);

        String[] writerNames = ImageIO.getWriterFormatNames();
        printList("Writer Names:", writerNames);

        String[] writerMimes = ImageIO.getWriterMIMETypes();
        printList("Writer MIME types:", writerMimes);
    }

    public static void describeWriters() {
        String[] writerNames = getUniqueWriterFormatNames();
        //        printList("Writer Names:", writerNames);
        for (String writerName : writerNames) {
            System.out.println(writerName + " writers:");
            Iterator<ImageWriter> iterator = ImageIO.getImageWritersByFormatName(writerName);
            while (iterator.hasNext()) {
                ImageWriter writer = iterator.next();
                System.out.println("\tClass name        : " + writer.getClass().getName());
                String canWriteEmpty;
                try {
                    canWriteEmpty = "" + writer.canWriteEmpty();
                } catch (Exception e) {
                    canWriteEmpty = "?";
                }
                System.out.println("\tCan write empty   : " + canWriteEmpty);
                System.out.println("\tCan write rasters : " + writer.canWriteRasters());
                System.out.println("\tCan write sequence: " + writer.canWriteSequence());

                ImageWriteParam param = writer.getDefaultWriteParam();
                System.out.println("\tCan offset tiles           : " + param.canOffsetTiles());
                System.out.println("\tCan write compressed       : " + param.canWriteCompressed());
                System.out.println("\tCan write progressive      : " + param.canWriteProgressive());
                if (param.canWriteProgressive()) {
                    //                    param.setCompressionMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
                    System.out.println("\tProgressive mode: " + param.getProgressiveMode());
                }
                System.out.println("\tCan write tiles            : " + param.canWriteTiles());
                if (param.canWriteCompressed()) {
                    System.out.println("\tDefault compression mode   : " + param.getCompressionMode());
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    //                    System.out.println("\tIs compression lossless   : " + param.isCompressionLossless());
                    //                System.out.println("\tDefault compression quality: "+param.getCompressionQuality());
                    String[] compressionTypes = param.getCompressionTypes();
                    System.out.print("\tQuality descriptors: ");
                    for (String compressionType : compressionTypes) {
                        System.out.print(compressionType + ", ");
                    }
                    System.out.println();

                    for (String compressionType : compressionTypes) {
                        param.setCompressionType(compressionType);
                        String[] qualityDescriptors = param.getCompressionQualityDescriptions();
                        float[] qualityValues = param.getCompressionQualityValues();
                        if (qualityDescriptors != null && qualityDescriptors.length > 0) {
                            System.out.print("\tCompression quality descriptors for " + compressionType + ": ");
                            for (int k = 0; k < qualityDescriptors.length; k++) {
                                System.out.print(qualityDescriptors[k] + "[" + qualityValues[k] + "], ");
                            }
                            System.out.println();
                        }
                    }
                }
                System.out.println("\tController            : " + param.getController());
                System.out.println("\tDefault controller    : " + param.getDefaultController());
            }
        }
    }

    private static String[] getUniqueWriterFormatNames() {
        String[] writerNames = ImageIO.getWriterFormatNames();
        Map<String, String> map = new TreeMap<>();
        for (String writerName : writerNames) {
            Iterator<ImageWriter> iterator = ImageIO.getImageWritersByFormatName(writerName);
            while (iterator.hasNext()) {
                String className = iterator.next().getClass().getName();
                if (!map.containsKey(className)) {
                    map.put(className, writerName);
                }
            }
        }

        Collection<String> values = map.values();
        return values.toArray(new String[0]);
    }

    static void printList(String title, String[] list) {
        System.out.println(title);
        for (String aList : list) {
            System.out.println("\t" + aList);
        }
    }
}
