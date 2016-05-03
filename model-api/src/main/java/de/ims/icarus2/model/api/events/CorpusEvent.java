/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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
 * $Revision: 398 $
 * $Date: 2015-05-29 11:29:49 +0200 (Fr, 29 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/events/CorpusEvent.java $
 *
 * $LastChangedDate: 2015-05-29 11:29:49 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 398 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.events;

import java.util.Hashtable;
import java.util.Map;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.CorpusView;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.meta.MetaData;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 *
 * @author Markus Gärtner
 * @version $Id: CorpusEvent.java 398 2015-05-29 09:29:49Z mcgaerty $
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

	public CorpusView getCorpusView() {
		return (CorpusView) getProperty(VIEW_PROPERTY);
	}
}
