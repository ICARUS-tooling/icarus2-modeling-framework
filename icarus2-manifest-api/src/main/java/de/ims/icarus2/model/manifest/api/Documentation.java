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
package de.ims.icarus2.model.manifest.api;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface Documentation extends Lockable, TypedManifest {

	@AccessRestriction(AccessMode.READ)
	Optional<String> getContent();

	@AccessRestriction(AccessMode.READ)
	default List<Resource> getResources() {
		LazyCollection<Resource> result = LazyCollection.lazyList();

		forEachResource(result);

		return result.getAsList();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifest#getManifestType()
	 */
	@Override
	default public ManifestType getManifestType() {
		return ManifestType.DOCUMENTATION;
	}

	/**
	 * Apply {@code action} to each registered resource in this documentation
	 * in the order they have been added.
	 * @param action
	 */
	void forEachResource(Consumer<? super Resource> action);

	// Modification methods

	Documentation setContent(@Nullable String content);

	Documentation addResource(Resource resource);

	Documentation removeResource(Resource resource);

	/**
	 * Links to additional resources that can be used for documentation
	 * purposes.
	 *
	 * @author Markus Gärtner
	 *
	 */
	@AccessControl(AccessPolicy.DENY)
	public interface Resource extends ModifiableIdentity, Lockable {

		@AccessRestriction(AccessMode.READ)
		URI getUri();

		Resource setUri(URI uri);
	}
}
