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
package de.ims.icarus2.model.api.registry;

import static java.util.Objects.requireNonNull;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.view.CorpusView;

/**
 * @author Markus Gärtner
 *
 */
public class LayerLookup {

	private final Reference2IntMap<Layer> layerIds = new Reference2IntOpenHashMap<>();
	private final Int2ObjectMap<Layer> layerLookup = new Int2ObjectOpenHashMap<>();

	private final CorpusView view;

	public LayerLookup(CorpusView view) {
		requireNonNull(view);

		this.view = view;

		view.getScope().forEachLayer(l -> {
			int uid = l.getManifest().getUID();
			layerIds.put(l, uid);
			layerLookup.put(uid, l);
		});

		//TODO sanity check for making sure that we didn't get duplicate uids?
	}

	public CorpusView getView() {
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
