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
package de.ims.icarus2.model.standard.members.layers.annotation.single;

import java.util.Set;
import java.util.function.Supplier;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
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

	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		AnnotationLayerManifest manifest = layer.getManifest();
		AnnotationManifest annotationManifest = manifest.getAnnotationManifest(manifest.getDefaultKey());

		Object declaredNoEntryValue = annotationManifest.getNoEntryValue();

		noEntryValueSet = declaredNoEntryValue!=null;
		noEntryValue = declaredNoEntryValue==null ? DEFAULT_NO_ENTRY_VALUE : (boolean) declaredNoEntryValue;

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
		return getBooleanValue(item, key);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		setBooleanValue(item, key, (boolean) value);
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
