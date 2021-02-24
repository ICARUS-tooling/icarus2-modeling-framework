/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
