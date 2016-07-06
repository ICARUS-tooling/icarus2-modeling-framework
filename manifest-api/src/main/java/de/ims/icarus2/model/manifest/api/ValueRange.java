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

import de.ims.icarus2.model.manifest.types.ValueType;


/**
 * @author Markus Gärtner
 *
 */
public interface ValueRange extends Lockable, TypedManifest {

	public static final boolean DEFAULT_LOWER_INCLUSIVE_VALUE = true;
	public static final boolean DEFAULT_UPPER_INCLUSIVE_VALUE = true;

	Object getLowerBound();

	Object getUpperBound();

	Object getStepSize();

	boolean isLowerBoundInclusive();

	boolean isUpperBoundInclusive();

	ValueType getValueType();

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifest#getManifestType()
	 */
	@Override
	default public ManifestType getManifestType() {
		return ManifestType.VALUE_RANGE;
	}

	// Modification method

	void setLowerBound(Object lowerBound);

	void setUpperBound(Object upperBound);

	void setStepSize(Object stepSize);

	void setLowerBoundInclusive(boolean lowerBoundInclusive);

	void setUpperBoundInclusive(boolean upperBoundInclusive);
}
