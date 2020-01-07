/**
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
package de.ims.icarus2.model.api.view;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.util.AccessMode;

/**
 * Models the basic properties of a {@code view} on a corpus,
 * i.e. a filtered part of its content.
 *
 * @author Markus Gärtner
 *
 */
public interface CorpusView {

	/**
	 * Returns the backing {@code Corpus}.
	 * @return
	 */
	Corpus getCorpus();

	/**
	 * Returns the {@code scope} that was used to limit the contexts
	 * and layers involved in this part or {@code null} if no vertical
	 * filtering was performed.
	 */
	Scope getScope();

	AccessMode getAccessMode();

	/**
	 * Returns the number of element in this sub corpus, i.e. the number of items
	 * contained in the <i>primary-layer</i> of the associated {@link #getScope() scope}.
	 *
	 * @return
	 */
	long getSize();

	// Lookup support

	default ItemLayer fetchPrimaryLayer() {
		return getScope().getPrimaryLayer();
	}

	/**
	 * Look up a {@link Layer} available for this view by its fully
	 * qualified name.
	 *
	 * @param qualifiedLayerId
	 * @return
	 */
	default <L extends Layer> L fetchLayer(String qualifiedLayerId) {
		// First fetch layer from underlying corpus
		L layer = getCorpus().getLayer(qualifiedLayerId, false);

		// Then make sure this view actually provides access to it
		if(!getScope().containsLayer(layer))
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"No such layer available in current scope: "+qualifiedLayerId);

		return layer;
	}


	default <L extends Layer> L fetchLayer(String contextId, String layerId) {
		Scope scope = getScope();

		Context context = getCorpus().getContext(contextId);
		if(!scope.containsContext(context))
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"No such context available in current scope: "+contextId);

		L layer = context.getLayer(layerId);

		if(!scope.containsLayer(layer))
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"No such layer available in current scope: "+layerId);

		return layer;
	}
}
