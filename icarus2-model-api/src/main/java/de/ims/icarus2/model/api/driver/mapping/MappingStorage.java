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
package de.ims.icarus2.model.api.driver.mapping;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.LazyCollection;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * Not thread safe!
 *
 * @author Markus Gärtner
 *
 */
public class MappingStorage {

	public static String getLabel(Mapping mapping) {
		return mapping.getSourceLayer().getName()+"->"+mapping.getTargetLayer().getName(); //$NON-NLS-1$
	}

	private final Long2ObjectMap<Mapping> mappingMap;
	private final Set<Mapping> mappingSet = new ReferenceOpenHashSet<>();

	private final BiFunction<ItemLayerManifestBase<?>, ItemLayerManifestBase<?>, Mapping> fallback;

	protected MappingStorage(Builder builder) {

		// Copy builder data and reset builder
		mappingMap = builder.mappingMap;
		fallback = builder.fallback;
		builder.reset();

		// Post processing of builder data
		mappingSet.addAll(mappingMap.values());
	}

	public Set<Mapping> getMappings() {
		return CollectionUtils.getSetProxy(mappingSet);
	}

	public Mapping getMapping(ItemLayerManifestBase<?> source, ItemLayerManifestBase<?> target) {
		long key = getKey(source, target);
		Mapping mapping = mappingMap.get(key);

		if(mapping==null && fallback!=null) {
			mapping = fallback.apply(source, target);
			if(mapping!=null) {
				mappingMap.put(key, mapping);
				mappingSet.add(mapping);
			}
		}

		return mapping;
	}

	public boolean hasMapping(ItemLayerManifestBase<?> source, ItemLayerManifestBase<?> target) {
		return mappingMap.containsKey(getKey(source, target));
	}

	public List<Mapping> getOutgoingMappings(ItemLayerManifestBase<?> source) {
		LazyCollection<Mapping> result = LazyCollection.lazyList();

		for(Mapping mapping : mappingSet) {
			if(mapping.getSourceLayer()==source) {
				result.add(mapping);
			}
		}

		return result.getAsList();
	}

	public List<Mapping> getIncomingMappings(ItemLayerManifestBase<?> target) {
		LazyCollection<Mapping> result = LazyCollection.lazyList();

		for(Mapping mapping : mappingSet) {
			if(mapping.getTargetLayer()==target) {
				result.add(mapping);
			}
		}

		return result.getAsList();
	}

	public void forEachIncomingMapping(ItemLayerManifestBase<?> target, Consumer<? super Mapping> action) {
		for(Mapping mapping : mappingSet) {
			if(mapping.getTargetLayer()==target) {
				action.accept(mapping);
			}
		}
	}

	public void forEachOutgoingMapping(ItemLayerManifestBase<?> target, Consumer<? super Mapping> action) {
		for(Mapping mapping : mappingSet) {
			if(mapping.getSourceLayer()==target) {
				action.accept(mapping);
			}
		}
	}

	public void forEachMapping(Consumer<? super Mapping> action) {
		mappingSet.forEach(action);
	}

	public static long getKey(Mapping mapping) {
		return getKey(mapping.getSourceLayer(), mapping.getTargetLayer());
	}


	public static long getKey(LayerManifest<?> source, LayerManifest<?> target) {
		return source.getUID() | ((long)target.getUID()<<32);
	}

	public static class Builder {
		private Long2ObjectMap<Mapping> mappingMap;
		private BiFunction<ItemLayerManifestBase<?>, ItemLayerManifestBase<?>, Mapping> fallback;

		public Builder() {
			reset();
		}

		public Builder addMapping(Mapping mapping) {
			requireNonNull(mapping);

			long key = getKey(mapping);

			if(mappingMap.containsKey(key))
				throw new IllegalArgumentException("Duplicate mapping: "+getLabel(mapping));

			mappingMap.put(key, mapping);

			return this;
		}

		public Mapping getMapping(ItemLayerManifestBase<?> source, ItemLayerManifestBase<?> target) {

			long key = getKey(source, target);

			return mappingMap.get(key);
		}

		public Builder fallback(BiFunction<ItemLayerManifestBase<?>, ItemLayerManifestBase<?>, Mapping> fallback) {
			requireNonNull(fallback);
			checkState(this.fallback==null);

			this.fallback = fallback;

			return this;
		}

		public Builder reset() {
			mappingMap = new Long2ObjectOpenHashMap<>();
			return this;
		}

		/**
		 * Verifies that all required fields have been set for a successful build operation.
		 * <p>
		 * The default implementation does nothing.
		 */
		protected void validate() {
			// Verification opportunity for subclasses
		}

		public MappingStorage build() {
			validate();

			return new MappingStorage(this);
		}
	}
}
