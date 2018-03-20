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

/**
 * @author Markus Gärtner
 *
 */
public class CorpusAdapter implements CorpusListener {

	/**
	 * @see de.ims.icarus2.model.api.events.CorpusListener#corpusChanged(de.ims.icarus2.model.api.events.CorpusEvent)
	 */
	@Override
	public void corpusChanged(CorpusEvent e) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.CorpusListener#contextAdded(de.ims.icarus2.model.api.events.CorpusEvent)
	 */
	@Override
	public void contextAdded(CorpusEvent e) {
		// no-op

	}

	/**
	 * @see de.ims.icarus2.model.api.events.CorpusListener#contextRemoved(de.ims.icarus2.model.api.events.CorpusEvent)
	 */
	@Override
	public void contextRemoved(CorpusEvent e) {
		// no-op
	}

	@Override
	public void layerAdded(CorpusEvent e) {
		// no-op
	}

	@Override
	public void layerRemoved(CorpusEvent e) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.CorpusListener#metaDataAdded(de.ims.icarus2.model.api.events.CorpusEvent)
	 */
	@Override
	public void metaDataAdded(CorpusEvent e) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.CorpusListener#metaDataRemoved(de.ims.icarus2.model.api.events.CorpusEvent)
	 */
	@Override
	public void metaDataRemoved(CorpusEvent e) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.CorpusListener#corpusSaved(de.ims.icarus2.model.api.events.CorpusEvent)
	 */
	@Override
	public void corpusSaved(CorpusEvent e) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.CorpusListener#memberStateChanged(de.ims.icarus2.model.api.events.CorpusEvent)
	 */
	@Override
	public void memberStateChanged(CorpusEvent e) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.CorpusListener#corpusPartCreated(de.ims.icarus2.model.api.events.CorpusEvent)
	 */
	@Override
	public void corpusPartCreated(CorpusEvent e) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.CorpusListener#corpusPartDestroyed(de.ims.icarus2.model.api.events.CorpusEvent)
	 */
	@Override
	public void corpusPartDestroyed(CorpusEvent e) {
		// no-op
	}

}
