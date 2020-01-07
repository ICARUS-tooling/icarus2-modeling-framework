/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.registry.metadata.policy;

import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.api.registry.MetadataStoragePolicy;
import de.ims.icarus2.model.api.registry.SubRegistry;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ManifestFragment;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * Implements a metadata policy for targets describable by a {@link ManifestFragment}.
 * The returned registry instance will always be a fresh {@link SubRegistry} of the
 * supplied host registry with the {@link ManifestFragment#getId() id} of the manifest
 * used as prefix.
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(MetadataStoragePolicy.class)
public class PrefixMetadataPolicy<M extends ManifestFragment> implements MetadataStoragePolicy<M> {

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataStoragePolicy#registryFor(de.ims.icarus2.model.api.registry.CorpusManager, de.ims.icarus2.model.api.registry.MetadataRegistry, java.lang.Object)
	 */
	@Override
	public MetadataRegistry registryFor(CorpusManager manager,
			MetadataRegistry hostRegistry, M target) {
		return new SubRegistry(hostRegistry, getRegistryPrefix(target));
	}

	/**
	 * Returns the prefix used to instantiate a new {@link SubRegistry}.
	 * This method is only called from within {@link #registryFor(CorpusManager, MetadataRegistry, ManifestFragment)}
	 * and the default implementation simply returns the {@link ManifestFragment#getId() id}
	 * of the supplied {@link ManifestFragment}.
	 *
	 * @param target
	 * @return
	 */
	protected String getRegistryPrefix(M target) {
		return ManifestUtils.requireId(target);
	}

	/**
	 * Global sharable metadata policy for {@link CorpusManifest corpora} that uses the corpus' id as
	 * prefix to create a new {@link SubRegistry} of the provided host registry.
	 */
	public static final MetadataStoragePolicy<CorpusManifest> sharedCorpusMetadataPolicy = new PrefixMetadataPolicy<>();

	/**
	 * Global sharable metadata policy for {@link ContextManifest drivers} that uses the context's id
	 * as prefix to create a new {@link SubRegistry} of the provided host registry.
	 */
	public static final MetadataStoragePolicy<ContextManifest> sharedContextMetadataPolicy = new PrefixMetadataPolicy<>();
}
