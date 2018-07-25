/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
