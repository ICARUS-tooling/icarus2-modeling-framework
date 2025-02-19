/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.registry;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;

/**
 * Models management of {@link MetadataRegistry} instances for individual objects
 * like corpora, contexts, etc. which can have their own dedicated registries,
 * use multiple shared ones or access a single client wide one.
 *
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface MetadataStoragePolicy<O extends Object> {

	/**
	 * Returns the {@link MetadataRegistry} to be used for the given {@code target}.
	 * A return value of {@code null} means the caller should decide upon the registry
	 * instance to be used.
	 * <p>
	 * Implementations should throw {@link ModelException}s when they encounter a situation
	 * where they suspect corrupted settings or missing information.
	 *
	 * @param manager
	 * @param hostRegistry
	 * @param object
	 * @return
	 */
	MetadataRegistry registryFor(CorpusManager manager, MetadataRegistry hostRegistry, O target);

	/**
	 * A shared global policy for corpus metadata that always returns {@code null}
	 */
	public static final MetadataStoragePolicy<CorpusManifest> emptyCorpusMetadataStoragePolicy = (m,r,c) -> null;
	public static final MetadataStoragePolicy<ContextManifest> emptyContextMetadataStoragePolicy = (m,r,c) -> null;
}
