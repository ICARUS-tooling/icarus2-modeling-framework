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
package de.ims.icarus2.model.standard.members.layer.annotation.packed;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.collections.LookupList;

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

	/**
	 * Sentinel value to signal an empty or {@code null} value.
	 * Chosen to be {@code 0} so that empty storage doesn't need
	 * any pre-processing.
	 */
	private static final int EMPTY_VALUE = 0;

	/**
	 * Implements (re)substitution of arbitrary types via a backing {@link LookupList}.
	 * Note that the back-end storage only ever grows through the lifetime of any instance
	 * of this class!
	 *
	 * @author Markus Gärtner
	 *
	 * @param <T> type of values to be substituted (must be immutable
	 * wrt {@link Object#equals(Object)})
	 */
	public static class Substitutor<T> implements Closeable, ToIntFunction<T>, IntFunction<T> {
		private final LookupList<T> buffer;
		private final boolean clearOnClose;

		public Substitutor(LookupList<T> buffer, boolean clearOnClose) {
			this.buffer = requireNonNull(buffer);
			this.clearOnClose = clearOnClose;
		}

		public Substitutor(int capacity) {
			buffer = new LookupList<>(capacity);
			clearOnClose = true;
		}

		public Substitutor() {
			buffer = new LookupList<>();
			clearOnClose = true;
		}

		@Override
		public int applyAsInt(T value) {
			if(value==null) {
				return EMPTY_VALUE;
			}

			int index = buffer.indexOf(value);
			if(index==UNSET_INT) {
				synchronized (buffer) {
					index = buffer.size();
					buffer.add(index, value);
				}
			}
			return index+1;
		}

		@Override
		public T apply(int value) {
			return value==EMPTY_VALUE ? null : buffer.get(value-1);
		}

		@Override
		public void close() {
			if(clearOnClose) {
				buffer.clear();
			}
		}
	}
}
