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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.List;

import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.api.registry.MetadataStoragePolicy;

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