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
package net.sf.ij.jaiio;

import javax.swing.*;
import java.io.File;

/**
 * Factory for creation of JAI IO customized file choosers.
 * 
 * @author Jarek Sacha
 * @version $Revision: 1.2 $
 */

public class JAIFileChooserFactory {

    /**
     * Creates an image file open chooser with an image preview. File filters
     * correspond to registered JAI decoders.
     * 
     * @return Description of the Returned Value
     */
    public static ImageFileChooser createJAIOpenChooser() {
        ImageFileChooser chooser = new ImageFileChooser(new File(".").getAbsoluteFile());

        return chooser;
    }


    /**
     * Creates file save chooser with file filters corresponding to JAI codecs
     * supporting writing (encoders).
     * 
     * @return Description of the Returned Value
     */
    public static JFileChooser createJAISaveChooser() {
        JFileChooser chooser = new SaveImageFileChooser(new File(".").getAbsoluteFile());

        return chooser;
    }
}
