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
package de.ims.icarus2.model.api.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.meta.MetaData;
import de.ims.icarus2.model.api.view.CorpusView;
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

	public void fireCorpusViewCreated(CorpusView corpusView) {
		if (corpusView == null)
			throw new NullPointerException("Invalid corpusView"); //$NON-NLS-1$

		checkProvidedCorpus(corpusView.getCorpus());

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				CorpusEvent.VIEW_PROPERTY, corpusView);

		for(CorpusListener listener : listeners) {
			listener.corpusViewCreated(event);
		}
	}

	public void fireCorpusViewDestroyed(CorpusView corpusView) {
		if (corpusView == null)
			throw new NullPointerException("Invalid corpusView"); //$NON-NLS-1$

		checkProvidedCorpus(corpusView.getCorpus());

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				CorpusEvent.VIEW_PROPERTY, corpusView);

		for(CorpusListener listener : listeners) {
			listener.corpusViewDestroyed(event);
		}
	}
}
