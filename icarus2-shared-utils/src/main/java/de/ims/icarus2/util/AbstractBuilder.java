/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.function.Function;

/**
 *
 * @param <B> the class of the derived builder, used to cast the results of appendable method calls
 * @param <O> class of the object the builder is constructing
 *
 * @author Markus Gärtner
 *
 */
public abstract class AbstractBuilder<B extends AbstractBuilder<B, O>, O extends Object> {

	private Function<B, O> constructor;

	@SuppressWarnings("unchecked")
	protected B thisAsCast() {
		return (B)this;
	}

	public B constructor(Function<B, O> constructor) {
		requireNonNull(constructor);
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

		//FIXME change this so that the final result is validated (not null, etc...) before returning it!
		return constructor==null ? create() : constructor.apply((B) this);
	}
}
