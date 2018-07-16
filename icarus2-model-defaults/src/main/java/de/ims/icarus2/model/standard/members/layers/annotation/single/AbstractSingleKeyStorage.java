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

import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.layers.annotation.AbstractAnnotationStorage;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractSingleKeyStorage extends AbstractAnnotationStorage {

	/**
	 * @param weakKeys
	 * @param initialCapacity
	 */
	public AbstractSingleKeyStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	private String annotationKey;

	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		AnnotationLayerManifest manifest = layer.getManifest();

		String annotationKey = manifest.getDefaultKey();

		if (annotationKey == null)
			throw new NullPointerException("Invalid annotationKey");

		this.annotationKey = annotationKey;
	}

	@Override
	public void removeNotify(AnnotationLayer layer) {
		super.removeNotify(layer);

		annotationKey = null;
	}

	public String getAnnotationKey() {
		return annotationKey;
	}

	/**
	 * Special consideration of single key storages: We allow {@code null}
	 * keys which default to the one single key defined for this annotation!
	 */
	protected void checkKey(String key) {
		if(key!=null && !annotationKey.equals(key))
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					Messages.mismatchMessage("Unknown key", annotationKey, key));
	}

	@Override
	public boolean collectKeys(Item item, Consumer<String> action) {
		boolean result = false;

		if(hasAnnotations(item)) {
			action.accept(annotationKey);

			result = true;
		}

		return result;
	}

	@Override
	public void removeAllValues(String key) {
		checkKey(key);
		removeAllValues();
	}

	@Override
	public boolean containsItem(Item item) {
		return hasAnnotations(item);
	}

}
