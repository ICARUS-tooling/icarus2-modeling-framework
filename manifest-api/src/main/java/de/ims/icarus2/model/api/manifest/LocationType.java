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

 * $Revision: 445 $
 * $Date: 2016-01-11 17:33:05 +0100 (Mo, 11 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/io/LocationType.java $
 *
 * $LastChangedDate: 2016-01-11 17:33:05 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 445 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.manifest;

import java.net.URL;

import de.ims.icarus2.model.api.manifest.DriverManifest.ModuleSpec;
import de.ims.icarus2.model.util.StringResource;

/**
 * @author Markus Gärtner
 * @version $Id: LocationType.java 445 2016-01-11 16:33:05Z mcgaerty $
 *
 */
public enum LocationType implements StringResource {

	/**
	 * Specifies that a certain location denotes a local file object
	 * accessible via a simple path string.
	 */
	LOCAL,

	/**
	 * Marks a location as remotely accessible via a dedicated {@link URL}
	 */
	REMOTE,

	/**
	 * The location describes a remote or local service which should be used
	 * to access data. Typically this type of location requires additional
	 * {@link ModuleSpec} specifications in a driver manifest to define the
	 * interface to the service.
	 */
	SERVICE,

	/**
	 * Locations with this type denote a database of arbitrary implementation.
	 * It is up to the {@link ResourcePath} or {@link LocationManifest} to provide
	 * additional information to properly access the database.
	 */
	DATABASE;

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return name().toLowerCase();
	}

	public static LocationType parseLocationType(String s) {
		return valueOf(s.toUpperCase());
	}
}
