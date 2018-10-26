/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import java.net.URL;

import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec;
import de.ims.icarus2.util.LazyNameStore;
import de.ims.icarus2.util.strings.StringResource;

/**
 * @author Markus Gärtner
 *
 */
public enum LocationType implements StringResource {

	/**
	 * Specifies that a certain location denotes a local file object
	 * accessible via a simple path string.
	 */
	LOCAL("local"),

	/**
	 * Marks a location as remotely accessible via a dedicated {@link URL}
	 */
	REMOTE("remote"),

	/**
	 * The location describes a remote or local service which should be used
	 * to access data. Typically this type of location requires additional
	 * {@link ModuleSpec} specifications in a driver manifest to define the
	 * interface to the service.
	 */
	SERVICE("service"),

	/**
	 * Locations with this type denote a database of arbitrary implementation.
	 * It is up to the {@link ResourcePath} or {@link LocationManifest} to provide
	 * additional information to properly access the database.
	 */
	DATABASE("database");

	private final String xmlForm;

	/**
	 * @param xmlForm
	 */
	private LocationType(String xmlForm) {
		this.xmlForm = xmlForm;
	}

	private static final LazyNameStore<LocationType> store = new LazyNameStore<>(LocationType.class);

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return xmlForm;
	}

	public static LocationType parseLocationType(String s) {
		return store.lookup(s);
	}
}
