/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.apiguard.DataObject;
import de.ims.icarus2.query.api.QueryErrorCode;

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
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE)
public interface IqlQueryElement extends DataObject {

	/**
	 * Return the type information for this element, i.e. the {@link IqlType#getId() type id}
	 * to be used when serializing. Note that the {@link IqlType#getType() type} reported by the
	 * returned {@link IqlType} instance must be the same as the actual implementation of this
	 * element or a super class of it!
	 *
	 * @return the type information for serialization of this element.
	 */
	IqlType getType();

	/**
	 * Verifies the integrity of this element and (recursively) all of its contained child
	 * elements. This is meant to allow easy sanity checks before and after serialization.
	 *
	 * @throws IcarusRuntimeException of type {@link QueryErrorCode#CORRUPTED_QUERY} iff
	 * any of the obligatory fields are missing or any contained data does not conform to
	 * the requirements of the element.
	 */
	void checkIntegrity();
}
