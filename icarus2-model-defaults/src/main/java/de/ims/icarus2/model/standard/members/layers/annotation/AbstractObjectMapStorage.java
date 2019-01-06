/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.layers.annotation;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractObjectMapStorage<B extends Object> extends AbstractAnnotationStorage {

	/**
	 * Maps items to their respective buffer of annotation values.
	 */
	private Map<Item, B> annotations;

	public AbstractObjectMapStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	protected Map<Item, B> buildMap(AnnotationLayer layer) {

		if(isWeakKeys()) {
			return new WeakHashMap<>(getInitialCapacity(layer));
		} else {
			return new Object2ObjectOpenHashMap<>(getInitialCapacity(layer));
		}
	}

	protected Map<Item, B> getAnnotations() {
		return annotations;
	}

	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		annotations = buildMap(layer);
	}

	@Override
	public void removeNotify(AnnotationLayer layer) {
		super.removeNotify(layer);

		annotations = null;
	}

	protected B getBuffer(Item item) {
		return annotations.get(item);
	}

	protected B getBuffer(Item item, boolean createIfMissing) {
		B buffer = annotations.get(item);

		if(buffer==null) {
			buffer = createBuffer();
			annotations.put(item, buffer);
		}

		return buffer;
	}

	@Override
	public void removeAllValues() {
		annotations.clear();
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues(java.util.function.Supplier)
	 */
	@Override
	public void removeAllValues(Supplier<? extends Item> source) {
		Item item;
		while((item=source.get())!=null) {
			annotations.remove(item);
		}
	}

	@Override
	public boolean hasAnnotations() {
		return !annotations.isEmpty();
	}

	@Override
	public boolean hasAnnotations(Item item) {
		return annotations.containsKey(item);
	}

	@Override
	public boolean containsItem(Item item) {
		return annotations.containsKey(item);
	}

	protected abstract B createBuffer();

	@Override
	public boolean addItem(Item item) {
		// Cannot use putIfAbsent because of buffer creation
		if(!annotations.containsKey(item)) {
			B buffer = createBuffer();
			if(buffer!=null) {
				annotations.put(item, buffer);
			}
		}

		return false;
	}

	@Override
	public boolean removeItem(Item item) {
		return annotations.remove(item)!=null;
	}

}
