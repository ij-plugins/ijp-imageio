/*
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
import java.io.BufferedInputStream;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *  Loads classes from a JAR file.
 *
 * @author     Jarek Sacha
 * @created    January 26, 2002
 * @version    $Revision: 1.7 $
 */
public class JarClassLoader extends ClassLoader {
  private final static int BUFFER_SIZE = 0xFFFF;

  private String jarFileName;


  /**
   *  Constructor for the JarClassLoader object.
   *
   * @param  jarFileName  Name of the JAR file from where to load classes.
   */
  public JarClassLoader(String jarFileName) {
    super();
    this.jarFileName = jarFileName.replace('\\', '/');
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
    this.jarFileName = jarFileName.replace('\\', '/');
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


  /**
   *  Finds the resource with the given name.
   *
   * @param  name  The resource name.
   * @return       A URL for reading the resource, or null if the resource could
   *      not be found.
   */
  public URL findResource(String name) {
    try {
      URL url = new java.net.URL("jar", null,
          "file:/" + jarFileName + "!/" + name);
      if (url != null) {
        // Verify URL
        try {
          java.io.InputStream inputStream = url.openStream();
          inputStream.close();
        }
        catch (Throwable t) {
          return null;
        }
      }

      return url;
    }
    catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }


  /*
   *
   */
  private byte[] loadClassData(String name) throws ClassNotFoundException {
    File file = new File(jarFileName);
    if (!file.exists()) {
      throw new ClassNotFoundException("Unable to load class " + name +
          " from " + file.getAbsolutePath() + ". JAR file does not exist.");
    }

    String className = name + ".class";
    try {
      JarFile jarFile = new JarFile(file);
      // Look for an entry corresponding to requested class
      Enumeration entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = (JarEntry) entries.nextElement();
        String entryName = entry.getName().replace('/', '.');
        if (entryName.equals(className)) {
          // Load class
          BufferedInputStream bis = new BufferedInputStream(
              jarFile.getInputStream(entry));
          byte[] buffer = new byte[BUFFER_SIZE];
          int totalBytesRead = 0;
          int bytesReadThisTime = 0;
          while (bytesReadThisTime != -1) {
            bytesReadThisTime = bis.read(buffer, totalBytesRead,
                buffer.length - totalBytesRead);
            if (bytesReadThisTime != -1) {
              totalBytesRead += bytesReadThisTime;
            }
          }
          bis.close();

          byte[] data = new byte[totalBytesRead];
          System.arraycopy(buffer, 0, data, 0, data.length);
          return data;
        }
      }
    }
    catch (Exception ex) {
      throw new ClassNotFoundException(
          "Unable to load class " + name + " from "
          + file.getAbsolutePath() + ". Exception: " + ex.toString());
    }

    throw new ClassNotFoundException(
        "Unable to load class " + name + " from "
        + file.getAbsolutePath() +
        ". Class entry not found in the JAR file.");
  }
}
