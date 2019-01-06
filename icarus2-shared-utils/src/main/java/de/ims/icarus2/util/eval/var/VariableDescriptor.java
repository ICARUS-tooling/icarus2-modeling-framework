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
package de.ims.icarus2.util.eval.var;

import static java.util.Objects.requireNonNull;

/**
 * @author Markus Gärtner
 *
 */
public class VariableDescriptor {

	public static enum Mode {
		IN(true, false),
		OUT(false, true),
		IN_OUT(true, true),
		;

		private final boolean in, out;

		private Mode(boolean in, boolean out) {
			this.in = in;
			this.out = out;
		}

		public boolean isIn() {
			return in;
		}

		public boolean isOut() {
			return out;
		}
	}

	private final String name;
	private final boolean nullable;
	private final Mode mode;

	private final Class<?> namespaceClass;


	public VariableDescriptor(String name, Class<?> namespaceClass, Mode mode, boolean nullable) {
		requireNonNull(name);
		requireNonNull(namespaceClass);
		requireNonNull(mode);

		this.name = name;
		this.namespaceClass = namespaceClass;
		this.mode = mode;
		this.nullable = nullable;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the namespaceClass
	 */
	public Class<?> getNamespaceClass() {
		return namespaceClass;
	}

	/**
	 * @return the mode
	 */
	public Mode getMode() {
		return mode;
	}

	/**
	 * @return the nullable
	 */
	public boolean isNullable() {
		return nullable;
	}
}
