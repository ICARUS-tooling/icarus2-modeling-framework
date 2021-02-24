/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util;

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus2.util.strings.StringResource;

/**
 * Defines the multiplicity of allowed (external) elements in a certain context.
 *
 * @author Markus Gärtner
 *
 */
public enum Multiplicity implements StringResource {

	/**
	 * Defines an "empty" docking point for external entities.
	 */
	NONE("none", 0, 0),

	/**
	 * Docking point for at most one external entity.
	 */
	NONE_OR_ONE("none-or-one", 0, 1),

	/**
	 * Requires exactly one external entity to be docked.
	 */
	ONE("one", 1, 1),

	/**
	 * Requires at least one external entity to be docked but
	 * poses no upper limit.
	 */
	ONE_OR_MORE("one-or-more", 1, -1),

	/**
	 * Unrestricted docking point.
	 */
	ANY("any", 0, -1),
	;

	private static final int UNRESTRICTED = -1;

	private final String xmlForm;
	private final int min, max;

	private Multiplicity(String xmlForm, int min, int max) {
		this.xmlForm = xmlForm;
		this.min = min;
		this.max = max;
	}

	/**
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return name().toLowerCase();
	}

	/**
	 * @see de.ims.icarus2.util.strings.StringResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return xmlForm;
	}

	public boolean isLegalCount(int value) {
		if(value<0) {
			return false;
		}

		if(min!=UNRESTRICTED && value<min) {
			return false;
		}

		if(max!=UNRESTRICTED && value>max) {
			return false;
		}

		return true;
	}

	public int getRequiredMinimum() {
		return min;
	}

	public int getAllowedMaximum() {
		return max;
	}

	private static Map<String, Multiplicity> xmlLookup;

	public static Multiplicity parseMultiplicity(String s) {
		if(xmlLookup==null) {
			Map<String, Multiplicity> map = new HashMap<>();
			for(Multiplicity type : values()) {
				map.put(type.xmlForm, type);
			}
			// Ignore thread-safety, since the outcome will be the same anyway
			xmlLookup = map;
		}

		return xmlLookup.get(s);
	}
}