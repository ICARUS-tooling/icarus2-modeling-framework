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

	@JsonProperty(value=IqlProperties.CONTENT, required=true)
	private String content;

	@JsonProperty(IqlProperties.RETURN_TYPE)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<Class<?>> returnType = Optional.empty();

	@Override
	public IqlType getType() { return IqlType.EXPRESSION; }

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
