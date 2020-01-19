/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;

import java.util.Optional;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlExpression extends AbstractIqlQueryElement {

	@JsonProperty(IqlProperties.CONTENT)
	private String content;

	@JsonProperty(IqlProperties.RETURN_TYPE)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<Class<?>> returnType = Optional.empty();

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.EXPRESSION;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.AbstractIqlQueryElement#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(content, IqlProperties.CONTENT);
	}

	public String getContent() { return content; }

	@Nullable
	public Optional<Class<?>> getReturnType() { return returnType; }

	public void setContent(String content) { this.content = checkNotEmpty(content); }

	public void setReturnType(Class<?> returnType) { this.returnType = Optional.of(returnType); }

}
