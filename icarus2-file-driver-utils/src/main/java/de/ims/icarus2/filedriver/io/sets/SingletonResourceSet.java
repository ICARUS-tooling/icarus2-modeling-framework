/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.io.sets;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(ResourceSet.class)
public final class SingletonResourceSet implements ResourceSet {

	private final IOResource resource;

	/**
	 * Creates a new {@code SingletonResourceSet} that points to the given {@code resource}.
	 *
	 * @param resource
	 * @param storage
	 */
	public SingletonResourceSet(IOResource resource) {
		requireNonNull(resource);

		this.resource = resource;
	}

	@Override
	public String toString() {
		return "SingletonResourceSet [resource="+resource+"]";
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.ResourceSet#getResourceCount()
	 */
	@Override
	public int getResourceCount() {
		return 1;
	}

	private void checkIndex(int resourceIndex) {
		if(resourceIndex!=0)
			throw new IllegalArgumentException("Invalid resource index: "+resourceIndex+" - only legal value is 0");
	}

	/**
	 * @see de.ims.icarus2.filedriver.io.sets.ResourceSet#getResourceAt(int)
	 */
	@Override
	public IOResource getResourceAt(int resourceIndex) {
		checkIndex(resourceIndex);

		return resource;
	}
}
