/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.meta.MetaData;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.model.util.ModelUtils;

/**
 * @author Markus Gärtner
 *
 */
public class CorpusEventManager {

	private final Corpus corpus;

	private final List<CorpusListener> listeners = new CopyOnWriteArrayList<>();

	public CorpusEventManager(Corpus corpus) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$

		this.corpus = corpus;
	}

	public void addCorpusListener(CorpusListener listener) {
		if (listener == null)
			throw new NullPointerException("Invalid listener");  //$NON-NLS-1$

		if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeCorpusListener(CorpusListener listener) {
		if (listener == null)
			throw new NullPointerException("Invalid listener");  //$NON-NLS-1$

		if(listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	private void checkProvidedCorpus(Corpus target) {
		if(target!=corpus)
			throw new IllegalArgumentException("Cannot fire events for foreign corpus "+ModelUtils.getName(corpus));
	}

	public void fireCorpusChanged() {
		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus);

		for(CorpusListener listener : listeners) {
			listener.corpusChanged(event);
		}
	}

	public void fireCorpusSaved() {
		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus);

		for(CorpusListener listener : listeners) {
			listener.corpusSaved(event);
		}
	}

	public void fireContextAdded(Context context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		checkProvidedCorpus(context.getCorpus());

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				CorpusEvent.CONTEXT_PROPERTY, context);

		for(CorpusListener listener : listeners) {
			listener.contextAdded(event);
		}
	}

	public void fireContextRemoved(Context context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		checkProvidedCorpus(context.getCorpus());

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				CorpusEvent.CONTEXT_PROPERTY, context);

		for(CorpusListener listener : listeners) {
			listener.contextRemoved(event);
		}
	}

	public void fireLayerAdded(Layer layer) {
		if (layer == null)
			throw new NullPointerException("Invalid layer"); //$NON-NLS-1$

		checkProvidedCorpus(layer.getCorpus());

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				CorpusEvent.LAYER_PROPERTY, layer);

		for(CorpusListener listener : listeners) {
			listener.layerAdded(event);
		}
	}

	public void fireLayerRemoved(Layer layer) {
		if (layer == null)
			throw new NullPointerException("Invalid layer"); //$NON-NLS-1$

		checkProvidedCorpus(layer.getCorpus());

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				CorpusEvent.LAYER_PROPERTY, layer);

		for(CorpusListener listener : listeners) {
			listener.layerRemoved(event);
		}
	}

//	public void fireMemberAdded(CorpusMember member) {
//		if (member == null)
//			throw new NullPointerException("Invalid member"); //$NON-NLS-1$
//
//		if(listeners.isEmpty()) {
//			return;
//		}
//
//		CorpusEvent event = new CorpusEvent(corpus,
//				"member", member); //$NON-NLS-1$
//
//		for(CorpusListener listener : listeners) {
//			listener.memberAdded(event);
//		}
//	}
//
//	public void fireMemberRemoved(CorpusMember member) {
//		if (member == null)
//			throw new NullPointerException("Invalid member"); //$NON-NLS-1$
//
//		if(listeners.isEmpty()) {
//			return;
//		}
//
//		CorpusEvent event = new CorpusEvent(corpus,
//				"member", member); //$NON-NLS-1$
//
//		for(CorpusListener listener : listeners) {
//			listener.memberRemoved(event);
//		}
//	}
//
//	public void fireMemberChanged(CorpusMember member) {
//		if (member == null)
//			throw new NullPointerException("Invalid member"); //$NON-NLS-1$
//
//		if(listeners.isEmpty()) {
//			return;
//		}
//
//		CorpusEvent event = new CorpusEvent(corpus,
//				"member", member); //$NON-NLS-1$
//
//		for(CorpusListener listener : listeners) {
//			listener.memberChanged(event);
//		}
//	}

	public void fireMetaDataAdded(MetaData metaData, Layer layer) {
		if (metaData == null)
			throw new NullPointerException("Invalid metaData"); //$NON-NLS-1$
		if (layer == null)
			throw new NullPointerException("Invalid layer");  //$NON-NLS-1$

		checkProvidedCorpus(layer.getCorpus());

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				CorpusEvent.METADATA_PROPERTY, metaData,
				CorpusEvent.LAYER_PROPERTY, layer);

		for(CorpusListener listener : listeners) {
			listener.metaDataAdded(event);
		}
	}

	public void fireMetaDataRemoved(MetaData metaData, Layer layer) {
		if (metaData == null)
			throw new NullPointerException("Invalid metaData"); //$NON-NLS-1$
		if (layer == null)
			throw new NullPointerException("Invalid layer");  //$NON-NLS-1$

		checkProvidedCorpus(layer.getCorpus());

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				CorpusEvent.METADATA_PROPERTY, metaData,
				CorpusEvent.LAYER_PROPERTY, layer);

		for(CorpusListener listener : listeners) {
			listener.metaDataRemoved(event);
		}
	}

	public void fireCorpusViewCreated(PagedCorpusView pagedCorpusView) {
		if (pagedCorpusView == null)
			throw new NullPointerException("Invalid corpusView"); //$NON-NLS-1$

		checkProvidedCorpus(pagedCorpusView.getCorpus());

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				CorpusEvent.VIEW_PROPERTY, pagedCorpusView);

		for(CorpusListener listener : listeners) {
			listener.corpusPartCreated(event);
		}
	}

	public void fireCorpusViewDestroyed(PagedCorpusView pagedCorpusView) {
		if (pagedCorpusView == null)
			throw new NullPointerException("Invalid corpusView"); //$NON-NLS-1$

		checkProvidedCorpus(pagedCorpusView.getCorpus());

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				CorpusEvent.VIEW_PROPERTY, pagedCorpusView);

		for(CorpusListener listener : listeners) {
			listener.corpusPartDestroyed(event);
		}
	}
}
