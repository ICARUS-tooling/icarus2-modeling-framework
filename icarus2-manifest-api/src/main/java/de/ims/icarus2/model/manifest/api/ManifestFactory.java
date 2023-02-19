/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nullable;

import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.Options;

/**
 * Factory for instantiating {@link ManifestFragment manifests} based on provided
 * {@link ManifestType} information.
 *
 * @author Markus Gärtner
 *
 */
public interface ManifestFactory {

	/**
	 * Key for storing a {@link ValueType} in the options for
	 * creation of a manifest.
	 */
	public static final String OPTION_VALUE_TYPE = "valueType";

	/**
	 * Returns all the {@link ManifestType types} this factory can instantiate.
	 * Note that this set must never be empty.
	 * <p>
	 * The default implementation simply returns all possible types in a set.
	 *
	 * @return
	 */
	default Set<ManifestType> getSupportedTypes() {
		return EnumSet.allOf(ManifestType.class);
	}

	/**
	 * @see #create(ManifestType, TypedManifest, Options)
	 *
	 * @param type
	 * @return
	 */
	default <M extends TypedManifest> M create(ManifestType type) {
		return create(type, null, null);
	}

	default <M extends TypedManifest> M create(ManifestType type, Class<M> clazz) {
		return create(type, null, null);
	}

	default <M extends TypedManifest> M create(Class<M> clazz) {
		return create(ManifestType.forClass(clazz), null, null);
	}

	default <M extends TypedManifest> M create(Class<M> clazz, Options options) {
		return create(ManifestType.forClass(clazz), null, options);
	}

	/**
	 * @see #create(ManifestType, TypedManifest, Options)
	 *
	 * @param type
	 * @param host
	 * @return
	 */
	default <M extends TypedManifest> M create(ManifestType type, TypedManifest host) {
		return create(type, host, null);
	}

	/**
	 * Instantiate a manifest object for the specified {@code type}. If the {@code host} argument is
	 * not {@code null}, it will be used as environment for the manifest.
	 * <p>
	 * Note that the {@code options} parameter is an important carrier for obligatory secondary
	 * constructor arguments, such as the {@link ValueType} for certain storage classes. In those
	 * cases the {@link #OPTION_VALUE_TYPE} key should be used to store the desired value type in
	 * the supplied {@link Options} object.
	 *
	 * @param type
	 * @param host
	 * @param options
	 * @return
	 */
	<M extends TypedManifest> M create(ManifestType type, @Nullable TypedManifest host, @Nullable Options options);

	/**
	 * Returns the {@link ManifestLocation location} that manifest objects instantiated
	 * by this factory will be assigned.
	 *
	 * @return
	 */
	ManifestLocation getManifestLocation();

	/**
	 * Returns the {@link ManifestRegistry registry} that manifest objects instantiated
	 * by this factory will be assigned.
	 *
	 * @return
	 */
	ManifestRegistry getRegistry();
}
