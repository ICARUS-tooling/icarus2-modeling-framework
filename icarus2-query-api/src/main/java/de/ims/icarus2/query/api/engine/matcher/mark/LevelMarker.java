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
/**
 *
 */
package de.ims.icarus2.query.api.engine.matcher.mark;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;

import de.ims.icarus2.util.LazyStore;

/**
 * @author Markus Gärtner
 *
 */
public enum LevelMarker {
	ROOT("isRoot"),
	NOT_ROOT("isNotRoot"),
	LEAFT("isLeaf"),
	NO_LEAF("isNoLeaf"),
	INTERMEDIATE("isIntermediate")
	;

	private LevelMarker(String label) {
		this.label = checkNotEmpty(label);
	}

	private final String label;

	public String getLabel() { return label; }

	private static final LazyStore<LevelMarker, String> store
			= new LazyStore<>(LevelMarker.class, LevelMarker::getLabel, String::toLowerCase);

	public static LevelMarker forName(String s) { return store.lookup(s); }

	public static boolean isValidName(String s) { return store.hasKey(s); }
}
