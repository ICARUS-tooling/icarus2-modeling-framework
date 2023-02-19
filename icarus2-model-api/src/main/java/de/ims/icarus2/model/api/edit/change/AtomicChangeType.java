/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.edit.change;

import java.util.Map;

import de.ims.icarus2.util.strings.StringResource;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public enum AtomicChangeType implements StringResource {

	ITEM_CHANGE("item"),
	ITEM_MOVE_CHANGE("item-move"),
	ITEMS_CHANGE("items"),

	EDGE_CHANGE("edge"),
	EDGE_MOVE_CHANGE("edge-move"),
	EDGES_CHANGE("edges"),

	TERMINAL_CHANGE("terminal"),
	POSITION_CHANGE("position"),

	VALUE_CHANGE("value"),

	INT_VALUE_CHANGE("int-value"),
	LONG_VALUE_CHANGE("long-value"),
	FLOAT_VALUE_CHANGE("float-value"),
	DOUBLE_VALUE_CHANGE("double-value"),
	BOOLEAN_VALUE_CHANGE("boolean-value"),
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
			types = new Object2ObjectOpenHashMap<>();

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
