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
package de.ims.icarus2.filedriver.schema.resolve.common;

import java.util.function.ObjLongConsumer;

import de.ims.icarus2.filedriver.ComponentSupplier;
import de.ims.icarus2.filedriver.schema.resolve.MappingHandler;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache;

class ProxyContext implements ResolverContext {
	private ResolverContext source;
	private CharSequence data;

	@Override
	public ItemLayer getPrimaryLayer() {
		return source.getPrimaryLayer();
	}
	@Override
	public Container currentContainer() {
		return source.currentContainer();
	}
	@Override
	public long currentIndex() {
		return source.currentIndex();
	}
	@Override
	public Item currentItem() {
		return source.currentItem();
	}
	@Override
	public CharSequence rawData() {
		return data;
	}
	public void reset(ResolverContext source, CharSequence data) {
		this.source = source;
		this.data = data;
	}
	@Override
	public ObjLongConsumer<? super Item> getTopLevelAction() {
		return source.getTopLevelAction();
	}
	@Override
	public ComponentSupplier getComponentSupplier(ItemLayer layer) {
		return source.getComponentSupplier(layer);
	}
	@Override
	public InputCache getCache(ItemLayer layer) {
		return source.getCache(layer);
	}
	@Override
	public MappingHandler createMappingHandler(ItemLayer parent, ItemLayer layer) {
		return source.createMappingHandler(parent, layer);
	}
}