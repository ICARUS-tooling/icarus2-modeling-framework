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

	/** Defines how to interpret the 'content' string */
	@JsonProperty(IqlProperties.CODEC)
	private String codec;

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
		checkStringNotEmpty(codec, IqlProperties.CODEC);
		checkStringNotEmpty(variable, IqlProperties.VARIABLE);

		checkOptionalStringNotEmpty(checksum, IqlProperties.CHECKSUM);
	}

	public String getContent() { return content; }

	public String getCodec() { return codec; }

	public String getVariable() { return variable; }

	public Optional<String> getChecksum() { return checksum; }

	public void setContent(String content) { this.content = requireNonNull(content); }

	public void setCodec(String codec) { this.codec = requireNonNull(codec); }

	public void setVariable(String variable) { this.variable = requireNonNull(variable); }

	public void setChecksum(String checksum) { this.checksum = Optional.of(checkNotEmpty(checksum)); }
}
