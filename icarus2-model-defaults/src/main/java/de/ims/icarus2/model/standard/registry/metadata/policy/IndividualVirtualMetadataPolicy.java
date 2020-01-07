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
import de.ims.icarus2.model.standard.registry.metadata.VirtualMetadataRegistry;
import de.ims.icarus2.util.annotations.TestableImplementation;

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
@TestableImplementation(MetadataStoragePolicy.class)
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
