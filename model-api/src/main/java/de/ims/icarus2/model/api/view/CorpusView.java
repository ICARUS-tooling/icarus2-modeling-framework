/**
 *
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
 * @author Markus
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

	default <L extends Layer> L fetchLayer(String qualifiedLayerId) {
		L layer = getCorpus().getLayer(qualifiedLayerId, false);

		Scope scope = getScope();

		if(!scope.containsLayer(layer))
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
