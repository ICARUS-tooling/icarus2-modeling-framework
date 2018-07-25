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
 * without having to worry.
 * <p>
 * As a matter of consistency all three methods, when provided with
 * the same {@link StructureInfoField field} argument, should either
 * all return their respective <i>no-entry</i> sentinel value
 * ({@link IcarusUtils#UNSET_LONG} or {@link IcarusUtils#UNSET_DOUBLE}) or all provide
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
	 * supplied {@code field} and checks its equality to {@link IcarusUtils#UNSET_LONG}.
	 *
	 * @param field
	 * @return
	 */
	default boolean isUndefined(StructureInfoField field) {
		return getMin(field)==IcarusUtils.UNSET_LONG;
	}
}
