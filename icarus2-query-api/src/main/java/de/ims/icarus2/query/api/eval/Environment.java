/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.query.api.eval;

import java.util.Optional;

import javax.annotation.Nullable;

import de.ims.icarus2.util.Wrapper;

/**
 * @author Markus Gärtner
 *
 */
public interface Environment {

	default Optional<Environment> getParent() {
		return Optional.empty();
	}

	/**
	 * Tries to resolve the given {@code name} to a field or no-args method
	 * equivalent. Using the {@code resultFilter} argument, returned expressions
	 * can be restricted to be return type compatible to a desired target type.
	 */
	Expression<?> resolve(String name, @Nullable TypeFilter filter, EvaluationContext context);

	/**
	 * Tries to resolve the given {@code name} to a method that takes the
	 * specified {@code arguments} as input. The {@code context} defines
	 * whether or not it is allowed to apply value conversion, value expansion
	 * or any other kind of syntactic sugar operations.
	 * If the {@code resultFilter} argument is provided, it will be used to
	 * restrict the pool of methods to be considered to those that return
	 * a compatible value.
	 */
	Expression<?> resolve(String name, @Nullable TypeFilter resultFilter,
			Expression<?>[] arguments, EvaluationContext context);

	/**
	 * Try to resolve the specified {@code name} to an entry in this namespace.
	 */
//	default NsEntry lookup(String name) {
//		return lookup(name, TypeFilter.ALL);
//	}

	/**
	 * Try to resolve the specified {@code name} to an entry in this namespace
	 * that satisfies the specified filter. This will return
	 */
//	NsEntry lookup(String name, TypeFilter filter);

	/**
	 * An individual type-aware entry in a namespace.
	 *
	 * @author Markus Gärtner
	 *
	 */
	interface NsEntry extends Wrapper<Object> {
		/** Info about the (expected) type of the entry. */
		TypeInfo getType();

		/** Fetches and returns the actual content of this entry. */
		@Override
		Object get();

		/** Returns the namespace this entry belongs to. */
		Environment getSource();
	}

	public enum EntryType {
		FIELD,
		METHOD,
		UNDEFINED,
		;
	}
}
