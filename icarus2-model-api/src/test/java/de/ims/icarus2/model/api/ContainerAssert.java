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
/**
 *
 */
package de.ims.icarus2.model.api;

import de.ims.icarus2.model.api.members.container.Container;

/**
 * @author Markus Gärtner
 *
 */
public class ContainerAssert extends AbstractContainerAssert<ContainerAssert, Container> {

	public ContainerAssert(Container actual) {
		super(actual, ContainerAssert.class);
	}

	@SuppressWarnings("boxing")
	public ItemAssert element(long index) {
		isNotNull();
		if(index>=actual.getItemCount())
			throw failure("Item index %d out of bounds. Container only holds %d items.", index, actual.getItemCount());
		return new ItemAssert(actual.getItemAt(index));
	}

	//TODO
}
