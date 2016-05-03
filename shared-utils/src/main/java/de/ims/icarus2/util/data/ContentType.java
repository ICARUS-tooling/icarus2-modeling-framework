/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision: 380 $
 * $Date: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/data/ContentType.java $
 *
 * $LastChangedDate: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $LastChangedRevision: 380 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.data;

import java.util.Map;

import de.ims.icarus2.util.Filter;
import de.ims.icarus2.util.id.Identity;


/**
 *
 *
 * @author Markus Gärtner
 * @version $Id: ContentType.java 380 2015-04-02 01:28:48Z mcgaerty $
 *
 */
public interface ContentType extends Identity, Filter {

	/**
	 * Returns the root class or interface that objects associated
	 * with this {@code ContentType} must extend or implement.
	 * This method must not return {@code null} but is technically
	 * allowed to return the {@link Object} class.
	 */
	Class<?> getContentClass();

	String getContentClassName();

	/**
	 * Returns a read-only collection of properties in the form of
	 * a key-value mapping. If this {@code ContentType} does not
	 * declare any properties it may either return an empty map or
	 * {@code null}.
	 */
	Map<String, Object> getProperties();

	/**
	 * Checks the given argument (typically a {@code Class} instance)
	 * for compatibility with this content-type.
	 * <p>
	 * Note that although all members of the content type framework
	 * will call this method with a valid {@code Class} argument, it
	 * not explicitly required to do so! Custom implementations that
	 * are designed to be used outside the framework might want to handle
	 * arbitrary argument objects.
	 *
	 * @see de.ims.icarus2.util.Filter#accepts(java.lang.Object)
	 */
	@Override
	boolean accepts(Object obj);

	/**
	 * Signals whether content is only allowed to be of the class
	 * returned by {@link #getContentClass()} and not of some subclass
	 * of it.
	 * <p>
	 * The property type is {@code boolean} and this property is only
	 * effective when the return value of {@link #getContentClass()}
	 * is not a class describing an interface, array or enum.
	 */
	public static final String STRICT_INHERITANCE = "strictInheritance"; //$NON-NLS-1$
}
