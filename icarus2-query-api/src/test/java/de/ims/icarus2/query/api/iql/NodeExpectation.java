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
