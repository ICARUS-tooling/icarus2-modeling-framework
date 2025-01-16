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
