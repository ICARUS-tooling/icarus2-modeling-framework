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

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.manifest.api.CorpusManifest;

/**
 * @author Markus Gärtner
 *
 */
public class CorpusLifecycleAdapter implements CorpusLifecycleListener {

	/**
	 * @see de.ims.icarus2.model.api.events.CorpusLifecycleListener#corpusConnected(CorpusManager, de.ims.icarus2.model.api.corpus.Corpus)
	 */
	@Override
	public void corpusConnected(CorpusManager manager, Corpus corpus) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.CorpusLifecycleListener#corpusDisconnected(CorpusManager, de.ims.icarus2.model.api.corpus.Corpus)
	 */
	@Override
	public void corpusDisconnected(CorpusManager manager, CorpusManifest corpus) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.CorpusLifecycleListener#corpusEnabled(CorpusManager, de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public void corpusEnabled(CorpusManager manager, CorpusManifest corpus) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.CorpusLifecycleListener#corpusDisabled(CorpusManager, de.ims.icarus2.model.manifest.api.CorpusManifest)
	 */
	@Override
	public void corpusDisabled(CorpusManager manager, CorpusManifest corpus) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.CorpusLifecycleListener#corpusChanged(CorpusManager, de.ims.icarus2.model.api.corpus.Corpus)
	 */
	@Override
	public void corpusChanged(CorpusManager manager, CorpusManifest corpus) {
		// no-op
	}

}
