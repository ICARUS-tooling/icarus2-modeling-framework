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
package de.ims.icarus2.model.standard.members.layers.annotation;

import java.util.Set;
import java.util.function.Supplier;

import de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage;
import de.ims.icarus2.model.manifest.api.AnnotationFlag;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBoolean15BitStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBoolean31BitStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBoolean7BitStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysBooleanBitSetStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysDoubleStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysFloatStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysIntStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysLongStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.fixed.FixedKeysMixedObjectStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyBooleanStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyDoubleStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyFloatStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyIntegerStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyLongStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.single.SingleKeyObjectStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationStorage.AnnotationBundle;
import de.ims.icarus2.util.Options;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
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
			options = Options.emptyOptions;
		}

		this.layerManifest = layerManifest;
		this.options = options;
	}

	public AnnotationStorage buildStorage() {

		AnnotationStorage storage = null;

		final Set<String> keySet = layerManifest.getAvailableKeys();
		final String defaultKey = layerManifest.getDefaultKey();

		if(layerManifest.isAnnotationFlagSet(AnnotationFlag.UNKNOWN_KEYS)) {
			storage = buildUnboundStorage(layerManifest);
		} else {
			if(keySet.size()==1) {
				storage = buildSingleKeyStorage(layerManifest.getAnnotationManifest(defaultKey));
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
			AnnotationManifest annotationManifest = layerManifest.getAnnotationManifest(key);
			valueTypes.add(annotationManifest.getValueType());
		}

		// If only 1 type is present we can optimize in case of primitives
		if(valueTypes.size()==1) {
			ValueType valueType = valueTypes.iterator().next();

			return buildFixedKeysStorage(layerManifest, valueType);
		} else {
			return buildUnboundStorage(layerManifest);
		}
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
