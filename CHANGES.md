IJP-ImageIO Changes
===================

IJP-ImageIO v.2.1.0
-------------------

This release moves away from Java 8, assuming Java 9 or better. A major new feature is better support for RGB48 TIFF images.

New features:
* Support saving of composite color images (48bit color TIFF) [Issue #3](https://github.com/ij-plugins/ijp-imageio/issues/3)
* Support reading of composite color images (48bit color TIFF) [Issue #4](https://github.com/ij-plugins/ijp-imageio/issues/4)
* Use new ImageIO (with TIFF etc) in Java 9+ [Issue #6](https://github.com/ij-plugins/ijp-imageio/issues/6)

Bug fixes:
* Image Title should be updated when image is saved [Issue #2](https://github.com/ij-plugins/ijp-imageio/issues/2)
* Do not assume unit 'pixels' if no unit is present [Issue #3](https://github.com/ij-plugins/ijp-imageio/issues/3)

IJP-ImageIO v.2.0.2
-------------------

Fixes for Java 9: use updated `jai-imageio-core` and remove dependency on `sun.misc.Service`.

IJP-ImageIO v.2.0.1
-------------------

Bug fix:
* [Issue #1]: java.lang.IllegalAccessError: tried to access class net.sf.ij_plugins.imageio.IJImageIO$ImageAndMetadata

[Issue #1]: https://github.com/ij-plugins/ijp-imageio/issues/1


IJP-ImageIO v.2.0
----------------------

New & improved:
* Removing dependency on old SUN libraries, moving to javax.imageio.
* Using codecs from jai-imageio-core, now maintained on GitHub [jai-imageio-core](https://github.com/jai-imageio/jai-imageio-core)
* Several improvements to plugins user interface.
* Library binaries published on Sonatype.

Bug fix:
* BUG-1434311: Problem saving 1 bit TIFF images.
* Correct how multi image files are combined into stacks.
* Numerous small big fixes.

Code level:
* Added unit tests.
* Major rewrite of the code.
* Build system changed to SBT.
* Source code moved to GitHub [ijp-imageio](https://github.com/ij-plugins/ijp-imageio)


IJ-ImageIO v.1.2.4
----------------------

Bug fix:

* Fix bug 1066969: First slice replaces all slices in 'Compressed' TIFF files.
  Fixed the problem by explicitly duplicating each slice in
  net.sf.ij.jaiio.BufferedImageCreator.create(ImagePlus src, int sliceNb).
  The problem was caused by unexpected side effect of ij.ImagePlus.setSlice(int).


IJ-ImageIO v.1.2.3
----------------------

Bug fix:

* Preserve 256 colors in 8 bit COLOR_256 images, even if they color map
  reports less colors.


IJ-ImageIO v.1.2.2
----------------------

Bug fixes:

* Fixed problems saving 8 bit gray level images in cases when ImageJ had
  16 bit color map for them.
* Fixed problem with use of 'save as' plugins in macros.

Developer visible changes:

* Added subdirectory example with with simple illustration how IJ-ImageIO
  can be used as a library.
* Minor changes to source based on feedback from 'FindBugs'
* Updated Javadoc documentation.
* ImageJ updated to v.1.33m.


IJ-ImageIO v.1.2.1
----------------------

Bug fix: Written files were always deleted (only partial writes due to 
         error should be deleted)
  
Bug fix: Error message was shown when save dialog was canceled.


IJ-ImageIO v.1.2.0
----------------------

* Use new IJ feature allowing to distribute plugins in JARS
* Save dialog now maintains file name when file type is changed
* New option dialog to select TIFF compression options
* "JAI Reader" plugin renamed to "Plugins/Image IO/Open ..."
* "JAI Reader with preview" plugin renamed to "Plugins/Image IO/Open with preview..."
* "JAI Writer" plugin renamed to "Plugins/Image IO/Save as ..."
* New shortcut: "File/Save As/PNG ..."
* New shortcut: "File/Save As/PNM ..."
* New shortcut: "File/Save As/Compressed TIFF ..."
* New plugin to display HTML help file (Help/About Plugin/Image IO)
* Plugin to remove obsolete installation of version 1.1 or earlier



IJ-JAI-ImageIO v.1.1.0
----------------------

Enhancement: JAI Reader and JAI Writer plugins support scripting through
             Image/J macros; both work well with Macro Recorder (tested
	     with Image/J 1.29x).
	     
Enhancement: JAI Reader with Preview plugin can combine multiple files 
             into a stack.	     


IJ-JAI-ImageIO v.1.0.5
----------------------

Bug fix: Saving of 2-bit images, that is images that have only two entries
         in the color map (Note: 2-bit compression is not implemented yet).


IJ-JAI-ImageIO v.1.0.4
----------------------

Bug fix: Pixel values of 16 bit images are not saved - saved images have 
         all pixels set to zero.

Bug fix: ClassCastException thrown when saving 'inch' as a calibration 
         unit in TIFF images. Problem caused by obscure JAI IO 'feature': 
         unsigned short TIFF Tags are represented as 'char's.            


IJ-JAI-ImageIO v.1.0.3
----------------------

JAI Reader with Preview now offers option to open only selected pages
(images) for files containing multiple pages (images).


IJ-JAI-ImageIO v.1.0.2
----------------------

This version provides a temporary fix for reading images with less
then 8 bits per pixel. Permanent fix will come with next release.


IJ-JAI-ImageIO v.1.0.1
----------------------

Optimized JAIFileFilter performance: improved filtering time and
eliminated occurrence of InterruptedIOException.



IJ-JAI-ImageIO v.1.0
--------------------

Version 1.0 resolves all outstanding issues and known bugs. Following
summarizes difference since beta version 0.8.


NEW FEATURES:

* JAI_Writer adds file name extension if not specified.

* JAI_Reader_with_Preview shows image dimensions (2D and 3D).

* Added reading and writing of ImageJ TIFF description string.


BUG FIXES:

* Fixed problem with reading some GIFs, now conversion is done using
  ImagePlus(String, Image) constructor.
  
* Corrected problem with chromosome3d.tif color maps (display intensity
  is now not adjustment for images with color maps).

* Preview now correctly shows 32 bit TIFF images.


SOURCE CHANGES

* Renamed "imageio" package to more accurate "jaiio" 

* Moved all JAI dependent code to "jaiio" package (except plugins).

* Removed any classes that are not used by the JAI Image IO plugin
  bundle.

* JarPluginProxy made more generic, JAR file path needs to be specified
  by derived classes.
  
* Update comments. Javadoc added to website.




