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
package de.ims.icarus2.model.standard.members.layers;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Collection;

import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class LayerCache {

	private static Int2ObjectMap<Layer> _map(Layer[] layers) {

		Int2ObjectMap<Layer> map = new Int2ObjectOpenHashMap<>();

		for(Layer layer : layers) {
			map.put(layer.getManifest().getUID(), layer);
		}

		return map;
	}

	private static <L extends Layer> Int2ObjectMap<Layer> _map(Iterable<L> layers) {

		Int2ObjectMap<Layer> map = new Int2ObjectOpenHashMap<>();

		for(Layer layer : layers) {
			map.put(layer.getManifest().getUID(), layer);
		}

		return map;
	}

	public static LayerCache fromLayers(Layer...layers) {
		requireNonNull(layers);
		checkArgument(layers.length>0);

		return new LayerCache(_map(layers));
	}

	public static <L extends Layer> LayerCache fromCollection(Collection<L> layers) {
		requireNonNull(layers);
		checkArgument(!layers.isEmpty());

		return new LayerCache(_map(layers));
	}

	public static <L extends Layer> LayerCache fromScope(Scope scope) {
		requireNonNull(scope);

		Int2ObjectMap<Layer> map = _map(scope.getLayers());

		ItemLayer primaryLayer = scope.getPrimaryLayer();
		map.put(primaryLayer.getManifest().getUID(), primaryLayer);

		return new LayerCache(map);
	}

	private final Int2ObjectMap<Layer> layers;

	private LayerCache(Int2ObjectMap<Layer> layers) {
		requireNonNull(layers);
		this.layers = layers;
	}

	@SuppressWarnings("unchecked")
	public <L extends Layer> L getLayer(int uid) {
		return (L) layers.get(uid);
	}

	public boolean contains(Layer layer) {
		return layers.containsKey(layer.getManifest().getUID());
	}

	public Collection<Layer> layerCollection() {
		return CollectionUtils.getCollectionProxy(layers.values());
	}

	public Layer[] layers() {
		Layer[] result = new Layer[layers.size()];
		layers.values().toArray(result);
		return result;
	}
}
