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
package de.ims.icarus2.model.api.members;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.Mutable.MutableObject;

/**
 * Provides a facility that stores and manages locks for arbitrary members
 * of the modeling framework.
 *
 * @author Markus Gärtner
 *
 */
public class MemberLockSupport {

	private Consumer<Runnable> getProtector(Item item) {
		//TODO implement
		throw new UnsupportedOperationException("not implemented");
	}

	public void doProtected(Item item, Runnable job) {
		getProtector(item).accept(job);
	}

	public <T extends Object> T doProtected(Item item, Supplier<T> job) {
		MutableObject<T> buffer = new MutableObject<>();
		getProtector(item).accept(() -> buffer.set(job.get()));
		return buffer.get();
	}

	public <I extends Item, T extends Object> T doProtected(I item, Function<I, T> job) {
		MutableObject<T> buffer = new MutableObject<>();
		getProtector(item).accept(() -> buffer.set(job.apply(item)));
		return buffer.get();
	}

	//TODO add a mechanism to handle lambdas that throw exceptions
}
