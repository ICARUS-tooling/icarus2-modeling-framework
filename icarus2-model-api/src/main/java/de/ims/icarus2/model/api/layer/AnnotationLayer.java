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
package de.ims.icarus2.model.api.layer;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestOwner;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 *
 * @author Markus Gärtner
 *
 */
public interface AnnotationLayer extends Layer, ManifestOwner<AnnotationLayerManifest> {

	/**
	 * Returns the shared {@code AnnotationLayerManifest} that holds
	 * information about keys and possible values in this annotation layer.
	 *
	 * @return The manifest that describes this annotation layer
	 */
	@Override
	AnnotationLayerManifest getManifest();

	/**
	 * Returns the {@code AnnotationLayer}s that this layer
	 * references in case it is stores meta-annotations on.
	 *
	 * @return
	 */
	DataSet<AnnotationLayer> getReferenceLayers();

	/**
	 * Returns the background storage that holds the actual annotation data in this
	 * layer and is directly managed by the driver that created this layer and its
	 * context.
	 * <p>
	 * The returned {@link AnnotationStorage} instance also gives access to all
	 * the specialized setter and getter methods for working with primitive annotation
	 * types for efficiency reasons.
	 *
	 * @return
	 */
	@Nullable AnnotationStorage getAnnotationStorage();

	/**
	 * Defines the storage to be used for this layer.
	 *
	 * @param storage
	 *
	 * @throws NullPointerException iff the {@code storage} argument is {@code null}
	 */
	//TODO check whether to throw exception when storage is already set or allow client code modifications?
	void setAnnotationStorage(AnnotationStorage storage);

	/**
	 * Defines the set of reference layers to be used for this layer.
	 * @param baseLayers
	 *
	 * @throws NullPointerException iff the {@code referenceLayers} argument is {@code null}
	 * @throws ModelException in case the reference layers have already been set
	 */
	void setReferenceLayers(DataSet<AnnotationLayer> referenceLayers);

	/**
	 * Shorthand method to fetch the annotation value for a the specified combination
	 * of {@code item} and {@code key}.
	 *
	 * @param item
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default <T extends Object> T getValue(Item item, String key) {
		return (T) getAnnotationStorage().getValue(item, key);
	}

	/**
	 * Shorthand method to fetch the annotation value for a the specified {@code item}
	 * using this layer's {@link AnnotationLayerManifest#getDefaultKey() default key}.
	 *
	 * @param item
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default <T extends Object> T getValue(Item item) {
		requireNonNull(item);
		return (T) getAnnotationStorage().getValue(item, getManifest().getDefaultKey().get());
	}

	default void setValue(Item item, String key, @Nullable Object value) {
		getAnnotationStorage().setValue(item, key, value);
	}

	default void clearValue(Item item, String key) {
		requireNonNull(item);
		requireNonNull(key);
		getManifest().getAnnotationManifest(key)
			.flatMap(AnnotationManifest::getNoEntryValue)
			.ifPresent(v -> getAnnotationStorage().setValue(item, key, v));
	}
}
