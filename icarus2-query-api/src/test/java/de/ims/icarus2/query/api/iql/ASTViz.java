/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.query.api.iql.IQLTestUtils.createParser;
import static de.ims.icarus2.util.collections.CollectionUtils.list;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.tree.ParseTree;

import de.ims.icarus2.query.api.iql.antlr.IQL_TestParser;

/**
 * @author Markus Gärtner
 *
 */
public class ASTViz {

	public static void main(String[] args) {
		IQL_TestParser parser = createParser("[]---[]", "", null, true);
		ParseTree tree = parser.graphStatement();
		TreeViewer viewer = new TreeViewer(list(parser.getRuleNames()), tree);
		viewer.open();
	}
}
