/*
 *  IJ-Plugins ImageIO
 *  Copyright (C) 2002-2021 Jarek Sacha
 *  Author's email: jpsacha at gmail dot com
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
 *  Latest release available at https://github.com/ij-plugins/ijp-imageio/
 */

package ij_plugins.imageio.impl;

import javax.swing.*;
import java.io.File;

/**
 * Factory for creation of JAI IO customized file choosers.
 *
 * @author Jarek Sacha
 */

public class ImageFileChooserFactory {

    /**
     * Creates an image file open chooser with an image preview. File filters correspond to
     * registered JAI decoders.
     *
     * @return Description of the Returned Value
     */
    public static OpenImageFileChooser createJAIOpenChooser() {
        return new OpenImageFileChooser(new File(".").getAbsoluteFile());
    }


    /**
     * Creates file save chooser with file filters corresponding to JAI codecs supporting writing
     * (encoders).
     *
     * @return Description of the Returned Value
     */
    public static JFileChooser createJAISaveChooser() {

        return new SaveImageFileChooser(new File(".").getAbsoluteFile());
    }
}
