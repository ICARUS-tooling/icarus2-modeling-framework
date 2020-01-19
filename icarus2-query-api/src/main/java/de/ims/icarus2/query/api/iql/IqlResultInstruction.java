/**
 *
 */
package de.ims.icarus2.query.api.iql;

/**
 * @author Markus Gärtner
 *
 */
public class IqlResultInstruction extends IqlUnique {

	//TODO define actual content based on the antlr gramamr for results

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.RESULT_INSTRUCTION;
	}

}
