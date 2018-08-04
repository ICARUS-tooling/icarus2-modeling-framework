/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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

import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.eval.Expression;

/**
 * A discrete collection of values.
 * Note that this interface does not differentiate between primitive and
 * value types and complex objects.
 * <p>
 * Values in this collection can have any of the following types:
 * <ul>
 * <li>An {@link Expression} relying on the environment this set is used in to generate the final values</li>
 * <li>A wrapper object around a primitive value</li>
 * <li>The value as an object</li>
 * </ul>
 *
 * @author Markus Gärtner
 *
 */
public interface ValueSet extends Lockable, TypedManifest {

	int valueCount();

	Object getValueAt(int index);

	void forEachValue(Consumer<? super Object> action);

	default Set<Object> getValues() {
		return LazyCollection.lazySet(valueCount())
				.addFromForEach(this::forEachValue)
				.getAsSet();
	}

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
	default void addValue(Object value) {
		addValue(value, -1);
	}

	/**
	 * Adds the given {@code value} at the specified {@code index}.
	 * If the {@code index} is {@code -1} then the {@code value} should
	 * be appended to the end of this set.
	 *
	 * @param value
	 * @param index
	 */
	void addValue(Object value, int index);

	void removeValue(int index);
}
