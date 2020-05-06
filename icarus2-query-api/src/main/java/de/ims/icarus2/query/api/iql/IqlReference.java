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

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Markus Gärtner
 *
 */
public class IqlReference extends IqlNamedReference {

	@JsonProperty(value=IqlProperties.REFERENCE_TYPE, required=true)
	private ReferenceType referenceType;

	public ReferenceType getReferenceType() { return referenceType; }

	public void setReferenceType(ReferenceType referenceType) { this.referenceType = requireNonNull(referenceType); }

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkNotNull(referenceType, "referenceType");
	}

	@Override
	public IqlType getType() { return IqlType.REFERENCE; }

	public enum ReferenceType {

		//TODO REFERENCE is not used?
		REFERENCE("reference", '\0', false, true, true),
		MEMBER("member", '$', false, false, true),
		VARIABLE("variable", '@', true, false, false),
		;

		private final String label;
		private final char prefix;
		private final boolean assignable, constant, autoAssigned;

		private ReferenceType(String label, char prefix, boolean assignable,
				boolean constant, boolean autoAssigned) {
			this.label = label;
			this.prefix = prefix;
			this.assignable = assignable;
			this.constant = constant;
			this.autoAssigned = autoAssigned;
		}

		public char getPrefix() { return prefix; }

		@JsonValue
		public String getLabel() { return label; }

		/** Signals whether or not the underlying object reference can ever be changed. */
		public boolean isAssignable() { return assignable; }

		/** Signals whether or not the underlying object reference will ever change. */
		public boolean isConstant() { return constant; }

		/** Signals whether the reference will be automatically assigned by the framework. */
		public boolean isAutoAssigned() { return autoAssigned; }
	}
}
