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

/**
 * @author Markus Gärtner
 *
 */
public enum ManifestType {
	CONTAINER_MANIFEST(true),
	STRUCTURE_MANIFEST(true),
	ANNOTATION_MANIFEST(true),
	ANNOTATION_LAYER_MANIFEST(true),
	ITEM_LAYER_MANIFEST(true),
	STRUCTURE_LAYER_MANIFEST(true),
	FRAGMENT_LAYER_MANIFEST(true),
	HIGHLIGHT_LAYER_MANIFEST(true),

	LOCATION_MANIFEST(false),
	OPTIONS_MANIFEST(true),
	CONTEXT_MANIFEST(true),
	CORPUS_MANIFEST(true),
	PATH_RESOLVER_MANIFEST(true),
	RASTERIZER_MANIFEST(true),
	DRIVER_MANIFEST(true),
	IMPLEMENTATION_MANIFEST(true),
	LAYER_GROUP_MANIFEST(false),

	MODULE_MANIFEST(true),
	MODULE_SPEC(false),
	MAPPING_MANIFEST(false),
	OPTION(false),

	VALUE_RANGE(false),
	VALUE_SET(false),
	VALUE_MANIFEST(false),
	DOCUMENTATION(false),
	VERSION(false),

	/**
	 * Reserved manifest type for use in testing.
	 * Client code is free to throw an exception whenever this type is
	 * encountered during runtime.
	 */
	DUMMY_MANIFEST(false),
	;

	private final boolean supportTemplating;

	ManifestType(boolean supportTemplating) {
		this.supportTemplating = supportTemplating;
	}

	public boolean isSupportTemplating() {
		return supportTemplating;
	}
}
