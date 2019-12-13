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
/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static java.util.Objects.requireNonNull;

import org.antlr.v4.runtime.tree.ParseTree;

/**
 * @author Markus Gärtner
 *
 */
public class NodeExpectation {

	public static NodeExpectation of(Class<? extends ParseTree> nodeClass, String text) {
		return new NodeExpectation(nodeClass, text);
	}

	public static NodeExpectation of(Class<? extends ParseTree> nodeClass) {
		return new NodeExpectation(nodeClass, null);
	}

	public final Class<? extends ParseTree> nodeClass;
	public final String text;

	private NodeExpectation(Class<? extends ParseTree> nodeClass, String text) {
		this.nodeClass = requireNonNull(nodeClass);
		this.text = text;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return nodeClass.getSimpleName() + ":" +(text==null ? "?" : "<"+text+">");
	}
}
