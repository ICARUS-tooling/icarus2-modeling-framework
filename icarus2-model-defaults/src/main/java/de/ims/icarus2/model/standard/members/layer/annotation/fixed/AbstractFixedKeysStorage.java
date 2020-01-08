/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.layer.annotation.fixed;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.layer.annotation.AbstractObjectMapStorage;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractFixedKeysStorage<B extends Object> extends AbstractObjectMapStorage<B> {

	private IndexLookup indexLookup;
	private B noEntryValues;

	public AbstractFixedKeysStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		indexLookup = createIndexLookup(layer);
		noEntryValues = createNoEntryValues(layer, indexLookup);
	}

	protected abstract B createNoEntryValues(AnnotationLayer layer, IndexLookup indexLookup);

	@Override
	public void removeNotify(AnnotationLayer layer) {
		super.removeNotify(layer);

		indexLookup = null;
		noEntryValues = null;
	}

	@Override
	protected B getFallbackBuffer() {
		return noEntryValues;
	}

	protected B getNoEntryValues() {
		return noEntryValues;
	}

	protected IndexLookup createIndexLookup(AnnotationLayer layer) {
		return IndexLookup.defaultCreateIndexLookup(layer);
	}

	public IndexLookup getIndexLookup() {
		return indexLookup;
	}

	protected int checkKeyAndGetIndex(String key) {
		requireNonNull(key);
		int index = indexLookup.indexOf(key);

		if(index==-1)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					Messages.mismatch("Unknown key", indexLookup.getAvailableKeysString(), key));

		return index;
	}

	public int getKeyCount() {
		return indexLookup.keyCount();
	}
}
