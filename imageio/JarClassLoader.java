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
import java.util.jar.*;
import java.io.File;
import java.io.FileInputStream;

/**
 *  Loads classes from a JAR file.
 *
 * @author     Jarek Sacha
 * @created    January 26, 2002
 * @version    $Revision: 1.1.1.1 $
 */
public class JarClassLoader extends ClassLoader {

  private String jarFileName;
  private final static int BUFFER_SIZE = 0xFFFF;


  /**
   *  Constructor for the JarClassLoader object.
   *
   * @param  jarFileName  Name of the JAR file from where to load classes.
   */
  public JarClassLoader(String jarFileName) {
    super();
    this.jarFileName = jarFileName;
  }


  /**
   *  Creates a new class loader using the specified parent class loader for
   *  delegation.
   *
   * @param  jarFileName  Name of the JAR file from where to load classes.
   * @param  parent       The parent class loader.
   */
  public JarClassLoader(String jarFileName, ClassLoader parent) {
    super(parent);
    this.jarFileName = jarFileName;
  }


  /**
   *  Finds the specified class in the JAR file.
   *
   * @param  name                        The name of the class.
   * @return                             The resulting Class object.
   * @exception  ClassNotFoundException  If the class could not be found.
   */
  public Class findClass(String name) throws ClassNotFoundException {
    byte[] b = loadClassData(name);
    return defineClass(name, b, 0, b.length);
  }


  /*
   *
   */
  private byte[] loadClassData(String name) throws ClassNotFoundException {
    File f = new File(jarFileName);
    if (!f.exists()) {
      throw new ClassNotFoundException("Unable to load class " + name +
          " from " + f.getAbsolutePath() + ". JAR file does not exist.");
    }

    String className = name + ".class";
    try {
      JarInputStream jarInputStream = new JarInputStream(new FileInputStream(f));
      JarEntry entry = jarInputStream.getNextJarEntry();
      while (entry != null) {
        String entryName = entry.getName().replace('/', '.');
        if (entryName.equals(className)) {
          byte[] buffer = new byte[BUFFER_SIZE];
          int totalBytesRead = 0;
          int bytesReadThisTime = 0;
          while (bytesReadThisTime != -1) {
            bytesReadThisTime = jarInputStream.read(
                buffer, totalBytesRead, buffer.length - totalBytesRead);
            if (bytesReadThisTime != -1) {
              totalBytesRead += bytesReadThisTime;
            }
          }
          jarInputStream.close();

          byte[] data = new byte[totalBytesRead];
          System.arraycopy(buffer, 0, data, 0, data.length);
          return data;
        }
        else {
          entry = jarInputStream.getNextJarEntry();
        }
      }
      jarInputStream.close();
    }
    catch (Exception ex) {
      throw new ClassNotFoundException(
          "Unable to load class " + name + " from "
           + f.getAbsolutePath() + ". Exception: " + ex.toString());
    }

    throw new ClassNotFoundException(
        "Unable to load class " + name + " from "
         + f.getAbsolutePath() +
        ". Class entry not found in the JAR file.");
  }
}
