/**
 *
 */
package de.ims.icarus2.query.api.iql;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

/**
 * Helper class that uses {@link IqlQueryElement#getType()} to obtain type id information
 * when serializing objects and {@link IqlType#forId(String)} to map back from ids to type
 * information when deserializing JSON data.
 *
 * @author Markus Gärtner
 *
 */
public class IqlTypeIdResolver extends TypeIdResolverBase {

	/**
	 * @see com.fasterxml.jackson.databind.jsontype.TypeIdResolver#idFromValue(java.lang.Object)
	 */
	@Override
	public String idFromValue(Object value) {
		return ((IqlQueryElement)value).getType().getId();
	}

	/**
	 * @see com.fasterxml.jackson.databind.jsontype.TypeIdResolver#idFromValueAndType(java.lang.Object, java.lang.Class)
	 */
	@Override
	public String idFromValueAndType(Object value, Class<?> suggestedType) {
		return idFromValue(value);
	}

	/**
	 * @see com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase#typeFromId(com.fasterxml.jackson.databind.DatabindContext, java.lang.String)
	 */
	@Override
	public JavaType typeFromId(DatabindContext context, String id) throws IOException {
		IqlType type = IqlType.forId(id);
		return context.getTypeFactory().constructType(type.getType());
	}

	/**
	 * @see com.fasterxml.jackson.databind.jsontype.TypeIdResolver#getMechanism()
	 */
	@Override
	public Id getMechanism() {
		return Id.NAME;
	}

}
