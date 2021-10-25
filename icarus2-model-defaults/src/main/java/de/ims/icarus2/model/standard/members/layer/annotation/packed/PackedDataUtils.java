/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.layer.annotation.packed;

import java.util.Set;

import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.collections.LookupList;
import de.ims.icarus2.util.collections.Substitutor;

/**
 * @author Markus Gärtner
 *
 */
public class PackedDataUtils {

	/**
	 * Creates a set of {@link PackageHandle} instances describing all the
	 * {@link AnnotationManifest}s within the given {@link ContextManifest}
	 * that reference the context's {@link ContextManifest#getPrimaryLayerManifest() primary layer}.
	 *
	 * @param contextManifest
	 * @return
	 */
	public static Set<PackageHandle> createHandles(ContextManifest contextManifest,
			boolean allowBitPacking) {
		ItemLayerManifestBase<?> primaryLayer = (ItemLayerManifestBase<?>) contextManifest.getPrimaryLayerManifest()
				.orElseThrow(ManifestException.missing(contextManifest, "primary layer"))
				.getResolvedLayerManifest()
				.orElseThrow(ManifestException.missing(contextManifest, "resolved primary layer"));

		LazyCollection<PackageHandle> buffer = LazyCollection.lazySet();

		contextManifest.getLayerManifests().stream()
			.filter(ManifestUtils::isAnnotationLayerManifest)
			.map(AnnotationLayerManifest.class::cast)
			.forEach(manifest -> {
				if(manifest.isBaseLayerManifest(primaryLayer)) {
					manifest.forEachAnnotationManifest(annotationManifest ->
						buffer.add(createHandle(annotationManifest, allowBitPacking)));
				}
			});

		return buffer.getAsSet();
	}

	public static Set<PackageHandle> createHandles(AnnotationLayerManifest manifest,
			boolean allowBitPacking) {
		LazyCollection<PackageHandle> buffer = LazyCollection.lazySet();
		manifest.forEachAnnotationManifest(annotationManifest ->
			buffer.add(createHandle(annotationManifest, allowBitPacking)));

		return buffer.getAsSet();
	}

	public static PackageHandle createHandle(AnnotationManifest manifest, boolean allowBitPacking) {
		ValueType valueType = manifest.getValueType();

		// Easy mode for primitive values
		if(valueType.isPrimitiveType()) {
			return new PackageHandle(manifest, manifest.getNoEntryValue().orElseThrow(
					ManifestException.missing(manifest, "primitive noEntryValue")),
					BytePackConverter.forPrimitiveType(valueType, allowBitPacking));
		}

		// From here on only option is using substitutions
		return createDefaultSubstitutingHandler(manifest);
	}

	private static PackageHandle createDefaultSubstitutingHandler(AnnotationManifest manifest) {
		LookupList<Object> buffer = new LookupList<>(1000);
		Substitutor<Object> substitutor = new Substitutor<>(buffer, true);
		BytePackConverter converter = new BytePackConverter.SubstitutingConverterInt<>(
				manifest.getValueType(), 4, substitutor, substitutor);

		return new PackageHandle(manifest, manifest.getNoEntryValue().orElse(null), converter);
	}
}
