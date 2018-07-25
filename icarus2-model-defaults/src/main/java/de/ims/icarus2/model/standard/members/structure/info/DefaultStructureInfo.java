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
package de.ims.icarus2.model.standard.members.structure.info;

import de.ims.icarus2.model.api.members.structure.StructureInfo;
import de.ims.icarus2.model.api.members.structure.StructureInfoField;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultStructureInfo implements StructureInfo {

	private static final long serialVersionUID = -2562709334248640742L;

	/**
	 * Package-private so that {@link StructureInfoBuilder} can access it for consistency.
	 * In case we ever decide to
	 */
	static final StructureInfoField[] _fields = StructureInfoField.values();

	/**
	 * Package-private so that {@link StructureInfoBuilder} can access it for consistency.
	 */
	static int index(StructureInfoField field) {
		return field.ordinal();
	}

	/**
	 * Use {@link StructureInfoField#ordinal() ordinal} to get index for entry
	 */
	private final double[] avgValues = new double[_fields.length];

	/**
	 * Use {@link StructureInfoField#ordinal() ordinal*2} to get index for min entry
	 * and {@code ordinal*2 + 1} for max entry.
	 */
	private final long[] minMaxValues = new long[_fields.length*2];

	DefaultStructureInfo(double[] avgValues, long[] minMaxValues) {
		//TODO assign internal arrays to the given arguments!!!
	}

	static int avgIndex(StructureInfoField field) {
		return field.ordinal();
	}

	static int minIndex(StructureInfoField field) {
		return field.ordinal()<<1;
	}

	static int maxIndex(StructureInfoField field) {
		return (field.ordinal()<<1)+1;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureInfo#getAvg(de.ims.icarus2.model.api.members.structure.StructureInfoField)
	 */
	@Override
	public double getAvg(StructureInfoField field) {
		return avgValues[avgIndex(field)];
	}

//	void setAvg(StructureInfoField field, double value) {
//		avgValues[field.ordinal()] = value;
//	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureInfo#getMin(de.ims.icarus2.model.api.members.structure.StructureInfoField)
	 */
	@Override
	public long getMin(StructureInfoField field) {
		return minMaxValues[minIndex(field)];
	}

//	void setMin(StructureInfoField field, long value) {
//		minMaxValues[field.ordinal()<<1] = value;
//	}

	/**
	 * @see de.ims.icarus2.model.api.members.structure.StructureInfo#getMax(de.ims.icarus2.model.api.members.structure.StructureInfoField)
	 */
	@Override
	public long getMax(StructureInfoField field) {
		return minMaxValues[maxIndex(field)];
	}

//	void setMax(StructureInfoField field, long value) {
//		minMaxValues[(field.ordinal()<<1)+1] = value;
//	}
}
