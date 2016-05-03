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

 * $Revision: 447 $
 * $Date: 2016-01-14 11:34:47 +0100 (Do, 14 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/ValueManifest.java $
 *
 * $LastChangedDate: 2016-01-14 11:34:47 +0100 (Do, 14 Jan 2016) $
 * $LastChangedRevision: 447 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.api;

import de.ims.icarus2.model.manifest.types.ValueType;


/**
 * A helper class that wraps a value and provides additional textual information
 * like a description and an optional name. The purpose of those strings is so
 * that user interfaces can provide the user with information about the available
 * values for an option or annotation.
 *
 * @author Markus Gärtner
 * @version $Id: ValueManifest.java 447 2016-01-14 10:34:47Z mcgaerty $
 *
 */
public interface ValueManifest extends Documentable, TypedManifest {

	/**
	 * Returns the value this manifest wraps and describes.
	 *
	 * @return
	 */
	Object getValue();

	/**
	 * Returns the (optional) name for this value, which is not required to be
	 * localized.
	 * <p>
	 * This is an optional method.
	 *
	 * @return The name of this value or {@code null} if the value is unnamed
	 */
	String getName();

	/**
	 * Returns the (preferably localized) textual description of this value.
	 *
	 * @return A textual description of this value
	 */
	String getDescription();

	ValueType getValueType();

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifest#getManifestType()
	 */
	@Override
	default public ManifestType getManifestType() {
		return ManifestType.VALUE_MANIFEST;
	}

	// Modification methods

	void setValue(Object value);
	void setName(String name);
	void setDescription(String description);

}