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
package de.ims.icarus2.model.standard.members.layers.annotation.fixed;

import static de.ims.icarus2.util.lang.Primitives._boolean;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.layers.annotation.AbstractAnnotationStorage;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractFixedKeysBooleanStorage extends AbstractAnnotationStorage {

	private IndexLookup indexLookup;

	public AbstractFixedKeysBooleanStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		indexLookup = createIndexLookup(layer);
	}

	@Override
	public void removeNotify(AnnotationLayer layer) {
		super.removeNotify(layer);

		indexLookup = null;
	}

	protected IndexLookup createIndexLookup(AnnotationLayer layer) {
		return IndexLookup.defaultCreateIndexLookup(layer);
	}

	public IndexLookup getIndexLookup() {
		return indexLookup;
	}

	protected int checkKeyAndGetIndex(String key) {
		int index = indexLookup.indexOf(key);

		if(index==-1) {
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					Messages.mismatch("Unknown key", indexLookup.getAvailableKeysString(), key));
		}

		return index;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public Object getValue(Item item, String key) {
		return Boolean.valueOf(getBooleanValue(item, key));
	}

	protected abstract boolean getNoEntryValue(String key);

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		if(value==null) {
			value = _boolean(getNoEntryValue(key));
		}
		setBooleanValue(item, key, ((Boolean) value).booleanValue());
	}
}
