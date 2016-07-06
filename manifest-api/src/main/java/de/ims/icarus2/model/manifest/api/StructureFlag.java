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

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus2.util.Flag;
import de.ims.icarus2.util.strings.StringResource;

/**
 *
 * @author Markus Gärtner
 *
 */
public enum StructureFlag implements StringResource, Flag {

	/**
	 * Specifies whether edges are allowed to be virtual (i.e. they
	 * may have virtual items assigned to them as terminals). Note
	 * that this restriction only applies to edges that are <b>not</b>
	 * attached to the virtual root node of a structure!
	 */
	VIRTUAL("virtual"),

	/**
	 * Specifies whether or not structures are allowed
	 * to have an edge count of {@code 0}, i.e. being empty.
	 */
	EMPTY("empty"),

	/**
	 * Arrangement of edges in a structure can be altered by the user.
	 */
	NON_STATIC("non-static"),

	/**
	 * Signals that edges in a structure are allowed to have the same
	 * item assigned as source and target terminal.
	 */
	LOOPS("loops"),

	/**
	 * Specifies whether or not a structure requires its edges to be arranged according
	 * to the default item ordering defined by the model.
	 */
	ORDERED("ordered"),

	/**
	 * Signals that a structure is not required to use all its nodes.
	 * If set a structure is not allowed to host nodes for which the
	 * {@link Structure#getEdgeCount(de.ims.icarus2.model.api.members.Item) edge count}
	 * is {@code 0}. This property exists to enable optimization for very compact
	 * implementations of certain structure types like {@link StructureType#CHAIN chains}
	 * where the total number of possible edges is fixed by the number of nodes.
	 */
	PARTIAL("partial"),

	/**
	 * Specifies whether or not a structure may have more than {@code 1} edge
	 * assigned to its virtual root node (effectively meaning that it has in fact
	 * several "real" root nodes).
	 */
	MULTI_ROOT("multi-root"),

	;
	private final String xmlForm;

	private StructureFlag(String xmlForm) {
		this.xmlForm = xmlForm;
	}

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return xmlForm;
	}

	private static Map<String, StructureFlag> xmlLookup;

	public static StructureFlag parseStructureFlag(String s) {
		if(xmlLookup==null) {
			Map<String, StructureFlag> map = new HashMap<>();
			for(StructureFlag type : values()) {
				map.put(type.xmlForm, type);
			}
			xmlLookup = map;
		}

		return xmlLookup.get(s);
	}
}