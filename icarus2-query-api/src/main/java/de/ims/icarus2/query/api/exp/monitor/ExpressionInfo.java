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
package de.ims.icarus2.query.api.exp.monitor;

/**
 * @author Markus Gärtner
 *
 */
public final class ExpressionInfo {

	public static Builder builder() {
		return new Builder();
	}

	//TODO immutable counters for types of expressions (= nesting depth) and factory methods

	/*
	 * Goal:
	 *
	 * Define a class that can be used to track how deep the underlying expression
	 * stack/tree is and what kinds of nested expressions a given expression depends
	 * on.
	 */

//	private static final Set<String> members, variables, references;
//
//	private static final int c_children, c_ancestors, c_constants, c_literals,
//		c_methods, c_arrays;

	public static enum Field {
		/** Number of immediate child expressions */
		CHILD,
		/** Total number of expressions (transitively) nested within */
		ANCESTOR,
		/** Total number of constant expressions within {@link #ANCESTOR} */
		CONSTANT,
		STRING,
		INTEGER,
		FLOAT,
		METHOD,
		ARRAY,
		ANNOTATION,

	}

	public static enum Label {
		MEMBER,
		VARIABLE,
		REFERENCE,
		;
	}

	public static final class Builder {

	}
}
