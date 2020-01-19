/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlData extends IqlUnique {

	@JsonProperty(IqlProperties.CONTENT)
	private String content;

	@JsonProperty(IqlProperties.VARIABLE)
	private String variable;

	@JsonProperty(IqlProperties.CHECKSUM)
	@JsonInclude(Include.NON_EMPTY)
	private Optional<String> checksum = Optional.empty();

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.DATA;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlUnique#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(content, IqlProperties.CONTENT);
		checkStringNotEmpty(variable, IqlProperties.VARIABLE);

		checkOptionalStringNotEmpty(checksum, IqlProperties.CHECKSUM);
	}

	public String getContent() { return content; }

	public String getVariable() { return variable; }

	public Optional<String> getChecksum() { return checksum; }

	public void setContent(String content) { this.content = requireNonNull(content); }

	public void setVariable(String variable) { this.variable = requireNonNull(variable); }

	public void setChecksum(String checksum) { this.checksum = Optional.of(checkNotEmpty(checksum)); }
}
