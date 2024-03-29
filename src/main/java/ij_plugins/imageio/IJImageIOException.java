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
package ij_plugins.imageio;

/**
 * Exception specific to package net.sf.ij_plugins.imageio.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class IJImageIOException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public IJImageIOException(final String message) {
        super(message);
    }

    public IJImageIOException(final Throwable cause) {
        super(cause);
    }

    public IJImageIOException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
