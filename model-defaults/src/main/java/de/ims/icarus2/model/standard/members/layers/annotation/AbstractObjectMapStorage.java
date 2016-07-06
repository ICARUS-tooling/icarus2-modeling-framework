/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 */
package de.ims.icarus2.model.standard.members.layers.annotation;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractObjectMapStorage<B extends Object> extends AbstractAnnotationStorage {

	/**
	 * Maps items to their respective array of annotation values.
	 */
	private Map<Item, B> annotations;

	public AbstractObjectMapStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	protected Map<Item, B> buildMap(AnnotationLayer layer) {

		if(isWeakKeys()) {
			return new WeakHashMap<>(getInitialCapacity(layer));
		} else {
			return new HashMap<>(getInitialCapacity(layer));
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
