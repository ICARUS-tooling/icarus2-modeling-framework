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
package de.ims.icarus2.model.standard.members.layer.annotation.single;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.mem.ByteAllocator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(AnnotationStorage.class)
public class SingleKeyStringStorage extends AbstractSingleKeyStorage {

	private Map<Item, String> annotations;
	private String noEntryValue;

	/** Placeholder for the addItem() method to reserve storage */
	private static final String DUMMY = "";

	public SingleKeyStringStorage(String annotationKey) {
		this(annotationKey, -1);
	}

	public SingleKeyStringStorage(String annotationKey, int initialCapacity) {
		this(annotationKey, false, initialCapacity);
	}

	public SingleKeyStringStorage(String annotationKey, boolean weakKeys, int initialCapacity) {
		super(annotationKey, weakKeys, initialCapacity);
	}

	protected Map<Item, String> buildBuffer(AnnotationLayer layer) {

		if(isWeakKeys()) {
			return new WeakHashMap<>(getInitialCapacity(layer));
		}

		//TODO evaluate which map implementation to use!!!
		return new Object2ObjectOpenHashMap<>(getInitialCapacity(layer));
	}

	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		AnnotationLayerManifest manifest = layer.getManifest();
		String key = getAnnotationKey();
		AnnotationManifest annotationManifest = requireAnnotationsManifest(manifest, key);

		noEntryValue = annotationManifest.getNoEntryValue()
				.map(CharSequence.class::cast)
				.map(CharSequence::toString)
				.orElse(null);
		annotations = buildBuffer(layer);
	}

	@Override
	public void removeNotify(@Nullable AnnotationLayer layer) {
		super.removeNotify(layer);

		noEntryValue = null;
		annotations = null;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getValue(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int)
	 */
	@Override
	public Object getValue(Item item, String key) {
		return getString(item, key);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractManagedAnnotationStorage#getString(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public String getString(Item item, String key) {
		checkKey(key);
		requireNonNull(item);

		String value = annotations.get(item);

		if(value==null || value==DUMMY) {
			value = noEntryValue;
		}

		return value;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, @Nullable Object value) {
		if(value==null) {
			setString(item, key, null);
		} else {
			checkArgument("Can only store CharSequence objects", CharSequence.class.isInstance(value));
			setString(item, key, ((CharSequence)value).toString());
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layer.annotation.AbstractManagedAnnotationStorage#setString(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.String)
	 */
	@Override
	public void setString(Item item, String key, @Nullable String value) {
		checkKey(key);
		requireNonNull(item);

		if(value==null || value==noEntryValue) {
			annotations.remove(item);
		} else {
			annotations.put(item, value);
		}
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
		requireNonNull(item);
		return annotations.containsKey(item);
	}

	@Override
	public boolean removeItem(Item item) {
		requireNonNull(item);
		return annotations.remove(item)!=null;
	}

	@Override
	public boolean addItem(Item item) {
		requireNonNull(item);
		return annotations.putIfAbsent(item, DUMMY)==null;
	}

	@Override
	public boolean containsItem(Item item) {
		requireNonNull(item);
		return annotations.containsKey(item);
	}

	public Object getNoEntryValue() {
		return noEntryValue;
	}
}
