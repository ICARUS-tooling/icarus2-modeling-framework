/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.Annotation;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestOwner;
import de.ims.icarus2.model.manifest.api.ValueSet;
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
	AnnotationStorage getAnnotationStorage();

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
		return (T) getAnnotationStorage().getValue(item, getManifest().getDefaultKey());
	}

	default void setValue(Item item, String key, Object value) {
		getAnnotationStorage().setValue(item, key, value);
	}

	default void clearValue(Item item, String key) {
		AnnotationManifest manifest = getManifest().getAnnotationManifest(key);
		getAnnotationStorage().setValue(item, key, manifest.getNoEntryValue());
	}

	/**
	 * @author Markus Gärtner
	 *
	 */
	public interface AnnotationStorage {

		/**
		 * Collects all the keys in this layer which are mapped to valid annotation values for
		 * the given item. This method returns {@code true} iff at least one key was added
		 * to the supplied {@code action}.
		 *
		 * @param item
		 * @param action
		 * @return
		 * @throws NullPointerException if any one of the two arguments is {@code null}
		 */
		boolean collectKeys(Item item, Consumer<String> action);

		/**
		 * Returns the annotation for a given item and key or {@code null} if that item
		 * has not been assigned an annotation value for the specified key in this layer.
		 * Note that the returned object can be either an actual value or an {@link Annotation}
		 * instance that wraps a value and provides further information.
		 *
		 * @param item
		 * @param key
		 * @return
		 * @throws NullPointerException if either the {@code item} or {@code key}
		 * is {@code null}
		 */
		Object getValue(Item item, String key);

		default String getString(Item item, String key) {
			return (String) getValue(item, key);
		}

		int getIntegerValue(Item item, String key);
		float getFloatValue(Item item, String key);
		double getDoubleValue(Item item, String key);
		long getLongValue(Item item, String key);
		boolean getBooleanValue(Item item, String key);

		/**
		 * Deletes all annotations in this layer
		 * <p>
		 * Note that this does include all annotations for all keys,
		 * not only those declared for the default annotation.
		 *
		 * @throws UnsupportedOperationException if the corpus
		 * is not editable
		 */
		@Deprecated
		default void removeAllValues() {
			throw new ModelException(GlobalErrorCode.DEPRECATED, "To be removed in future build - don't use");
		}

		/**
		 * Deletes in this layer all annotations for
		 * the given {@code key}.
		 *
		 * @param key The key for which annotations should be
		 * deleted
		 * @throws UnsupportedOperationException if this layer does not allow multiple keys
		 * @throws UnsupportedOperationException if the corpus
		 * is not editable
		 *
		 * @deprecated this method defies the idea of light-weight
		 * annotation storage due to it requiring access to the entire
		 * data contained in the underlying storage.
		 */
		@Deprecated
		default void removeAllValues(String key) {
			throw new ModelException(GlobalErrorCode.DEPRECATED, "To be removed in future build - don't use");
		}

		/**
		 * Removes from this annotation storage all annotations for
		 * items returned by the given source.
		 * <p>
		 * If the {@code source} returns a {@code null} value, the operation
		 * will stop.
		 *
		 *
		 * @param source the "stream" of items to remove annotation values for
		 * @throws ModelException with {@link ModelErrorCode#MODEL_CORRUPTED_STATE} in case one of the
		 * given {@link Item items} is missing from this storage.
		 */
		void removeAllValues(Supplier<? extends Item> source);

		/**
		 * Removes from this annotation storage all annotations for
		 * items returned by the given source.
		 *
		 * @param source
		 *
		 * @see #removeAllValues(Supplier)
		 */
		default void removeAllValues(Iterator<? extends Item> source) {
			removeAllValues(() -> {return source.hasNext() ? source.next() : null;});
		}


		/**
		 * Assigns the given {@code value} as new annotation for the specified
		 * {@code Item} and {@code key}, replacing any previously defined value.
		 * If the {@code value} argument is {@code null} any stored annotation
		 * for the combination of {@code item} and {@code key} will be deleted.
		 * <p>
		 * This is an optional method
		 *
		 * @param item The {@code Item} to change the annotation value for
		 * @param key the key for which the annotation should be changed
		 * @param value the new annotation value or {@code null} if the annotation
		 * for the given {@code item} and {@code key} should be deleted
		 *
		 * @throws NullPointerException if the {@code item} or {@code key}
		 * argument is {@code null}
		 * @throws ModelException if the supplied {@code value} is not
		 * contained in the {@link ValueSet} of this layer's manifest for the given {@code key}.
		 * This is only checked if the manifest actually defines such restrictions.
		 * @throws UnsupportedOperationException if the corpus is not editable
		 */
		void setValue(Item item, String key, Object value);

		default void setString(Item item, String key, String value) {
			setValue(item, key, value);
		}

		void setIntegerValue(Item item, String key, int value);
		void setLongValue(Item item, String key, long value);
		void setFloatValue(Item item, String key, float value);
		void setDoubleValue(Item item, String key, double value);
		void setBooleanValue(Item item, String key, boolean value);

		/**
		 * Tells whether or not there are any annotations available for this storage.
		 * Note that the scope of the returned value is limited to the current part of
		 * the surrounding {@link AnnotationLayer} that has already been loaded into
		 * memory.
		 * <p>
		 * The returned value is also only to be taken as an indicator! Depending on the
		 * storage implementation it could be rather expensive to do a deep analysis of
		 * the content or backing data structures.
		 *
		 * @return {@code true} iff this layer holds at least one valid annotation object.
		 */
		boolean hasAnnotations();
		/**
		 *
		 * @return {@code true} iff this layer holds at least one valid annotation object
		 * for the given {@code Item}.
		 */
		boolean hasAnnotations(Item item);
	}
}
