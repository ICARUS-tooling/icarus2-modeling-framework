/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.List;

import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.api.registry.MetadataStoragePolicy;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * Implements a delegating metadata policy that takes a priority list of actual policies
 * and when asked for a registry instance forwards the request. It iterates over the ordered
 * list of prioritized policies and uses the first valid registry as result instance.
 * <p>
 * This allows client code to be very flexible with regards to metadata storage for individual
 * corpora or even drivers.
 *
 * @author Markus Gärtner
 *
 * @param <O>
 */
@TestableImplementation(MetadataStoragePolicy.class)
public class PriorityListMetadataPolicy<O extends Object> implements MetadataStoragePolicy<O> {

	@SuppressWarnings("rawtypes")
	private final MetadataStoragePolicy[] policies;

	public PriorityListMetadataPolicy(MetadataStoragePolicy<? extends O>[] policies) {
		requireNonNull(policies);
		checkArgument(policies.length>0);

		this.policies = new MetadataStoragePolicy[policies.length];

		for(int i=0; i<policies.length; i++) {
			this.policies[i] = (MetadataStoragePolicy<? extends O>) policies[i];
		}
	}

	public PriorityListMetadataPolicy(List<MetadataStoragePolicy<? extends O>> policies) {
		requireNonNull(policies);
		checkArgument(!policies.isEmpty());

		this.policies = new MetadataStoragePolicy[policies.size()];

		for(int i=0; i<policies.size(); i++) {
			this.policies[i] = (MetadataStoragePolicy<? extends O>) policies.get(i);
		}
	}

	/**
	 * Iterates over the internal priority list of policies and uses the first result
	 * other than {@code null}.
	 *
	 * @see de.ims.icarus2.model.api.registry.MetadataStoragePolicy#registryFor(de.ims.icarus2.model.api.registry.CorpusManager, de.ims.icarus2.model.api.registry.MetadataRegistry, java.lang.Object)
	 */
	@Override
	public MetadataRegistry registryFor(CorpusManager manager,
			MetadataRegistry hostRegistry, O target) {

		MetadataRegistry result = null;

		for(int i=0; i<policies.length; i++) {

			@SuppressWarnings("unchecked")
			MetadataStoragePolicy<O> policy = (MetadataStoragePolicy<O>) policies[i];
			result = policy.registryFor(manager, hostRegistry, target);

			// Stop as soon as we get a valid registry
			if(result!=null) {
				break;
			}
		}

		return null;
	}

}