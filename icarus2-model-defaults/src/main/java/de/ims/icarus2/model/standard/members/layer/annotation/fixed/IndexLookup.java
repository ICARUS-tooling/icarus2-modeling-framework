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
package de.ims.icarus2.model.standard.members.layer.annotation.fixed;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Set;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;

/**
 * Implements the actual key storage and provides a sorted view
 * on the available keys. In addition the implementation specific
 * {@link #indexOf(String)} method provides efficient reverse mapping
 * from a key string to its respective index in the sorted keys
 * storage.
 *
 * @author Markus Gärtner
 *
 */
public abstract class IndexLookup {


	public static IndexLookup defaultCreateIndexLookup(AnnotationLayer layer) {
		AnnotationLayerManifest manifest = layer.getManifest();
		Set<String> keySet = manifest.getAvailableKeys();

		String[] keys = new String[keySet.size()];
		keySet.toArray(keys);

		IndexLookup indexLookup = null;

		if(keys.length<=8) {
			indexLookup = new BinarySearchLookup(keys);
		} else {
			indexLookup = new MappedLookup(keys);
		}

		return indexLookup;
	}

	protected final String[] keys;

	protected IndexLookup(String[] keys) {
		requireNonNull(keys);

		Arrays.sort(keys);

		this.keys = keys;
	}

	public abstract int indexOf(String key);

	public int keyCount() {
		return keys.length;
	}

	public String keyAt(int index) {
		return keys[index];
	}

	public String getAvailableKeysString() {
		String[] keys = new String[keyCount()];
		for(int i=0; i<keys.length; i++) {
			keys[i] = keyAt(i);
		}

		return Arrays.toString(keys);
	}
}