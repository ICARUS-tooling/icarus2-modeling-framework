/**
 *
 */
package de.ims.icarus2.util.tree;

/**
 * @author Markus Gärtner
 *
 */
public enum BracketStyle {

	ROUND('(', ')'),
	SQUARE('[', ']'),
	ANGLE('<', '>'),
	CURLY('{', '}'),
	;

	private BracketStyle(char openBracket, char closeBracket) {
		this.openBracket = openBracket;
		this.closeBracket = closeBracket;
	}

	public final char openBracket;
	public final char closeBracket;
}
