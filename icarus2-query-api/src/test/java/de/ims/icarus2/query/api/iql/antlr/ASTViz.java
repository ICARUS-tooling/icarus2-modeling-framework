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
package de.ims.icarus2.query.api.iql.antlr;

import static de.ims.icarus2.query.api.iql.antlr.IQLTestUtils.createParser;
import static de.ims.icarus2.util.collections.CollectionUtils.list;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * @author Markus Gärtner
 *
 */
public class ASTViz {

	public static void main(String[] args) {
		IQL_TestParser parser = createParser("[]---[]", "", null, true);
		ParseTree tree = parser.nodeStatement();
		TreeViewer viewer = new TreeViewer(list(parser.getRuleNames()), tree);
		viewer.open();
	}
}
