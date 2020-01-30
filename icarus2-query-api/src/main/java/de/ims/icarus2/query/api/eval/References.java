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
/**
 *
 */
package de.ims.icarus2.query.api.eval;

/**
 * @author Markus Gärtner
 *
 */
public final class References {

	private References() { /* no-op */ }

	public static enum ReferenceType {
		REFERENCE(false, true),
		MEMBER(false, false),
		VARIABLE(true, false),
		;

		private final boolean assignable, constant;

		private ReferenceType(boolean assignable, boolean constant) {
			this.assignable = assignable;
			this.constant = constant;
		}
		/** Signals whether or not the underlying object reference can ever be changed. */
		public boolean isAssignable() { return assignable; }

		/** Signals whether or not the underlying object reference will ever change. */
		public boolean isConstant() { return constant; }
	}
}
