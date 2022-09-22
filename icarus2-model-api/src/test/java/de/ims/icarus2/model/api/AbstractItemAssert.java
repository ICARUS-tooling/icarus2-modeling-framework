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
/**
 *
 */
package de.ims.icarus2.model.api;

import org.assertj.core.api.AbstractAssert;

import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
public class AbstractItemAssert<A extends AbstractItemAssert<A,I>, I extends Item> extends AbstractAssert<A, I> {

	protected AbstractItemAssert(I actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public A containerIsNull() {
		isNotNull();
		if(actual.getContainer()!=null)
			throw failure("Container expected to be null, but was %s", actual.getContainer());
		return myself;
	}

	public A containerIsNotNull() {
		isNotNull();
		if(actual.getContainer()==null)
			throw failure("Container expected not to be null");
		return myself;
	}

	//TODO
}
