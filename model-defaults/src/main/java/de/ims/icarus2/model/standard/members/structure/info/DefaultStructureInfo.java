/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

	private static final StructureInfoField[] _fields = StructureInfoField.values();

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
