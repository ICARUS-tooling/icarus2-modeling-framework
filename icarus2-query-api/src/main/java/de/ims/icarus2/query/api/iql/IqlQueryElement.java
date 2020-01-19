/**
 *
 */
package de.ims.icarus2.query.api.iql;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

/**
 *
 *
 * @author Markus Gärtner
 *
 */
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = IqlConstants.TYPE_PROPERTY
)
@JsonTypeIdResolver(IqlTypeIdResolver.class)
public interface IqlQueryElement {

	/**
	 * Return the type information for this element, i.e. the {@link IqlType#getId() type id}
	 * to be used when serializing. Note that the {@link IqlType#getType() type} reported by the
	 * returned {@link IqlType} instance must be the same as the actual implementation of this
	 * element or a super class of it!
	 *
	 * @return the type information for serialization of this element.
	 */
	IqlType getType();
}
