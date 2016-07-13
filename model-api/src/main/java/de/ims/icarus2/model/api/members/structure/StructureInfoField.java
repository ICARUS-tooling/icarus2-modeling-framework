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
package de.ims.icarus2.model.api.members.structure;

import java.util.EnumSet;

import de.ims.icarus2.model.manifest.api.StructureType;

/**
 * @author Markus Gärtner
 *
 */
public enum StructureInfoField {

	/**
	 * @deprecated duplicate of {@link #OUTGOING_EDGES}
	 */
	@Deprecated
	BRANCHING,
	INCOMING_EDGES,
	OUTGOING_EDGES,
	HEIGHT(StructureType.CHAIN, StructureType.TREE, StructureType.SET),
	DEPTH(StructureType.CHAIN, StructureType.TREE, StructureType.SET),
	DESCENDANTS(StructureType.CHAIN, StructureType.TREE, StructureType.SET),
	;

	private final EnumSet<StructureType> supportedTypes;

	StructureInfoField(StructureType...types) {
		if(types==null || types.length==0) {
			supportedTypes = null;
		} else {
			supportedTypes = EnumSet.noneOf(StructureType.class);
			for(StructureType type : types) {
				supportedTypes.add(type);
			}
		}
	}

	public boolean isTypeSupported(StructureType type) {
		return supportedTypes==null || supportedTypes.contains(type);
	}
}
