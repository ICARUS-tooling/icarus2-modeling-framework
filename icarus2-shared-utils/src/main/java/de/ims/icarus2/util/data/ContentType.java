/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.util.data;

import java.util.Map;

import de.ims.icarus2.util.Filter;
import de.ims.icarus2.util.id.Identity;


/**
 *
 *
 * @author Markus Gärtner
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
