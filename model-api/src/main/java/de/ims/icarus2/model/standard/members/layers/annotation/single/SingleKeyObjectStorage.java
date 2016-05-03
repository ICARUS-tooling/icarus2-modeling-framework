/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 407 $
 * $Date: 2015-06-24 02:12:18 +0200 (Mi, 24 Jun 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/layers/annotation/single/SingleKeyObjectStorage.java $
 *
 * $LastChangedDate: 2015-06-24 02:12:18 +0200 (Mi, 24 Jun 2015) $
 * $LastChangedRevision: 407 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.layers.annotation.single;

import gnu.trove.map.hash.THashMap;

import java.util.Map;
import java.util.WeakHashMap;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus2.model.api.manifest.AnnotationManifest;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 * @version $Id: SingleKeyObjectStorage.java 407 2015-06-24 00:12:18Z mcgaerty $
 *
 */
public class SingleKeyObjectStorage extends AbstractSingleKeyStorage {

	private Map<Item, Object> annotations;
	private Object noEntryValue;

	public SingleKeyObjectStorage() {
		this(-1);
	}

	public SingleKeyObjectStorage(int initialCapacity) {
		this(false, initialCapacity);
	}

	public SingleKeyObjectStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	protected Map<Item, Object> buildBuffer(AnnotationLayer layer) {

		if(isWeakKeys()) {
			return new WeakHashMap<>(getInitialCapacity(layer));
		} else {
			//TODO evaluate which map implementation to use!!!
			return new THashMap<>(getInitialCapacity(layer));
		}
	}

	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		AnnotationLayerManifest manifest = layer.getManifest();
		AnnotationManifest annotationManifest = manifest.getAnnotationManifest(manifest.getDefaultKey());

		noEntryValue = annotationManifest.getNoEntryValue();
		annotations = buildBuffer(layer);
	}

	@Override
	public void removeNotify(AnnotationLayer layer) {
		super.removeNotify(layer);

		noEntryValue = null;
		annotations = null;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public Object getValue(Item item, String key) {
		checkKey(key);

		Object value = annotations.get(item);

		if(value==null) {
			value = noEntryValue;
		}

		return value;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		checkKey(key);

		if(value==null || value==noEntryValue) {
			annotations.remove(item);
		} else {
			annotations.put(item, value);
		}
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
	public boolean removeItem(Item item) {
		return annotations.remove(item)!=null;
	}

	public Object getNoEntryValue() {
		return noEntryValue;
	}
}
