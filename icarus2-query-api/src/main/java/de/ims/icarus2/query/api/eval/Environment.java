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
import java.util.Set;

import javax.annotation.Nullable;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;

/**
 * @author Markus Gärtner
 *
 */
public interface Environment {

	default Optional<Environment> getParent() {
		return Optional.empty();
	}

	/**
	 * Returns the relative priority of this environment compared to others
	 * of the same scope.
	 * <p>
	 * The priority value is used when resolving references in a query to
	 * entries in the active environment(s). If a reference is resolved to
	 * entries from multiple environments, the one with the highest priority
	 * is preferred.
	 *
	 * @return
	 */
	default Priority getPriority() {
		return Priority.DEFAULT;
	}

	Set<NsEntry> getEntries();

	/**
	 * Returns the type that serves as the context of this namespace/environment.
	 * If this is a root environment, the method returns an empty optional.
	 */
	Optional<Class<?>> getContext();

	default boolean isGlobal() {
		return !getContext().isPresent();
	}

	/**
	 * An individual type-aware entry in a namespace/environment.
	 *
	 * @author Markus Gärtner
	 *
	 */
	interface NsEntry {
		//TODO add method to fetch description

		/**
		 * Info about the object type or return type of the field or method
		 * described by this entry.
		 */
		TypeInfo getValueType();

		/** Returns the primary identifier for this entry */
		String getName();

		/**
		 * Returns a set of alternative identifiers usable to reference this entry.
		 * Aliases have a special role for methods with zero arguments, as they allow
		 * field-style references as syntactic sugar:
		 * <pre>
		 * someObject.getName()
		 * vs.
		 * someObject.name
		 * </pre>
		 */
		//TODO write about array shortcuts via aliases
		Set<String> getAliases();

		default boolean hasAlias(String alias) {
			return getAliases().contains(alias);
		}

		/** Returns the kind of target this entry describes. */
		EntryType getEntryType();

		/**
		 * Returns the number of arguments or {@code 0} if this entry does not
		 * describe a method.
		 */
		int argumentCount();

		/**
		 * Returns the types of arguments consumed by the method described by
		 * this entry. If this entry describes a field or other non-method
		 * target, this method will throw a {@link QueryException}.
		 *
		 * @throws QueryException of type {@link QueryErrorCode#INCORRECT_USE}
		 * if this entry does not describe a {@link EntryType#METHOD method}.
		 */
		TypeInfo argumenTypeAt(int index);

		/** Returns the namespace this entry belongs to. */
		Environment getSource();

		/**
		 * Obtains a shared or newly instantiated expression to fetch or compute
		 * the target of this entry. It is the entry's job to perform sanity checks
		 * in order to ensure that the provided {@code target} and {@code arguments}
		 * expressions are type-compatible with the underlying method.
		 *
		 * @param target the instance to invoke a method on or to fetch a field from,
		 * or {@code null} if this entry points to a static field or method.
		 * @param arguments optional arguments to pass to the method invokation
		 * @return
		 */
		Expression<?> instantiate(EvaluationContext context, @Nullable Expression<?> target,
				Expression<?>...arguments);
	}

	public enum EntryType {
		FIELD,
		METHOD,
		UNDEFINED,
		;
	}

	public enum Priority {
		HIGHEST,
		HIGH,
		DEFAULT,
		LOW,
		LOWEST,
		;
	}
}
