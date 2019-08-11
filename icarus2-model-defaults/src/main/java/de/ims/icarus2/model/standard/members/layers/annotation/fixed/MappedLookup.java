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

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 *
 * @author Markus Gärtner
 *
 */
public class MappedLookup extends IndexLookup {

	protected final Object2IntMap<String> map;

	/**
	 * @param keys
	 */
	public MappedLookup(String[] keys) {
		super(keys);

		map = new Object2IntOpenHashMap<>(keys.length<<1);
		map.defaultReturnValue(UNSET_INT);

		for(int i=0; i<keyCount(); i++) {
			map.put(keyAt(i), i);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layers.annotation.fixed.IndexLookup#indexOf(java.lang.String)
	 */
	@Override
	public int indexOf(String key) {
		return map.getInt(key);
	}

}