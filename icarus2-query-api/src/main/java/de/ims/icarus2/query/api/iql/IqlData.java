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

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Markus Gärtner
 *
 */
public class IqlData extends IqlNamedReference {

	public static final String DEFAULT_CODEC = "hex";

	@JsonProperty(value=IqlTags.CONTENT, required=true)
	private String content;

	/** Defines how to interpret the 'content' string. Defaults to {@link #DEFAULT_CODEC}. */
	@JsonProperty(value=IqlTags.CODEC)
	@JsonInclude(Include.NON_DEFAULT)
	private String codec = DEFAULT_CODEC;

	@JsonProperty(IqlTags.CHECKSUM)
	@JsonInclude(Include.NON_EMPTY)
	private Optional<String> checksum = Optional.empty();

	@JsonProperty(IqlTags.CHECKSUM_TYPE)
	@JsonInclude(Include.NON_EMPTY)
	private Optional<ChecksumType> checksumType = Optional.empty();

	@Override
	public IqlType getType() { return IqlType.DATA; }

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(content, IqlTags.CONTENT);
		checkStringNotEmpty(codec, IqlTags.CODEC);

		checkOptionalStringNotEmpty(checksum, IqlTags.CHECKSUM);
	}

	public String getContent() { return content; }

	public String getCodec() { return codec; }

	public Optional<String> getChecksum() { return checksum; }

	public Optional<ChecksumType> getChecksumType() { return checksumType; }

	public void setContent(String content) { this.content = requireNonNull(content); }

	public void setCodec(String codec) { this.codec = checkNotEmpty(codec); }

	public void setChecksum(String checksum) { this.checksum = Optional.of(checkNotEmpty(checksum)); }

	public void setChecksumType(ChecksumType checksumType) { this.checksumType = Optional.of(checksumType); }

	public enum ChecksumType {
		MD5("MD5"),
		;

		private final String label;

		private ChecksumType(String label) { this.label = label; }

		@JsonValue
		public String getLabel() { return label; }
	}
}
