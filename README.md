IJP-ImageIO Plugin Bundle for ImageJ
====================================

[![Scala CI](https://github.com/ij-plugins/ijp-imageio/actions/workflows/scala.yml/badge.svg)](https://github.com/ij-plugins/ijp-imageio/actions/workflows/scala.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.sf.ij-plugins/ijp_imageio/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.sf.ij-plugins/ijp_imageio)
[![javadoc](https://javadoc.io/badge2/net.sf.ij-plugins/ijp_imageio/javadoc.svg)](https://javadoc.io/doc/net.sf.ij-plugins/ijp_imageio)


Overview
--------

IJP-ImageIO enable reading and writing images using Java ImageIO codecs. The core ImageIO formats: JPEG, PCX, PNG, PNM,
BMP, WBMP, and GIF. TIFF supports reading and writing using various compression schemes: LZW, JPEG, ZLib, and Deflate.
For more detailed information see [Wiki].


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

1. Download the latest binary release of ijp-imageio from [Releases](https://github.com/ij-plugins/ijp-imageio/releases)
   page: `ijp_imageio-<version>.jar`

2. Copy `ijp_imageio-<version>.jar` to ImageJ plugins directory. You can find location of ImageJ plugins directory by
   selecting "Plugins">"Utilities">"ImageJ Properties", look for value of tag "plugins dir" near the bottom of the
   displayed Properties' window.

3. Restart ImageJ to load newly installed plugins.


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

* Java compiler, tested with Oracle Java 11 (http://java.oracle.com). Java 9+ is required for TIFF support.
* [SBT build tool](https://www.scala-sbt.org/)
    
### Building

To rebuild and start included version of ImageJ type:
```
$ sbt ijRun
```

Support of Additional File Formats
----------------------------------

You can use additional image codecs that support Java ImageIO API and service registration. The JAR with the additional
codec will need to be added to application class path. For instance, you can get JPEG2000 support by adding JAR
from [jai-imageio-jpeg2000](https://github.com/jai-imageio/jai-imageio-jpeg2000) project.

License
-------

Copyright (C) 2002-2021 Jarek Sacha

This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later
version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
details.

You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA


[Wiki]: https://github.com/ij-plugins/ijp-imageio/wiki