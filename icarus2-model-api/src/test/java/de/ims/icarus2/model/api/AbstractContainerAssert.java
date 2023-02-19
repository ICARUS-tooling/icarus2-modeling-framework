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
/**
 *
 */
package de.ims.icarus2.model.api;

import de.ims.icarus2.model.api.members.container.Container;

/**
 * @author Markus Gärtner
 *
 */
public class AbstractContainerAssert<A extends AbstractContainerAssert<A, C>, C extends Container> extends AbstractItemAssert<A,C> {

	protected AbstractContainerAssert(C actual, Class<?> selfType) {
		super(actual, selfType);
	}

	@SuppressWarnings("boxing")
	public A isEmpty() {
		isNotNull();
		if(actual.getItemCount()>0)
			throw failure("Expected container %s to be empty, but contains %d items", actual, actual.getItemCount());
		return myself;
	}

	public A isNotEmpty() {
		isNotNull();
		if(actual.getItemCount()==0)
			throw failure("Expected container %s not to be empty", actual);
		return myself;
	}

	@SuppressWarnings("boxing")
	public A hasItemCountOf(long expected) {
		isNotNull();
		long size = actual.getItemCount();
		if(size!=expected)
			throw failureWithActualExpected(size, expected, "Expected container %s to have size %d, but got %d", actual, expected, size);
		return myself;
	}

	//TODO
}
