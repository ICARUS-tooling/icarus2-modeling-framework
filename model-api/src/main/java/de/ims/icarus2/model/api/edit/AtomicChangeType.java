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
 */
package de.ims.icarus2.model.api.edit;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import de.ims.icarus2.util.strings.StringResource;

/**
 * @author Markus Gärtner
 *
 */
public enum AtomicChangeType implements StringResource {

	INDEX_CHANGE("index"),

	ITEM_CHANGE("item"),
	ITEM_MOVE_CHANGE("item-move"),
	ITEMS_CHANGE("items"),

	EDGE_CHANGE("edge"),
	EDGE_MOVE_CHANGE("edge-move"),
	EDGES_CHANGE("edges"),

	TERMINAL_CHANGE("terminal"),
	POSITION_CHANGE("position"),

	VALUE_CHANGE("value"),
	;

	private final String key;

	AtomicChangeType(String key) {
		this.key = key;
	}

	/**
	 * @see de.ims.icarus2.util.strings.StringResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return key;
	}

	private static Map<String, AtomicChangeType> xmlLookup;

	private static Map<String, AtomicChangeType> getTypes0() {

		Map<String, AtomicChangeType> types = xmlLookup;

		if(types==null) {
			types = new THashMap<>();

			for(AtomicChangeType type : values()) {
				types.put(type.getStringValue(), type);
			}

			xmlLookup = types;
		}

		return types;
	}

	public static AtomicChangeType parseAtomicChangeType(String s) {
		return getTypes0().get(s);
	}

	//TODO maybe add methods for fetching all the available xml tags?
}
