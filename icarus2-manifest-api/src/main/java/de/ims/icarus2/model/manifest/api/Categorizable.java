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
package de.ims.icarus2.model.manifest.api;

import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
public interface Categorizable<C extends Categorizable<C>> extends Lockable {

	boolean addCategory(Category category);

	@SuppressWarnings("unchecked")
	default C addCategories(Category...categories) {
		for(Category category : categories) {
			addCategory(category);
		}

		return (C) this;
	}

	boolean removeCategory(Category category);

	@SuppressWarnings("unchecked")
	default C removeCategories(Category...categories) {
		for(Category category : categories) {
			removeCategory(category);
		}

		return (C) this;
	}

	void forEachCategory(Consumer<? super Category> action);

	default Set<Category> getCategories() {
		LazyCollection<Category> result = LazyCollection.lazySet();
		forEachCategory(result);
		return result.getAsSet();
	}

	default boolean hasCategory(Category category) {
		MutableBoolean result = new MutableBoolean(false);
		forEachCategory(c -> {
			if(c.equals(category))
				result.setBoolean(true);
		});
		return result.booleanValue();
	}
}
