IJP-ImageIO Plugin Bundle for ImageJ
====================================

[![Build Status](https://travis-ci.org/ij-plugins/ijp-imageio.svg?branch=master)](https://travis-ci.org/ij-plugins/ijp-imageio) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.sf.ij-plugins/ijp_imageio/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.sf.ij-plugins/ijp_imageio)
[![javadoc](https://javadoc-badge.appspot.com/net.sf.ij-plugins/ijp_imageio.svg?label=javadoc)](https://javadoc-badge.appspot.com/net.sf.ij-plugins/ijp_imageio)


Overview
--------

IJP-ImageIO enable reading and writing images using Java ImageIO codecs. The core ImageIO formats: JPEG, PCX, PNG, PNM, BMP, WBMP, and GIF. TIFF supports reading and writing using various compression schemes: LZW, JPEG,
ZLib, and Deflate. For more detailed information see [IJP-ImageIO home page](http://ij-plugins.sf.net/plugins/imageio).


Plugins
-------

IJP-ImageIO install plugins under following ImageJ menu locations:

* File/Save As
  - PNG ...
  - PNM ...
  - Compressed TIFF ...
  
* Plugins/Image IO:
  - Open ...
  - Open with preview ...
  - Save as ...
  
* Help/About Plugins
  - IJP-ImageIO ...
  - IJP-ImageIO readers & writers ...


System Requirements
-------------------

Image I/O plugin bundle requires ImageJ 1.48v or newer and Java 9 or newer.


Installation
------------

The installation is as simple as unzipping ijp-imageio_bin_*.zip in the
ImageJ plugins folder (or placing ijp-imageio_.jar in the plugins folder).


Using as a Stand-alone Library
------------------------------

You IJP-ImageIO as a stand-alone library. You will need to add dependency on:

```
groupId   : net.sf.ij-plugins
artifactId: ijp_imageio
version   : <version>
```
For instance, for [SBT] it would be:

```
"net.sf.ij-plugins" % "ijp_imageio" % "<version>"
```

Building
-----------------------------------

### Prerequisites:
  * Java compiler, tested with Oracle Java 11 (http://java.oracle.com)

  * [SBT build tool](https://www.scala-sbt.org/)
    - tested with 1.3.8
    
### Building

To rebuild and start included version of ImageJ type:
```
$ sbt ijRun
```

Support of Additional File Formats
----------------------------------

You can use additional image codecs that support Java ImageIO API and service registration. The JAR with the additional codec will need to be added to application class path. For instance you can get JPEG2000 support by adding JAR from [jai-imageio-jpeg2000](https://github.com/jai-imageio/jai-imageio-jpeg2000) project.

License
-------

Copyright (C) 2002-2020 Jarek Sacha

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