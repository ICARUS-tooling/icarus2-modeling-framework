/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
