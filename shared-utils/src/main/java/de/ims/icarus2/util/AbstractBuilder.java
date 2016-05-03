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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.util;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;
import static de.ims.icarus2.model.util.Conditions.checkState;

import java.util.function.Function;

/**
 *
 * @param <B> the class of the derived builder, used to cast the results of appendable method calls
 * @param <O> class of the object the builder is constructing
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractBuilder<B extends AbstractBuilder<B, O>, O extends Object> {

	private Function<B, O> constructor;

	@SuppressWarnings("unchecked")
	protected B thisAsCast() {
		return (B)this;
	}

	public B constructor(Function<B, O> constructor) {
		checkNotNull(constructor);
		checkState(this.constructor==null);

		this.constructor = constructor;

		return thisAsCast();
	}

	public Function<B, O> getConstructor() {
		return constructor;
	}

	protected void validate() {
		// no-op
	}

	protected abstract O create();

	/**
	 * {@link #validate() Validates} the content of this builder and then
	 * either calls {@link #create()} or tries using the supplied
	 * {@link #getConstructor() constructor} function if one has been set.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public O build() {
		validate();

		Function<B, O> constructor = getConstructor();

		return constructor==null ? create() : constructor.apply((B) this);
	}
}
