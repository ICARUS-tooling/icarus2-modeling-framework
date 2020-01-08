/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.layer.annotation;

import java.util.Set;
import java.util.function.Supplier;

import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.manifest.api.AnnotationFlag;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysBoolean15BitStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysBoolean31BitStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysBoolean7BitStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysBooleanBitSetStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysDoubleStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysFloatStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysIntStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysLongStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysMixedObjectStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.single.SingleKeyBooleanStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.single.SingleKeyDoubleStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.single.SingleKeyFloatStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.single.SingleKeyIntegerStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.single.SingleKeyLongStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.single.SingleKeyObjectStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationStorage;
import de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationStorage.AnnotationBundle;
import de.ims.icarus2.util.Options;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * A factory that generates annotation storages suitable for
 * individual provided manifests.
 *
 * @author Markus Gärtner
 *
 */
public class AnnotationStorageFactory {

	private final AnnotationLayerManifest layerManifest;
	private final Options options;

	public AnnotationStorageFactory(AnnotationLayerManifest layerManifest,
			Options options) {
		if (layerManifest == null)
			throw new NullPointerException("Invalid layerManifest");

		if(options==null) {
			options = Options.NONE;
		}

		this.layerManifest = layerManifest;
		this.options = options;
	}

	public AnnotationStorage buildStorage() {

		AnnotationStorage storage = null;

		final Set<String> keySet = layerManifest.getAvailableKeys();
		final String defaultKey = layerManifest.getDefaultKey().orElseThrow(
				ManifestException.missing(layerManifest, "default key"));

		if(layerManifest.isAnnotationFlagSet(AnnotationFlag.UNKNOWN_KEYS)) {
			storage = buildUnboundStorage(layerManifest);
		} else {
			if(keySet.size()==1) {
				storage = buildSingleKeyStorage(layerManifest.getAnnotationManifest(defaultKey).orElseThrow(
						ManifestException.missing(layerManifest, "annotation manifest for key: "+defaultKey)));
			} else {
				storage = buildFixedKeyStorage(keySet, layerManifest);
			}
		}

		return storage;
	}

	protected AnnotationStorage buildSingleKeyStorage(AnnotationManifest annotationManifest) {
		ValueType valueType = annotationManifest.getValueType();

		if(valueType==ValueType.INTEGER) {
			return new SingleKeyIntegerStorage();
		} else if(valueType==ValueType.LONG) {
			return new SingleKeyLongStorage();
		} else if(valueType==ValueType.FLOAT) {
			return new SingleKeyFloatStorage();
		} else if(valueType==ValueType.DOUBLE) {
			return new SingleKeyDoubleStorage();
		} else if(valueType==ValueType.BOOLEAN) {
			return new SingleKeyBooleanStorage();
		} else {
			return new SingleKeyObjectStorage();
		}
	}

	protected AnnotationStorage buildFixedKeyStorage(Set<String> keySet, AnnotationLayerManifest layerManifest) {
		Set<ValueType> valueTypes = new ReferenceOpenHashSet<>();

		// Collect value types to see if we can optimize storage
		for(String key : keySet) {
			AnnotationManifest annotationManifest = layerManifest.getAnnotationManifest(key).get();
			valueTypes.add(annotationManifest.getValueType());
		}

		// If only 1 type is present we can optimize in case of primitives
		if(valueTypes.size()==1) {
			ValueType valueType = valueTypes.iterator().next();

			return buildFixedKeysStorage(layerManifest, valueType);
		}

		return buildUnboundStorage(layerManifest);
	}

	protected AnnotationStorage buildFixedKeysStorage(AnnotationLayerManifest layerManifest, ValueType valueType) {

		if(valueType==ValueType.INTEGER) {
			return new FixedKeysIntStorage();
		} else if(valueType==ValueType.LONG) {
			return new FixedKeysLongStorage();
		} else if(valueType==ValueType.FLOAT) {
			return new FixedKeysFloatStorage();
		} else if(valueType==ValueType.DOUBLE) {
			return new FixedKeysDoubleStorage();
		} else if(valueType==ValueType.BOOLEAN) {
			return buildFixedBooleanStorage(layerManifest);
		} else {
			return new FixedKeysMixedObjectStorage();
		}
	}

	protected AnnotationStorage buildFixedBooleanStorage(AnnotationLayerManifest layerManifest) {
		int keyCount = layerManifest.getAvailableKeys().size();

		if(keyCount<=FixedKeysBoolean7BitStorage.MAX_KEY_COUNT) {
			return new FixedKeysBoolean7BitStorage();
		} else if(keyCount<=FixedKeysBoolean15BitStorage.MAX_KEY_COUNT) {
			return new FixedKeysBoolean15BitStorage();
		} else if(keyCount<=FixedKeysBoolean31BitStorage.MAX_KEY_COUNT) {
			return new FixedKeysBoolean31BitStorage();
		} else {
			return new FixedKeysBooleanBitSetStorage();
		}
	}

	protected AnnotationStorage buildUnboundStorage(AnnotationLayerManifest layerManifest) {
		Supplier<AnnotationBundle> bundleFactory = ComplexAnnotationStorage.COMPACT_BUNDLE_FACTORY;
		return new ComplexAnnotationStorage(bundleFactory);
	}
}
