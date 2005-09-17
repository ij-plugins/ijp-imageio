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
package net.sf.ij_plugins.imageio;

import junit.framework.TestCase;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class ReaderTest extends TestCase {
    public ReaderTest(String test) {
        super(test);
    }

    /**
     * The fixture set up called before every test method.
     */
    protected void setUp() throws Exception {
    }

    /**
     * The fixture clean up called after every test method.
     */
    protected void tearDown() throws Exception {
    }

    public void testTIFFStack() throws Exception {
        final String fileName = "test/data/mri-stack.tif";
        final File file = new File(fileName);

        assertTrue("Exist: " + file.getAbsolutePath(), file.exists());

        final ImageInputStream iis = ImageIO.createImageInputStream(file);

        final Iterator readers = ImageIO.getImageReaders(iis);

        final ArrayList readerList = new ArrayList();
        System.out.println("Readers: ");
        while (readers.hasNext()) {
            final ImageReader reader = (ImageReader) readers.next();
            System.out.println(reader.getFormatName() + " : " + reader.getClass().getName());
            readerList.add(reader);
        }

        assertTrue("At least one reader available", !readerList.isEmpty());

        final ImageReader reader = (ImageReader) readerList.get(0);

        reader.setInput(iis, false, false);
        int numImages = reader.getNumImages(true);

        System.out.println("Min index: " + reader.getMinIndex());

        //        final ImageReadParam readParam = reader.getDefaultReadParam();
        //        readParam.
        //        IIOParamController controller = readParam.getController();
        //        if(controller != null) {
        //            controller.activate(readParam);
        //        }


        for (int i = 0; i < numImages; ++i) {
            //            IIOImage iioImage = reader.readAll(i, readParam);
            //            IIOMetadata metadata = iioImage.getMetadata();
            IIOMetadata metadata = reader.getImageMetadata(i);
            System.out.println("Metadata: " + metadata);
            System.out.println("  Metadata format names: ");
            String[] formats = metadata.getMetadataFormatNames();
            for (int j = 0; j < formats.length; j++) {
                String format = formats[j];
                System.out.println("  " + format);
            }

            for (int j = 0; j < formats.length; j++) {
                String format = formats[j];
                System.out.println("Using format " + format);
                displayMetadata(metadata.getAsTree(format));
            }
        }


    }

    public void displayMetadata(Node root) {
        displayMetadata(root, 0);
    }

    void indent(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
    }

    void displayMetadata(Node node, int level) {
        indent(level); // emit open tag
        System.out.print("<" + node.getNodeName());
        NamedNodeMap map = node.getAttributes();
        if (map != null) { // print attribute values
            int length = map.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = map.item(i);
                System.out.print(" " + attr.getNodeName() +
                        "=\"" + attr.getNodeValue() + "\"");
            }
        }

        Node child = node.getFirstChild();
        if (child != null) {
            System.out.println(">"); // close current tag
            while (child != null) { // emit child tags recursively
                displayMetadata(child, level + 1);
                child = child.getNextSibling();
            }
            indent(level); // emit close tag
            System.out.println("</" + node.getNodeName() + ">");
        } else {
            System.out.println("/>");
        }
    }


}