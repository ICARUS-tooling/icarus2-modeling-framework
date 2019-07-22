/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

//	public enum LifecycleChange {
//		/**
//		 * The given {@code corpus} was connected successfully and can now be interacted with.
//		 */
//		CONNECTED,
//		/**
//		 * The specified {@code corpus} was disconnected successfully and is no longer available
//		 * for interaction. All previously held references of the former live corpus should be
//		 * discarded immediately, since their behavior might be unpredictable or unstable.
//		 */
//		DOSCONNECTED,
//		/**
//		 * The given {@code corpus} was previously disabled and now got enabled again.
//		 */
//		ENABLED,
//		/**
//		 * The given {@code corpus} was previously enabled and now got disabled by client code.
//		 */
//		DISABLED
//		;
//	}
}
