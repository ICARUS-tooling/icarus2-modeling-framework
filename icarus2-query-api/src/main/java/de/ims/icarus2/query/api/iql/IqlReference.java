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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Markus Gärtner
 *
 */
public class IqlReference extends IqlUnique {

	/**
	 * The basic identifier to be used for the variable without the type-specific
	 * prefix, such as '$' or '@'.
	 */
	@JsonProperty(IqlProperties.NAME)
	private String name;

	@JsonProperty(IqlProperties.REFERENCE_TYPE)
	private ReferenceType referenceType;

	public ReferenceType getReferenceType() { return referenceType; }

	public String getName() { return name; }

	public void setName(String name) { this.name = checkNotEmpty(name); }

	public void setReferenceType(ReferenceType referenceType) { this.referenceType = requireNonNull(referenceType); }

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(name, IqlProperties.NAME);
		checkNotNull(referenceType, "referenceType");
	}

	@Override
	public IqlType getType() { return IqlType.REFERENCE; }

	public enum ReferenceType {

		MEMBER("member", '$'),
		VARIABLE("variable", '@'),
		;

		private final String label;
		private final char prefix;

		private ReferenceType(String label, char prefix) {
			this.label = label;
			this.prefix = prefix;
		}

		public char getPrefix() {
			return prefix;
		}

		@JsonValue
		public String getLabel() {
			return label;
		}
	}

	@Deprecated
	public enum MemberType {

		ITEM("item"),
		EDGE("edge"),
		FRAGMENT("fragment"),
		CONTAINER("container"),
		STRUCTURE("structure"),
		;

		private final String label;

		private MemberType(String label) {
			this.label = label;
		}

		@JsonValue
		public String getLabel() {
			return label;
		}
	}
}
