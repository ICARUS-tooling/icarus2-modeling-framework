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
 */
package de.ims.icarus2.model.standard.registry.metadata.policy;

import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.api.registry.MetadataStoragePolicy;
import de.ims.icarus2.model.api.registry.SubRegistry;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ManifestFragment;

/**
 * Implements a metadata policy for targets describable by a {@link ManifestFragment}.
 * The returned registry instance will always be a fresh {@link SubRegistry} of the
 * supplied host registry with the {@link ManifestFragment#getId() id} of the manifest
 * used as prefix.
 *
 * @author Markus Gärtner
 *
 */
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
		return target.getId();
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
