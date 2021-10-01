/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.HashSet;
import java.util.Set;

import de.ims.icarus2.query.api.iql.IqlMarker;

/**
 * @author Markus Gärtner
 *
 */
public class GenerationMarker {

	private static final Set<String> names = new HashSet<>();

	private static void registerName(String name) {
		String key = name.toLowerCase();
		if(!names.add(key))
			throw new InternalError("Duplicate name: "+name);
	}

	static {
		registerName("isGeneration");
		registerName("isNotGeneration");
		registerName("isGenerationAfter");
		registerName("isGenerationBefore");
		registerName("isAnyGeneration");
	}

	public static boolean isValidName(String s) {
		return names.contains(s.toLowerCase());
	}

	public static boolean containsGenerationMarker(IqlMarker marker) {
		//TODO implement full scan of marker tree
		throw new UnsupportedOperationException();
	}
}