JAI-IMAGE I/O PLUGIN BUNDLE
---------------------------


OVERVIEW 

JAI Image I/O is an Image/J plugin bundle or reading and writing image
files using Java Advance Imaging (JAI) codec. It uses uncommitted source
code for JAI 1.1.1 codecs
http://developer.java.sun.com/developer/sampsource/jai/ The plugin bundle
is completely self contained. All used codec classes are included in the
bundle and there is no need to install JAI itself to use the bundle.

Most important functionality that this bundle adds to what already
available in Image/J and other plugins is ability to read 1 bit, 16 bit,
32 bit, float images from TIFF images that use tiles or compression.
Image/J built-in TIFF reader does not support tiles and compression. JIMI
plugins correctly handle only 8 bit images.

JAI Image I/O plugins can read and write 1 bit, 8 bit, 16 bit, 32 bit,
tiled, compressed (read), and multi-page TIFF images. Writing using LZW
compression is not supported for the usual reason of the patent on the
algorithm. Unlike JIMI plugins , JAI Image I/O plugins preserve sample
size, so for example, a 16 bit TIFF image is read into Image/J's
ShortProcessor. Plugins can additionally read/write image calibration
information from TIFF files. Standard TIFF image calibration tags are
supported (X_RESOLUTION, Y_RESOLUTION, RESOLUTION_UNIT). Additional image
formats supported by JAI Image I/O include BMP, FlashPIX, GIF, JPEG, and
PNG.


SOURCE

The root directory and net subdirectory contains source code for the JAI-
Image IO plugin bundle. Subdirectory non_com contains the uncommitted
source code for JAI 1.1.1 codecs.


CONTACT INFORMATION

Author : Jarek Sacha 
E-mail : jsacha@users.sourceforge.net
Webpage: http://ij-plugins.sourceforge.net/


LICENSE

Copyright (C) 2002,2003 Jarek Sacha

This library is free software; you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; either version 2.1 of the License, or (at
your option) any later version.

This library is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
General Public License for more details.


You should have received a copy of the GNU Lesser General Public License
along with this library; if not, write to the Free Software Foundation,
Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

