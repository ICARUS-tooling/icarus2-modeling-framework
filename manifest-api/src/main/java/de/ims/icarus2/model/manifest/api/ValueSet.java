/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 */
package de.ims.icarus2.model.manifest.api;

import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.eval.Expression;

/**
 * A discrete collection of values.
 * Note that this interface does not differentiate between primitive and
 * value types and complex objects.
 * <p>
 * Values in this collection can have any of the following types:
 * <ul>
 * <li>{@link Expression} relying on the environment the set is used in to generate the final values</li>
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

//	int getIntegerValueAt(int index);
//	long getLongValueAt(int index);
//	float getFloatValueAt(int index);
//	double getDoubleValueAt(int index);
//	boolean getBooleanValueAt(int index);

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
