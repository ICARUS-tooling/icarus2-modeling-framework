/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

	/**
	 * Indicator for which types of structures this field can be used.
	 * Set to {@code null} if the field is unrestricted and universally
	 * applies to all types of structures.
	 */
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
