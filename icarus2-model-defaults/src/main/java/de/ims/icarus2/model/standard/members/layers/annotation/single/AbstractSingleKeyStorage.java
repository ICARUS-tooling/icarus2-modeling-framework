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

import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.layers.annotation.AbstractManagedAnnotationStorage;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractSingleKeyStorage extends AbstractManagedAnnotationStorage {

	/**
	 * @param weakKeys
	 * @param initialCapacity
	 */
	public AbstractSingleKeyStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	protected static String requireDefaultKey(AnnotationLayerManifest manifest) {
		return manifest.getDefaultKey().orElseThrow(ManifestException.missing(manifest, "default key"));
	}

	private String annotationKey;

	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		this.annotationKey = requireDefaultKey(layer.getManifest());
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
					Messages.mismatch("Unknown key", annotationKey, key));
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
	public boolean containsItem(Item item) {
		return hasAnnotations(item);
	}

}
