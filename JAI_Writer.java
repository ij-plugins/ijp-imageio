/*
 *  Image/J Plugins
 *  Copyright (C) 2002-2004 Jarek Sacha
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
 *  Latest release available at http://sourceforge.net/projects/ij-plugins/
 */
/**
 *  A proxy to JAIWriterPlugin that saves an image using <a
 *  href="http://developer.java.sun.com/developer/sampsource/jai/"> JAI image
 *  I/O codec </a> .
 *
 * @author     Jarek Sacha
 * @created    March 2, 2002
 * @version    $Revision: 1.6 $
 * @see        net.sf.ij.plugin.JAIWriterPlugin
 */
public class JAI_Writer extends JarPluginProxy {

  private final static String JAR_FILE_PATH = "ij-jai-imageio.jar";


  protected String getJarFilePath() {
    return JAR_FILE_PATH;
  }


  protected String getPluginClassName() {
    return "net.sf.ij.plugin.JAIWriterPlugin";
  }
}
