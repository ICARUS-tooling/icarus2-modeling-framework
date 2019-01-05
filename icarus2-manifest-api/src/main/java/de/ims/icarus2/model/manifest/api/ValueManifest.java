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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import de.ims.icarus2.model.manifest.types.ValueType;


/**
 * A helper class that wraps a value and provides additional textual information
 * like a description and an optional name. The purpose of those strings is so
 * that user interfaces can provide the user with information about the available
 * values for an option or annotation.
 *
 * @author Markus Gärtner
 *
 */
public interface ValueManifest extends Documentable<ValueManifest>, ModifiableIdentity, TypedManifest {


	public static final Set<ValueType> SUPPORTED_VALUE_TYPES =
				Collections.unmodifiableSet(ValueType.filterWithout(
			true, // basic types only
			ValueType.UNKNOWN,
			ValueType.CUSTOM,
			ValueType.IMAGE_RESOURCE,
			ValueType.URL_RESOURCE));

	/**
	 * Returns the value this manifest wraps and describes.
	 *
	 * @return
	 */
	Optional<Object> getValue();

	ValueType getValueType();

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifest#getManifestType()
	 */
	@Override
	default public ManifestType getManifestType() {
		return ManifestType.VALUE_MANIFEST;
	}

	// Modification methods

	ValueManifest setValue(Object value);
}