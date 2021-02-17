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
 * A few methods from org.apache.commons.lang3.Validate, duplicated to reduce runtime dependencies.
 */
interface Validate {

    /**
     * <p>Validate that the specified argument array is neither {@code null}
     * nor a length of zero (no elements); otherwise throwing an exception
     * with the specified message.
     * </p>
     * <pre>Validate.notEmpty(myArray, "The array must not be empty");</pre>
     *
     * @param <T>     the array type
     * @param array   the array to check, validated not null by this method
     * @param message the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values  the optional values for the formatted exception message, null array not recommended
     * @return the validated array (never {@code null} method for chaining)
     * @throws NullPointerException     if the array is {@code null}
     * @throws IllegalArgumentException if the array is empty
     */
    static <T> T[] notEmpty(final T[] array, final String message, final Object... values) {
        if (array == null) {
            throw new NullPointerException(String.format(message, values));
        }
        if (array.length == 0) {
            throw new IllegalArgumentException(String.format(message, values));
        }
        return array;
    }


    /**
     * <p>Validate that the specified argument is not {@code null};
     * otherwise throwing an exception.
     * </p>
     * <pre>Validate.notNull(myObject, "The object must not be null");</pre>
     *
     * <p>The message of the exception is &quot;The validated object is
     * null&quot;.</p>
     *
     * @param <T>    the object type
     * @param object the object to check
     * @return the validated object (never {@code null} for method chaining)
     * @throws NullPointerException if the object is {@code null}
     * @see #notNull(Object, String, Object...)
     */
    static <T> T notNull(final T object) {
        return notNull(object, "The validated object is null");
    }

    /**
     * <p>Validate that the specified argument is not {@code null};
     * otherwise throwing an exception with the specified message.
     * </p>
     * <pre>Validate.notNull(myObject, "The object must not be null");</pre>
     *
     * @param <T>     the object type
     * @param object  the object to check
     * @param message the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values  the optional values for the formatted exception message
     * @return the validated object (never {@code null} for method chaining)
     * @throws NullPointerException if the object is {@code null}
     * @see #notNull(Object)
     */
    static <T> T notNull(final T object, final String message, final Object... values) {
        if (object == null) {
            throw new NullPointerException(String.format(message, values));
        }
        return object;
    }

    /**
     * <p>Validate that the argument condition is {@code true}; otherwise
     * throwing an exception with the specified message. This method is useful when
     * validating according to an arbitrary boolean expression, such as validating a
     * primitive number or using your own custom validation expression.</p>
     * <pre>
     * Validate.isTrue(i &gt;= min &amp;&amp; i &lt;= max, "The value must be between &#37;d and &#37;d", min, max);
     * Validate.isTrue(myObject.isOk(), "The object is not okay");</pre>
     *
     * @param expression the boolean expression to check
     * @param message    the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values     the optional values for the formatted exception message, null array not recommended
     * @throws IllegalArgumentException if expression is {@code false}
     */
    static void isTrue(final boolean expression, final String message, final Object... values) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(message, values));
        }
    }
}
