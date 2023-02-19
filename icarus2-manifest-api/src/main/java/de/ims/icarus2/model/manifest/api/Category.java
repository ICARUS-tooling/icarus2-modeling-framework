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
package de.ims.icarus2.model.manifest.api;

import java.util.Objects;
import java.util.Optional;

import de.ims.icarus2.util.id.Identity;
import it.unimi.dsi.fastutil.Hash.Strategy;

/**
 * Link to a category or term definition.
 * The concept extends the basic idea of {@link Identity}
 * and adds a {@link #getNamespace() namespace} information
 * that is used to specify the source of the category
 * definition.
 *
 * @author Markus Gärtner
 *
 */
public interface Category extends Identity {

	/**
	 * Returns the source or context in which this category
	 * has been defined in.
	 *
	 * @return
	 */
	Optional<String> getNamespace();

	/**
	 * Returns the unique identifier of this category within
	 * its declared {@link #getNamespace() namespace}. Note that
	 * unlike the original contract of {@link Identity} the
	 * value returned by this method must follow the defined
	 * vocabulary of the designated {@link #getNamespace() namespace}!
	 */
	@Override
	Optional<String> getId();

	public static final Strategy<Category> HASH_STRATEGY = new Strategy<Category>() {

		@Override
		public int hashCode(Category cat) {
			return cat==null ? 0 : 1+Objects.hash(cat.getId(), cat.getNamespace());
		}

		@Override
		public boolean equals(Category cat0, Category cat1) {
			if(cat0==null || cat1==null) {
				return cat0==cat1;
			}

			return Objects.equals(cat0.getId(), cat1.getId())
					&& Objects.equals(cat0.getNamespace(), cat1.getNamespace());
		}
	};
}
