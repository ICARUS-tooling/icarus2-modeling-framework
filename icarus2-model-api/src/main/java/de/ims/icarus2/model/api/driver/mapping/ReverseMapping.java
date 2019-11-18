/**
 *
 */
package de.ims.icarus2.model.api.driver.mapping;

/**
 * @author Markus Gärtner
 *
 */
public interface ReverseMapping extends Mapping {

	/**
	 * Returns the {@link Mapping} instance used by this implementation to
	 * perform the final part of the lookup.
	 * @return
	 */
	Mapping getInverseMapping();
}
