/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.api.registry;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class LayerLookup {

	private final Reference2IntMap<Layer> layerIds = new Reference2IntOpenHashMap<>();
	private final Int2ObjectMap<Layer> layerLookup = new Int2ObjectOpenHashMap<>();

	private final PagedCorpusView view;

	public LayerLookup(PagedCorpusView view) {
		requireNonNull(view);

		this.view = view;

		view.getScope().forEachLayer(l -> {
			int uid = l.getManifest().getUID();
			layerIds.put(l, uid);
			layerLookup.put(uid, l);
		});

		//TODO sanity check for making sure that we didn't get duplicate uids?
	}

	public PagedCorpusView getView() {
		return view;
	}

	public Layer getLayer(int uid) {
		return layerLookup.get(uid);
	}

	public int getUID(Layer layer) {
		return layerIds.getInt(layer);
	}

	/**
	 * Returns an array containing all the layers currently available in this lookup.
	 * @return
	 */
	public Layer[] getLayers() {
		Layer[] a = new Layer[layerIds.size()];
		return layerIds.keySet().toArray(a);
	}
}
