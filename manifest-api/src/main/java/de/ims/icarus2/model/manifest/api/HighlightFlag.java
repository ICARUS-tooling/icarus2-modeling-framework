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
 * @author Markus Gärtner
 *
 */
public enum HighlightFlag implements StringResource, Flag {

	/**
	 * Signals that a {@link Highlight} object may affect not only the top most
	 * items in a target container, but also deeply nested members.
	 */
	DEEP_HIGHLIGHTING("deep-highlighting"),

//	/**
//	 * Signals that cursors for a given {@link HighlightLayer} do not support
//	 * scrolling through concurrent highlight data and will throw an exception
//	 * for each of the scrolling related methods in the {@link HighlightCursor cursor} interface.
//	 */
//	CURSOR_NO_SCROLL("cursor-no-scroll"),
//
//	/**
//	 * Signals that
//	 */
//	CURSOR_FORWARD_ONLY("cursor-forward-only"),

	/**
	 * Signals that the user or other client code is allowed to make modifications to existing
	 * highlight information, remove it or add new data to it.
	 */
	EDITABLE("editable"),
	;

	private final String xmlForm;

	private HighlightFlag(String xmlForm) {
		this.xmlForm = xmlForm;
	}

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return xmlForm;
	}

	private static Map<String, HighlightFlag> xmlLookup;

	public static HighlightFlag parseHighlightFlag(String s) {
		if(xmlLookup==null) {
			// No real need to care about concurrency here
			Map<String, HighlightFlag> map = new HashMap<>();
			for(HighlightFlag type : values()) {
				map.put(type.xmlForm, type);
			}
			xmlLookup = map;
		}

		return xmlLookup.get(s);
	}
}
