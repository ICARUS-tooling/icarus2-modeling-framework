/**
 *
 */
package de.ims.icarus2.query.api.iql;

/**
 * @author Markus Gärtner
 *
 */
public class IqlCorpus extends IqlAliasedReference {

	//TODO extra fields?

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.CORPUS;
	}
}
