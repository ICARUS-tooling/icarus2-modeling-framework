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
package de.ims.icarus2.model.manifest.api;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.Searchable;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.eval.Expression;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

/**
 * A discrete collection of values.
 * Note that this interface does not differentiate between primitive and
 * value types and complex objects.
 * <p>
 * Values in this collection can have any of the following types:
 * <ul>
 * <li>An {@link Expression} relying on the environment this set is used in to generate the final values</li>
 * <li>A wrapper object around a primitive value</li>
 * <li>A {@link ValueManifest} that wraps a value and adds additional information to it</li>
 * <li>The value as an object</li>
 * </ul>
 *
 * @author Markus Gärtner
 *
 */
public interface ValueSet extends Lockable, TypedManifest, Searchable<Object> {

	int valueCount();

	Object getValueAt(int index);

	/**
	 * Executes the given {@code action} for every entry in this set.
	 *
	 * @see de.ims.icarus2.util.Traversable#forEach(java.util.function.Consumer)
	 */
	@Override
	void forEach(Consumer<? super Object> action);

	/**
	 * Returns the elements in this {@link ValueSet} as a regular {@link Set}.
	 * <p>
	 * Note: As we cannot predict the nature of objects stored in a {@code ValueSet},
	 * we have to take defensive measures in case the elements aren't suitable to be
	 * stored in a hash-based collection (if for example they don't obey the general
	 * contracts for {@link Object#equals(Object)} and {@link Object#hashCode()}).
	 * Therefore the returned {@code Set} is behaving like an implementation that
	 * uses object identity instead of the basic equality checks.
	 *
	 * @see IdentityHashMap
	 * @see ReferenceSet
	 *
	 * @return
	 */
	default Set<Object> getValuesAsSet() {
		return LazyCollection.lazyIdentitySet(valueCount())
				.addFromForEach(this::forEach)
				.getAsSet();
	}

	default List<Object> getValuesAsList() {
		return LazyCollection.lazyList(valueCount())
				.addFromForEach(this::forEach)
				.getAsList();
	}

	Object[] getValues();

	/**
	 * Returns the type of this set
	 */
	ValueType getValueType();

	/**
	 * Returns the {@code index} for the given {@code value} (i.e. the {@code index}
	 * that when used for {@link #getValueAt(int)} will yield the {@link Object#equals(Object) same}
	 * {@code value}), or {@code -1} if the {@code value} is not present in this set.
	 *
	 * @param value
	 * @return
	 */
	int indexOfValue(Object value);

	// Modification methods

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifest#getManifestType()
	 */
	@Override
	default public ManifestType getManifestType() {
		return ManifestType.VALUE_SET;
	}

	/**
	 * Adds the given {@code value} to the end of this set
	 *
	 * @param value
	 */
	void addValue(Object value);

	/**
	 * Adds the given {@code value} at the specified {@code index}.
	 *
	 * @param value
	 * @param index
	 */
	void addValue(Object value, int index);

	void removeValueAt(int index);

	default void removeValue(Object value) {
		int index = indexOfValue(value);
		if(index==-1)
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"Unknown value: "+value);
		removeValueAt(index);
	}

	void removeAllValues();
}
