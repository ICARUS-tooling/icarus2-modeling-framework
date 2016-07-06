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
package de.ims.icarus2.model.api.members;

import de.ims.icarus2.util.strings.StringResource;

/**
 * Defines the possibles types a {@link CorpusMember} can declare to represent by
 * its {@link CorpusMember#getMemberType()} method. Note although that a class can implement
 * multiple interfaces of the corpus framework, it can only ever be assigned to exactly one
 * <i>member role</i> specified by its {@code MemberType}.
 *
 * @author Markus Gärtner
 *
 */
public enum MemberType implements StringResource {
	FRAGMENT,
	ITEM,
	EDGE,
	CONTAINER,
	STRUCTURE,
	LAYER, // No distinction between different layer types. they are defined by the manifest type
	CONTEXT,
	;

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return name();
	}
}
