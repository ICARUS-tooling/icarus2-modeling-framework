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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.lang.Primitives._int;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.util.LazyStore;

/**
 * @author Markus Gärtner
 *
 */
public enum GenerationMarker {
	GENERATION("isGeneration", 1),
	NOT_GENERATION("isNotGeneration", 1),
	GENERATION_AFTER("isGenerationAfter", 1),
	GENERATION_BEFORE("isGenerationBefore", 1),
	ANY_GENERATION("isAnyGeneration", 0),
	;

	private final String label;
	private final int argCount;

	private GenerationMarker(String label, int argCount) {
		this.label = checkNotEmpty(label);
		checkArgument(argCount>=0);
		this.argCount = argCount;
	}

	Position posAt(Position[] positions, int index) {
		if(index>=positions.length)
			throw new QueryException(GlobalErrorCode.INVALID_INPUT,
					String.format("No position available for index %d - %s needs %d arguments, %d provided",
							_int(index), label, _int(argCount), _int(positions.length)));
		return positions[index];
	}

	public int getArgCount() { return argCount; }

	public String getLabel() { return label; }

	private static final LazyStore<GenerationMarker, String> store
			= new LazyStore<>(GenerationMarker.class, GenerationMarker::getLabel, String::toLowerCase);

	public static GenerationMarker forName(String s) { return store.lookup(s); }

	public static boolean isValidName(String s) { return store.hasKey(s); }
}
