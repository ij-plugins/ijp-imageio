/*
 * Image/J Plugins
 * Copyright (C) 2002 Jarek Sacha
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

import ij.IJ;
import ij.ImagePlus;
import ij.Menus;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

import java.io.File;
import java.lang.reflect.Method;

import net.sf.ij.imageio.JAIReader;

/**
 *  Read image files using JAI image I/O codec
 *  (http://developer.java.sun.com/developer/sampsource/jai/).
 *
 * @author     Jarek Sacha
 * @created    January 22, 2002
 * @version    $Revision: 1.1 $
 */
public class JAI_Reader_with_Preview extends JAI_Reader {

  public JAI_Reader_with_Preview() {
    jaiReaderPluginArg = "image preview";
  }

}