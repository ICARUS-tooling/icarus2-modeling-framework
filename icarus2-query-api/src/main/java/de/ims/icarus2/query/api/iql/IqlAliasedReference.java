/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public abstract class IqlAliasedReference extends IqlUnique {

	@JsonProperty(IqlProperties.NAME)
	private String name;

	@JsonProperty(IqlProperties.ALIAS)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<String> alias = Optional.empty();

	public String getName() { return name; }

	public Optional<String> getAlias() { return alias; }

	public void setName(String name) { this.name = checkNotEmpty(name); }

	public void setAlias(String alias) { this.alias = Optional.of(checkNotEmpty(alias)); }

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlUnique#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(name, IqlProperties.NAME);

		checkOptionalStringNotEmpty(alias, IqlProperties.ALIAS);
	}
}
