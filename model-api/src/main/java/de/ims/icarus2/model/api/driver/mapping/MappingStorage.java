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
package de.ims.icarus2.model.api.driver.mapping;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.LazyCollection;

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

	private final BiFunction<ItemLayerManifest, ItemLayerManifest, Mapping> fallback;

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

	public Mapping getMapping(ItemLayerManifest source, ItemLayerManifest target) {
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

	public boolean hasMapping(ItemLayerManifest source, ItemLayerManifest target) {
		return mappingMap.containsKey(getKey(source, target));
	}

	public List<Mapping> getOutgoingMappings(ItemLayerManifest source) {
		LazyCollection<Mapping> result = LazyCollection.lazyList();

		for(Mapping mapping : mappingSet) {
			if(mapping.getSourceLayer()==source) {
				result.add(mapping);
			}
		}

		return result.getAsList();
	}

	public List<Mapping> getIncomingMappings(ItemLayerManifest target) {
		LazyCollection<Mapping> result = LazyCollection.lazyList();

		for(Mapping mapping : mappingSet) {
			if(mapping.getTargetLayer()==target) {
				result.add(mapping);
			}
		}

		return result.getAsList();
	}

	public void forEachIncomingMapping(ItemLayerManifest target, Consumer<? super Mapping> action) {
		for(Mapping mapping : mappingSet) {
			if(mapping.getTargetLayer()==target) {
				action.accept(mapping);
			}
		}
	}

	public void forEachOutgoingMapping(ItemLayerManifest target, Consumer<? super Mapping> action) {
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


	public static long getKey(LayerManifest source, LayerManifest target) {
		return (long)source.getUID() | ((long)target.getUID()<<32);
	}

	public static class Builder {
		private Long2ObjectMap<Mapping> mappingMap;
		private BiFunction<ItemLayerManifest, ItemLayerManifest, Mapping> fallback;

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

		public Mapping getMapping(ItemLayerManifest source, ItemLayerManifest target) {

			long key = getKey(source, target);

			return mappingMap.get(key);
		}

		public Builder fallback(BiFunction<ItemLayerManifest, ItemLayerManifest, Mapping> fallback) {
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
