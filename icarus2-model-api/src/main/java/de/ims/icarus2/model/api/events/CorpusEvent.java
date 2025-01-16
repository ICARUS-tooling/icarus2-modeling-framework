/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.Hashtable;
import java.util.Map;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.meta.MetaData;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 *
 * @author Markus Gärtner
 *
 */
public class CorpusEvent {

	private final Corpus corpus;

	/**
	 * Holds the properties of the event.
	 */
	private final Map<String, Object> properties;

	/**
	 * Holds the consumed state of the event. Default is false.
	 */
	private boolean consumed = false;

	/**
	 * Constructs a new event for the given corpus.
	 */
	public CorpusEvent(Corpus corpus) {
		this(corpus, (Object[]) null);
	}

	/**
	 * Constructs a new event for the given corpus and properties. The optional
	 * properties are specified using a sequence of keys and values, eg.
	 * {@code new mxEventObject("eventName", key1, val1, .., keyN, valN))}
	 */
	public CorpusEvent(Corpus corpus, Object... args) {
		this.corpus = corpus;
		properties = new Hashtable<String, Object>();

		if (args != null) {
			for (int i = 0; i < args.length; i += 2) {
				if (args[i + 1] != null) {
					properties.put(String.valueOf(args[i]), args[i + 1]);
				}
			}
		}
	}

	/**
	 *
	 */
	public Map<String, Object> getProperties() {
		return CollectionUtils.getMapProxy(properties);
	}

	/**
	 *
	 */
	public Object getProperty(String key) {
		return properties.get(key);
	}

	/**
	 * Returns true if the event has been consumed.
	 */
	public boolean isConsumed() {
		return consumed;
	}

	/**
	 * Consumes the event.
	 */
	public void consume() {
		consumed = true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("CorpusEvent: "); //$NON-NLS-1$
		if(consumed) {
			sb.append(" (consumed)"); //$NON-NLS-1$
		}

		sb.append('[');
		for(Map.Entry<String, Object> entry : properties.entrySet()) {
			sb.append(entry.getKey()).append('=').append(entry.getValue()).append(' ');
		}
		sb.append(']');

		return sb.toString();
	}

	/**
	 * @return the corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	// PROPERTY CONSTANTS

	public static final String CONTEXT_PROPERTY = "context"; //$NON-NLS-1$
	public static final String MEMBER_PROPERTY = "member"; //$NON-NLS-1$
	public static final String LAYER_PROPERTY = "layer"; //$NON-NLS-1$
	public static final String CONTAINER_PROPERTY = "container"; //$NON-NLS-1$
	public static final String STRUCTURE_PROPERTY = "structure"; //$NON-NLS-1$
	public static final String METADATA_PROPERTY = "metadata"; //$NON-NLS-1$
	public static final String VIEW_PROPERTY = "view"; //$NON-NLS-1$

	// HELPER METHODS

	public Context getContext() {
		return (Context) getProperty(CONTEXT_PROPERTY);
	}

	public CorpusMember getMember() {
		return (CorpusMember) getProperty(MEMBER_PROPERTY);
	}

	public Layer getLayer() {
		return (Layer) getProperty(LAYER_PROPERTY);
	}

	public Container getContainer() {
		return (Container) getProperty(CONTAINER_PROPERTY);
	}

	public Structure getStructure() {
		return (Structure) getProperty(STRUCTURE_PROPERTY);
	}

	public MetaData getMetaData() {
		return (MetaData) getProperty(METADATA_PROPERTY);
	}

	public PagedCorpusView getCorpusView() {
		return (PagedCorpusView) getProperty(VIEW_PROPERTY);
	}
}
