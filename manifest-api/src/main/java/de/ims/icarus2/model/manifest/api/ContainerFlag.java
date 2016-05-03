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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.manifest.api;

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus2.util.Flag;
import de.ims.icarus2.util.strings.StringResource;

public enum ContainerFlag implements StringResource, Flag {

	/**
	 * Containers are allowed to contain virtual items
	 * (i.e. those items have no fixed <i>physical location</i> in the surrounding
	 * abstract corpus resource).
	 */
	VIRTUAL("virtual"),

	/**
	 * Specifies whether or not containers are allowed
	 * to have an item count of {@code 0}, i.e. being empty.
	 * Note that this restriction is only used when the framework verifies a container
	 * constructed by a driver implementation or when checking whether or not an
	 * attempted action is feasible.
	 */
	EMPTY("empty"),

	/**
	 * Arrangement of items in this container can be altered by the user.
	 */
	NON_STATIC("non-static"),

	/**
	 * Specifies whether a container requires all its contained items to be unique.
	 * Note that this is only relevant for aggregating containers which recruit
	 * their content from foreign containers. The items owned by a container (i.e.
	 * the one it introduces to the corpus framework) are <b>always</b> unique!
	 */
	DUPLICATES("duplicates"),

	/**
	 * Specifies whether or not a container requires its content to be arranged according
	 * to the default item ordering defined by the model. Note that this is only of
	 * interest for aggregating containers, since the top level elements of each layer
	 * must always be ordered!
	 */
	ORDERED("ordered"),

	;
	private final String xmlForm;

	private ContainerFlag(String xmlForm) {
		this.xmlForm = xmlForm;
	}

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return xmlForm;
	}

	private static Map<String, ContainerFlag> xmlLookup;

	public static ContainerFlag parseContainerFlag(String s) {
		if(xmlLookup==null) {
			Map<String, ContainerFlag> map = new HashMap<>();
			for(ContainerFlag type : values()) {
				map.put(type.xmlForm, type);
			}
			xmlLookup = map;
		}

		return xmlLookup.get(s);
	}
}