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

	Set<NsEntry> getEntries();

	/**
	 * An individual type-aware entry in a namespace.
	 *
	 * @author Markus Gärtner
	 *
	 */
	interface NsEntry {
		/**
		 * Info about the object type or return type of the field or method
		 * described by this entry.
		 */
		TypeInfo getValueType();

		/** Returns the kind of target this entry describes. */
		EntryType getEntryType();

		/**
		 * Returns the types of arguments consumed by the method described by
		 * this entry. If this entry describes a field or other non-method
		 * target, this method will throw a {@link QueryException}.
		 *
		 * @throws QueryException of type {@link QueryErrorCode#INCORRECT_USE}
		 * if this entry does not describe a {@link EntryType#METHOD method}.
		 */
		TypeInfo[] getArgumenTypes();

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
