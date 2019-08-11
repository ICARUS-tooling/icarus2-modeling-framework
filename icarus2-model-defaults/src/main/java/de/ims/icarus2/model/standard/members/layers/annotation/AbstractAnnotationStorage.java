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

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.util.AbstractPart;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractAnnotationStorage extends AbstractPart<AnnotationLayer>
		implements ManagedAnnotationStorage {

	private final boolean weakKeys;
	private final int initialCapacity;

	public AbstractAnnotationStorage(boolean weakKeys, int initialCapacity) {
		if(initialCapacity<=0 && initialCapacity!=UNSET_INT)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Capacity must be positive or -1: "+initialCapacity);

		this.weakKeys = weakKeys;
		this.initialCapacity = initialCapacity;
	}

	public boolean isWeakKeys() {
		return weakKeys;
	}

	protected static AnnotationManifest requireAnnotationsManifest(AnnotationLayerManifest manifest, String key) {
		return manifest.getAnnotationManifest(key).orElseThrow(ManifestException.missing(manifest,
				"annotation manifest for key: "+key));
	}

	protected static String requireKey(AnnotationManifest manifest) {
		return manifest.getKey().orElseThrow(ManifestException.missing(manifest, "key"));
	}

	/**
	 * Tries to estimate a good initial capacity to store annotation values
	 * for the given layer. The default implementation always returns {@code 1000}.
	 * Subclasses might want to access the driver(s) of the given layer's base
	 * layer(s) to query them for size information.
	 *
	 * @param layer
	 * @return
	 */
	protected int estimateRequiredCapacity(AnnotationLayer layer) {
		return 1000;
	}

	protected int getInitialCapacity(AnnotationLayer layer) {

		int initialCapacity = this.initialCapacity;

		if(initialCapacity==UNSET_INT) {
			initialCapacity = estimateRequiredCapacity(layer);
		}

		return initialCapacity;
	}

	@Override
	public boolean collectKeys(Item item, Consumer<String> action) {
		return false;
	}

	private static ModelException forUnsupportedGetter(String type, String key) {
		return new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Annotation value is not an "+type+" for key: "+key);
	}

	@Unguarded(Unguarded.UNSUPPORTED)
	@Override
	public String getString(Item item, String key) {
		throw forUnsupportedGetter("string", key);
	}

	@Unguarded(Unguarded.UNSUPPORTED)
	@Override
	public int getInteger(Item item, String key) {
		throw forUnsupportedGetter("integer", key);
	}

	@Unguarded(Unguarded.UNSUPPORTED)
	@Override
	public float getFloat(Item item, String key) {
		throw forUnsupportedGetter("float", key);
	}

	@Unguarded(Unguarded.UNSUPPORTED)
	@Override
	public double getDouble(Item item, String key) {
		throw forUnsupportedGetter("double", key);
	}

	@Unguarded(Unguarded.UNSUPPORTED)
	@Override
	public long getLong(Item item, String key) {
		throw forUnsupportedGetter("long", key);
	}

	@Unguarded(Unguarded.UNSUPPORTED)
	@Override
	public boolean getBoolean(Item item, String key) {
		throw forUnsupportedGetter("boolean", key);
	}

	private static ModelException forUnsupportedSetter(String type, String key) {
		return new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
				"Cannot set "+type+" value for key: "+key);
	}

	@Unguarded(Unguarded.UNSUPPORTED)
	@Override
	public void setString(Item item, String key, String value) {
		throw forUnsupportedSetter("string", key);
	}

	@Unguarded(Unguarded.UNSUPPORTED)
	@Override
	public void setInteger(Item item, String key, int value) {
		throw forUnsupportedSetter("integer", key);
	}

	@Unguarded(Unguarded.UNSUPPORTED)
	@Override
	public void setLong(Item item, String key, long value) {
		throw forUnsupportedSetter("long", key);
	}

	@Unguarded(Unguarded.UNSUPPORTED)
	@Override
	public void setFloat(Item item, String key, float value) {
		throw forUnsupportedSetter("float", key);
	}

	@Unguarded(Unguarded.UNSUPPORTED)
	@Override
	public void setDouble(Item item, String key, double value) {
		throw forUnsupportedSetter("double", key);
	}

	@Unguarded(Unguarded.UNSUPPORTED)
	@Override
	public void setBoolean(Item item, String key, boolean value) {
		throw forUnsupportedSetter("boolean", key);
	}

	@Override
	public boolean hasAnnotations() {
		return false;
	}

	@Override
	public boolean hasAnnotations(Item item) {
		return false;
	}

	@Override
	public boolean containsItem(Item item) {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage#addItem(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean addItem(@Nullable Item item) {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage#removeItem(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean removeItem(@Nullable Item item) {
		return true;
	}

}
