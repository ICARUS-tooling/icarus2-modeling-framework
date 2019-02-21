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
package de.ims.icarus2.model.standard.members.layers.annotation.single;

import static de.ims.icarus2.util.lang.Primitives._boolean;
import static de.ims.icarus2.util.lang.Primitives.cast;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.util.collections.WeakHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public class SingleKeyBooleanStorage extends AbstractSingleKeyStorage {

	private Set<Item> annotations;
	private boolean noEntryValue = DEFAULT_NO_ENTRY_VALUE;
	private boolean noEntryValueSet = false;

	public static final boolean DEFAULT_NO_ENTRY_VALUE = false;

	public SingleKeyBooleanStorage() {
		this(-1);
	}

	public SingleKeyBooleanStorage(int initialCapacity) {
		this(false, initialCapacity);
	}

	public SingleKeyBooleanStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	protected Set<Item> buildBuffer(AnnotationLayer layer) {
		if(isWeakKeys()) {
			//TODO expensive implementation, really use this one?
			return new WeakHashSet<>(getInitialCapacity(layer));
		} else {
			return new ObjectOpenHashSet<>(getInitialCapacity(layer));
		}
	}

	@SuppressWarnings("boxing")
	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		AnnotationLayerManifest manifest = layer.getManifest();
		String key = requireDefaultKey(manifest);
		AnnotationManifest annotationManifest = requireAnnotationsManifest(manifest, key);

		Optional<Object> declaredNoEntryValue = annotationManifest.getNoEntryValue();

		noEntryValueSet = declaredNoEntryValue.isPresent();
		noEntryValue = declaredNoEntryValue.map(Boolean.class::cast).orElse(DEFAULT_NO_ENTRY_VALUE).booleanValue();

		if(noEntryValueSet && noEntryValue)
			throw new ModelException(ManifestErrorCode.IMPLEMENTATION_ERROR,
					"Implementation does not support 'true' as 'neEntryValue'");

		annotations = buildBuffer(layer);
	}

	@Override
	public void removeNotify(AnnotationLayer layer) {
		super.removeNotify(layer);

		noEntryValue = DEFAULT_NO_ENTRY_VALUE;
		noEntryValueSet = false;
		annotations = null;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public Object getValue(Item item, String key) {
		return _boolean(getBooleanValue(item, key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		setBooleanValue(item, key, cast((Boolean)value));
	}

	@Override
	public boolean getBooleanValue(Item item, String key) {
		checkKey(key);

		boolean result = annotations.contains(item);

		if(!result && noEntryValueSet) {
			result = noEntryValue;
		}

		return result;
	}

	@Override
	public void setBooleanValue(Item item, String key, boolean value) {
		checkKey(key);

		if(!value || (noEntryValueSet && value==noEntryValue)) {
			annotations.remove(item);
		} else {
			annotations.add(item);
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
		return annotations.contains(item);
	}

	@Override
	public boolean removeItem(Item item) {
		return annotations.remove(item);
	}

	public boolean getNoEntryValue() {
		return noEntryValue;
	}
}
