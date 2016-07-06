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
	CONTAINER_MANIFEST,
	STRUCTURE_MANIFEST,
	ANNOTATION_MANIFEST,
	ANNOTATION_LAYER_MANIFEST,
	ITEM_LAYER_MANIFEST,
	STRUCTURE_LAYER_MANIFEST,
	FRAGMENT_LAYER_MANIFEST,
	HIGHLIGHT_LAYER_MANIFEST,
	LOCATION_MANIFEST,
	OPTIONS_MANIFEST,
	CONTEXT_MANIFEST,
	CORPUS_MANIFEST,
	PATH_RESOLVER_MANIFEST,
	RASTERIZER_MANIFEST,
	DRIVER_MANIFEST,
	IMPLEMENTATION_MANIFEST,
	LAYER_GROUP_MANIFEST,

	MODULE_MANIFEST,
	MODULE_SPEC,
	MAPPING_MANIFEST,
	OPTION,

	VALUE_RANGE,
	VALUE_SET,
	VALUE_MANIFEST,
	DOCUMENTATION,
	VERSION,

	/**
	 * Reserved manifest type for use in testing.
	 * Client code is free to throw an exception whenever this type is
	 * encountered during runtime.
	 */
	DUMMY_MANIFEST,
	;

}
