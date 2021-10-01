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

import java.util.function.Function;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.exp.EvaluationUtils;
import de.ims.icarus2.util.LazyStore;

/**
 * @author Markus Gärtner
 *
 */
public class GenerationMarker {

	public static boolean isGenerationMarker(String name) {
		return Type.isValidName(name);
	}

	public static boolean isLevelMarker(String name) {
		return Type.isValidName(name) && !Type.parseName(name).isRequiresClosure();
	}

	public static Type typeFor(String name) {
		return Type.parseName(name);
	}

	public static enum Type {
		ROOT("isRoot", 0, false),
		NOT_ROOT("isNotRoot", 0, false),
		LEAFT("isLeaf", 0, false),
		NO_LEAF("isNoLeaf", 0, false),
		INTERMEDIATE("isIntermediate", 0, false),
		GENERATION("isGeneration", 1, true),
		NOT_GENERATION("isNotGeneration", 1, true),
		GENERATION_AFTER("isGenerationAfter", 1, true),
		GENERATION_BEFORE("isGenerationBefore", 1, true),
		ANY_GENERATION("isAnyGeneration", 0, true),
		;

		private final String label;
		private final int argCount;
		private final boolean requiresClosure;

		private Type(String label, int argCount, boolean requiresClosure) {
			this.label = checkNotEmpty(label);
			checkArgument(argCount>=0);
			this.argCount = argCount;
			this.requiresClosure = requiresClosure;
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

		public boolean isRequiresClosure() { return requiresClosure; }

		private static final LazyStore<Type, String> store
				= new LazyStore<>(Type.class, Type::getLabel, String::toLowerCase);

		private static final Function<String, RuntimeException> ERROR
			= name -> EvaluationUtils.forUnknownIdentifier(name, "marker");

		public static Type parseName(String s) { return store.lookup(s, ERROR); }

		public static boolean isValidName(String s) { return store.hasKey(s); }
	}
}
