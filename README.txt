============================
 ij-ImageIO PLUGIN BUNDLE
============================


OVERVIEW
--------

ij-ImageIO plugins add to ImageJ support for additional image file formats
and their variants, including BMP, PNG, PNM, JPEG, TIFF. Added support
provides for handling of compressed, tiled, 1bit, 16bit, and 32bit images.
For more detailed informations see subdirectory 'docs' of ij-ImageIO home
page at: http://ij-plugins.sf.net/plugins/imageio


PLUGINS
-------

ij-ImageIO install plugins under following ImageJ menu locations:

* File/Save As
  - PNG ...
  - PNM ...
  - Compressed TIFF ...
  
* Plugins/Image IO:
  - Open ...
  - Opem with preview ...
  - Save as ...
  - Remove obsolete JAI Image IO plugins ...
  
* Help/About Plugins
  - Image IO ...


SYSTEM REQUIREMENTS
-------------------

Image I/O plugin bundle requires Image/J 1.31s or newer and Java 1.4 or
newer. It was tested wit Java 1.4 and 1.5, may also work with Java 1.3.


INSTALLATION
------------

The installation is as simple as unzipping ij-ImageIO_bin_*.zip in the
ImageJ plugins folder (or placing ij-ImageIO_.jar in the plugins folder).

Previous installation of JAI Image IO (prior to 1.2.0) will interfere with
this version. Please remove them manually or use the "Remove obsolete JAI
Image IO plugins" plugin after installation of ij-ImageIO.


CONTACT INFORMATION
-------------------

Author : Jarek Sacha 
E-mail : jsacha@users.sourceforge.net
Webpage: http://ij-plugins.sourceforge.net/plugins/imageio


LICENSE
-------

Copyright (C) 2002-2004 Jarek Sacha

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