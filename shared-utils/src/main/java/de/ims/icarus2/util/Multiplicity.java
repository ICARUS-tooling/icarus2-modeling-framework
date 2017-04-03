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
	 * Defines an "empty" docking point for external entities
	 */
	NONE("none", 0, 0),

	/**
	 * Docking point for at most one external entity
	 */
	NONE_OR_ONE("none-or-one", 0, 1),

	/**
	 * Requires exactly one external entity to be docked
	 */
	ONE("one", 1, 1),

	/**
	 * Requires at least one external entity to be docked but
	 * poses no upper limit.
	 */
	ONE_OR_MORE("one-or-more", 1, -1),

	/**
	 * Unrestricted docking point
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