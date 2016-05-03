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

 * $Revision: 396 $
 * $Date: 2015-05-20 11:11:11 +0200 (Mi, 20 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/events/CorpusLifecycleListener.java $
 *
 * $LastChangedDate: 2015-05-20 11:11:11 +0200 (Mi, 20 Mai 2015) $
 * $LastChangedRevision: 396 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.events;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.registry.CorpusManager;

/**
 * @author Markus Gärtner
 * @version $Id: CorpusLifecycleListener.java 396 2015-05-20 09:11:11Z mcgaerty $
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
	 * @param manager TODO
	 * @param corpus
	 */
	void corpusChanged(CorpusManager manager, CorpusManifest corpus);

	/**
	 * The given {@code corpus} was connected successfully and can now be interacted with.
	 * @param manager TODO
	 * @param corpus
	 */
	void corpusConnected(CorpusManager manager, Corpus corpus);

	/**
	 * The specified {@code corpus} was disconnected successfully and is no longer available
	 * for interaction. All previously held references of the previous live corpus should be
	 * discarded immediately, since their behavior might be unpredictable or unstable.
	 * @param manager TODO
	 * @param corpus
	 */
	void corpusDisconnected(CorpusManager manager, CorpusManifest corpus);

	/**
	 * The given {@code corpus} was previously disabled and now got enabled again.
	 * @param manager TODO
	 * @param corpus
	 */
	void corpusEnabled(CorpusManager manager, CorpusManifest corpus);

	/**
	 * The given {@code corpus} was previously enabled and now got disabled by client code.
	 * @param manager TODO
	 * @param corpus
	 */
	void corpusDisabled(CorpusManager manager, CorpusManifest corpus);
}
