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
package de.ims.icarus2.model.api.members.structure;

import java.io.Serializable;

import de.ims.icarus2.model.api.meta.MetaData;
import de.ims.icarus2.util.IcarusUtils;

/**
 * Models a collection of structure related metadata.
 * To minimize the number of required methods to provide access to all
 * kinds of metadata field this interface only defines generic methods
 * that take a {@link StructureInfoField} object as hint.
 * A total of three such methods exists to fetch the minimum, maximum
 * and average value for a given field.
 * <p>
 * Note that no direct link to the original {@code Structure} object
 * is required, therefore client code can freely store this metadata
 * without having to worries.
 * <p>
 * As a matter of consistency all three methods, when provided with
 * the same {@link StructureInfoField field} argument, should either
 * all return their respective <i>no-entry</i> sentinel value
 * ({@link #NO_ENTRY_LONG} or {@link #NO_ENTRY_DOUBLE}) or all provide
 * a valid meaningful result.
 * This way it is sufficient for the default implementation of
 * {@link #isUndefined(StructureInfoField)} to only use one {@code long}
 * based method for quick verification.
 *
 * @author Markus Gärtner
 *
 */
public interface StructureInfo extends MetaData, Serializable {

	//TODO add info getter methods for stuff like min/max branching etc

	long getMin(StructureInfoField field);
	double getAvg(StructureInfoField field);
	long getMax(StructureInfoField field);

	/**
	 * Returns {@code true} iff this metadata set does not contain valid
	 * data for the given {@code field}. The default implementation uses
	 * the return value of {@link #getMin(StructureInfoField)} with the
	 * supplied {@code field} and checks its equality to {@link #NO_ENTRY_LONG}.
	 *
	 * @param field
	 * @return
	 */
	default boolean isUndefined(StructureInfoField field) {
		return getMin(field)==IcarusUtils.UNSET_LONG; //FIXME also use Double.compare
	}
}
