/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base type for unique elements that are referenced via a explicit name.
 *
 * @author Markus Gärtner
 *
 */
public abstract class IqlNamedReference extends IqlUnique {

	@JsonProperty(IqlProperties.NAME)
	private String name;

	public String getName() { return name; }

	public void setName(String name) { this.name = checkNotEmpty(name); }

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(name, IqlProperties.NAME);
	}
}
