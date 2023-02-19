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

import de.ims.icarus2.model.api.members.item.Edge;

/**
 * @author Markus Gärtner
 *
 */
public class AbstractEdgeAssert<A extends AbstractEdgeAssert<A>> extends AbstractItemAssert<A, Edge> {

	protected AbstractEdgeAssert(Edge actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public A sourceIsNull() {
		isNotNull();
		if(actual.getSource()!=null)
			throw failure("Source terminal expected to be null, but was %s", actual.getSource());
		return myself;
	}

	public A targetIsNull() {
		isNotNull();
		if(actual.getTarget()!=null)
			throw failure("Target terminal expected to be null, but was %s", actual.getTarget());
		return myself;
	}

	public A sourceIsNotNull() {
		isNotNull();
		if(actual.getSource()==null)
			throw failure("Source terminal expected not to be null");
		return myself;
	}

	public A targetIsNotNull() {
		isNotNull();
		if(actual.getTarget()==null)
			throw failure("Target terminal expected not to be null");
		return myself;
	}

	public A isLoop() {
		sourceIsNotNull();
		targetIsNotNull();
		if(actual.getSource() != actual.getTarget())
			throw failure("Edge is not a loop: %s -> %s", actual.getSource(), actual.getTarget());
		return myself;
	}

	public A isNoLoop() {
		sourceIsNotNull();
		targetIsNotNull();
		if(actual.getSource() == actual.getTarget())
			throw failure("Edge is a loop: %s -> %s", actual.getSource(), actual.getTarget());
		return myself;
	}

	//TODO
}
