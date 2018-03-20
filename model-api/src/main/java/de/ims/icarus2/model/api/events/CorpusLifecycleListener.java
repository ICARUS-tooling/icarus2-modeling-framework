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
public interface CorpusLifecycleListener {

	/**
	 * Called when a state transition is initiated, meaning the corpus is requested
	 * to either {@link CorpusManager#connect(CorpusManifest) connect} or
	 * {@link CorpusManager#disconnect(CorpusManifest) disconnect}. In addition this
	 * event is fired when a corpus failed one of above mentioned requests and as a
	 * result got marked as {@link CorpusManager#isBadCorpus(CorpusManifest) bad} by
	 * the manager.
	 *
	 * @param manager the {@link CorpusManager} manager that is responsible for the new corpus
	 * @param corpus the manifest of the corpus that changed
	 */
	void corpusChanged(CorpusManager manager, CorpusManifest corpus);

	/**
	 * The given {@code corpus} was connected successfully and can now be interacted with.
	 *
	 * @param manager the {@link CorpusManager} manager that is responsible for the new corpus
	 * @param corpus the corpus the manager connected to successfully
	 */
	void corpusConnected(CorpusManager manager, Corpus corpus);

	/**
	 * The specified {@code corpus} was disconnected successfully and is no longer available
	 * for interaction. All previously held references of the previous live corpus should be
	 * discarded immediately, since their behavior might be unpredictable or unstable.
	 *
	 * @param manager the {@link CorpusManager} manager that is responsible for the new corpus
	 * @param corpus the manifest of the corpus the manager disconnected from
	 */
	void corpusDisconnected(CorpusManager manager, CorpusManifest corpus);

	/**
	 * The given {@code corpus} was previously disabled and now got enabled again.
	 *
	 * @param manager the {@link CorpusManager} manager that is responsible for the new corpus
	 * @param corpus the manifest of the corpus that got (re-)enabled
	 */
	void corpusEnabled(CorpusManager manager, CorpusManifest corpus);

	/**
	 * The given {@code corpus} was previously enabled and now got disabled by client code.
	 *
	 * @param manager the {@link CorpusManager} manager that is responsible for the new corpus
	 * @param corpus the manifest of the corpus that got disabled
	 */
	void corpusDisabled(CorpusManager manager, CorpusManifest corpus);
}
