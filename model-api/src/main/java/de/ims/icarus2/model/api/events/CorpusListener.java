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
 * $Revision: 392 $
 *
 */
package de.ims.icarus2.model.api.events;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.members.item.Item;

/**
 *
 * @author Markus Gärtner
 *
 */
public interface CorpusListener {

	/**
	 * A general change in the corpus occurred.
	 * <p>
	 * This event fires at descriptor based changes for manifests
	 * from the layer level upwards.
	 * The {@code "manifest"} property holds the {@code MemberManifest}
	 * that was changed.
	 *
	 * @param e
	 */
	void corpusChanged(CorpusEvent e);

	/**
	 * Indicates that the corpus and all it's context objects have
	 * been saved (i.e. saved to a permanent storage). This event
	 * only fires for corpora that are editable.
	 *
	 * @param e
	 */
	void corpusSaved(CorpusEvent e);

	/**
	 * A new {@link Context} was added to the corpus.
	 * <p>
	 * The {@code "context"} property holds the {@code Context}
	 * that was added.
	 * <p>
	 * This event fires either when a virtual context holding only highlight
	 * information gets added to a corpus or when a corpus lazily initializes
	 * and connects a regular context.
	 *
	 * @param e
	 */
	void contextAdded(CorpusEvent e);

	/**
	 * A {@link Context} was removed from the corpus
	 * <p>
	 * The {@code "context"} property holds the {@code Context}
	 * that was removed.
	 * <p>
	 * This event fires either when a virtual context gets removed from the corpus
	 * or when a corpus shuts down and closes/disconnects all active contexts.
	 *
	 * @param e
	 */
	void contextRemoved(CorpusEvent e);

	/**
	 * A new layer was added to the corpus.
	 * <p>
	 * The {@code "layer"} property holds the {@code Layer}
	 * that was added.
	 *
	 * @param e
	 */
	void layerAdded(CorpusEvent e);

	/**
	 * A layer was removed from the corpus.
	 * <p>
	 * The {@code "layer"} property holds the {@code Layer}
	 * that was removed.
	 *
	 * @param e
	 */
	void layerRemoved(CorpusEvent e);
//
	/**
	 * A (top level) member was changed.
	 * <p>
	 * This event is used to signal state changes resulting from user made modifications
	 * to the corpus. It is advised that driver implementations use this event to forward
	 * internal state changes that might affect the behavior and/or visualization of data
	 * they hold. For example adding a new item representing a word to a token layer would
	 * mark affected elements on sentence and document level as {@link Item#isDirty() dirty}
	 * and this change in state should be published with this event (for the top most members
	 * that are affected).
	 * <p>
	 * The {@code "member"} property holds the {@code CorpusMember}
	 * that was changed.
	 *
	 * @param e
	 */
	void memberStateChanged(CorpusEvent e);

	/**
	 * A new {@code MetaData} object was added to the corpus
	 * <p>
	 * The {@code "metadata"} property holds the {@code MetaData}
	 * that was added.
	 * <br>
	 * The {@code "layer"} property holds the {@code Layer} the
	 * meta-data has been added for.
	 *
	 * @param e
	 */
	void metaDataAdded(CorpusEvent e);

	/**
	 * An existing {@code MetaData} object was removed from the corpus
	 * <p>
	 * The {@code "metadata"} property holds the {@code MetaData}
	 * that was removed.
	 * <br>
	 * The {@code "layer"} property holds the {@code Layer} the
	 * meta-data has been removed from.
	 *
	 * @param e
	 */
	void metaDataRemoved(CorpusEvent e);

	void corpusPartCreated(CorpusEvent e);

	void corpusPartDestroyed(CorpusEvent e);
}
