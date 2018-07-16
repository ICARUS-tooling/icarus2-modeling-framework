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
import de.ims.icarus2.model.standard.registry.metadata.VirtualMetadataRegistry;

/**
 * Implements a metadata policy that creates a fresh new {@link VirtualMetadataRegistry} for every
 * target object.
 * <p>
 * Note that this kind of policy is unsuited for actual production environments, since all the stored
 * metadata will be lost once the registry is {@link MetadataRegistry#close() shut down}.
 *
 * @author Markus Gärtner
 *
 */
public class IndividualVirtualMetadataPolicy<O extends Object> implements MetadataStoragePolicy<O> {

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataStoragePolicy#registryFor(de.ims.icarus2.model.api.registry.CorpusManager, de.ims.icarus2.model.api.registry.MetadataRegistry, java.lang.Object)
	 */
	@Override
	public MetadataRegistry registryFor(CorpusManager manager,
			MetadataRegistry hostRegistry, O target) {
		return new VirtualMetadataRegistry();
	}

}
