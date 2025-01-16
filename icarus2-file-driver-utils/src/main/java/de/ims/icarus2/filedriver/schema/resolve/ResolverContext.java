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
package de.ims.icarus2.filedriver.schema.resolve;

import java.util.function.ObjLongConsumer;

import javax.annotation.Nullable;

import de.ims.icarus2.filedriver.ComponentSupplier;
import de.ims.icarus2.filedriver.schema.resolve.common.DependencyStructureResolver;
import de.ims.icarus2.filedriver.schema.tabular.TableConverter;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache;

/**
 * Interface between an instance of {@link TableConverter} or similar framework member
 * and individual {@link Resolver resolvers}. An instance of this context holds all the
 * information and state of the active (de)serialization process.
 *
 * @author Markus Gärtner
 *
 */
public interface ResolverContext {

	ItemLayer getPrimaryLayer();

	/**
	 * Returns the currently active host container
	 *
	 * @return
	 */
	Container currentContainer();

	/**
	 * Returns the index within the current container that reading is taking place at
	 *
	 * @return
	 */
	long currentIndex();

	/**
	 * Returns the item with which currently read data should be associated
	 *
	 * @return
	 */
	Item currentItem();

	/**
	 * Returns the current raw input data that should be read and converted
	 *
	 * @return
	 */
	CharSequence rawData();

	/**
	 * Tells the context that the current raw input line should be treated as being consumed,
	 * meaning that the code controlling the read operation must not pass it on to other
	 * subsequent tasks.
	 */
	default void consumeData() {
		// no-op
	}

	/**
	 * Returns the singular handler for lop-level members of the primary layer.
	 */
	@Nullable
	ObjLongConsumer<? super Item> getTopLevelAction();

	/**
	 * Returns a {@link ComponentSupplier} that is pre-configured for the given
	 * {@link ItemLayer layer}. Note that the returned supplier should be considered
	 * mutually exclusive with a {@link #getCache(ItemLayer)} obtained for the same
	 * layer. This is due to the fact that component suppliers are expected to be
	 * configured such that they already populate the underlying cache for their
	 * respective backing layer(s).
	 */
	ComponentSupplier getComponentSupplier(ItemLayer layer);

	/**
	 * Returns the {@link InputCache} associated with the given {@link ItemLayer layer}.
	 * This cache can be used to store members for the layer that have been created "outside"
	 * the core framework. For instance, the {@link DependencyStructureResolver} produces
	 * {@link Structure} instances that represent dependency trees on its own, but still
	 * has to register them with the underlying storage via the respective {@link InputCache cache}.
	 * <p>
	 * Note that this approach should <b>only</b> be used when a resolver or other
	 * component does <b>not</b> obtain its corpus members from a {@link #getComponentSupplier(ItemLayer) component supplier}!
	 * Otherwise members could potentially be added to storage multiple times and also
	 * metadata colelcted during scanning will be corrupted.
	 */
	InputCache getCache(ItemLayer layer);

	/**
	 * Creates a handler that will feed into a {@link Mapping} from {@code parent} to {@code layer}.
	 * If {@code parent} is {@code null}, this method will also return {@code null}.
	 * @param parent
	 * @param layer
	 * @return
	 */
	@Nullable MappingHandler createMappingHandler(@Nullable ItemLayer parent, ItemLayer layer);
}
