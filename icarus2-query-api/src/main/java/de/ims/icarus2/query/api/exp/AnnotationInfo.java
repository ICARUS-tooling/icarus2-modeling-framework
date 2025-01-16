/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.exp;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.AbstractBuilder;

/**
 *
 * @author Markus Gärtner
 *
 */
public class AnnotationInfo {

	public static Builder builer() { return new Builder(); }

	private final String rawKey;
	private final String key;
	private final ValueType valueType;
	private final TypeInfo type;

	private Function<Item, Object> objectSource;
	private ToLongFunction<Item> integerSource;
	private ToDoubleFunction<Item> floatingPointSource;
	private Predicate<Item> booleanSource;

	private AnnotationInfo(Builder builder) {
		rawKey = builder.rawKey;
		key = builder.key;
		valueType = builder.valueType;
		type = builder.type;
		objectSource = builder.objectSource;
		integerSource = builder.integerSource;
		floatingPointSource = builder.floatingPointSource;
		booleanSource = builder.booleanSource;
	}

	public String getRawKey() { return rawKey; }

	public String getKey() { return key; }

	public ValueType getValueType() { return valueType; }

	public TypeInfo getType() { return type; }


	public Function<Item, Object> getObjectSource() {
		checkState("No object source defined", objectSource!=null);
		return objectSource;
	}

	public ToLongFunction<Item> getIntegerSource() {
		checkState("No integer source defined", integerSource!=null);
		return integerSource;
	}

	public ToDoubleFunction<Item> getFloatingPointSource() {
		checkState("No floating point source defined", floatingPointSource!=null);
		return floatingPointSource;
	}

	public Predicate<Item> getBooleanSource() {
		checkState("No boolean source defined", booleanSource!=null);
		return booleanSource;
	}

	public static class Builder extends AbstractBuilder<Builder, AnnotationInfo> {
		private String rawKey;
		private String key;
		private ValueType valueType;
		private TypeInfo type;

		private Function<Item, Object> objectSource;
		private ToLongFunction<Item> integerSource;
		private ToDoubleFunction<Item> floatingPointSource;
		private Predicate<Item> booleanSource;

		private Builder() { /* no-op */ }

		@Override
		protected void validate() {
			checkState("no raw key defined", rawKey!=null);
			checkState("no key defined", key!=null);
			checkState("no value type defined", valueType!=null);
			checkState("no type info defined", type!=null);

			checkState("must define at least one source", objectSource!=null
					|| integerSource!=null || floatingPointSource!=null || booleanSource!=null);
		}

		public Builder rawKey(String rawKey) {
			requireNonNull(rawKey);
			checkState("raw key already set", this.rawKey==null);
			this.rawKey = rawKey;
			return this;
		}

		public Builder key(String key) {
			requireNonNull(key);
			checkState("key already set", this.key==null);
			this.key = key;
			return this;
		}

		public Builder valueType(ValueType valueType) {
			requireNonNull(valueType);
			checkState("raw keyvalue typealready set", this.valueType==null);
			this.valueType = valueType;
			return this;
		}

		public Builder type(TypeInfo type) {
			requireNonNull(type);
			checkState("type info already set", this.type==null);
			this.type = type;
			return this;
		}

		public Builder objectSource(Function<Item, Object> objectSource) {
			requireNonNull(objectSource);
			checkState("object source already set", this.objectSource==null);
			this.objectSource = objectSource;
			return this;
		}

		public Builder integerSource(ToLongFunction<Item> integerSource) {
			requireNonNull(integerSource);
			checkState("integer source already set", this.integerSource==null);
			this.integerSource = integerSource;
			return this;
		}

		public Builder floatingPointSource(ToDoubleFunction<Item> floatingPointSource) {
			requireNonNull(floatingPointSource);
			checkState("floating point source already set", this.floatingPointSource==null);
			this.floatingPointSource = floatingPointSource;
			return this;
		}

		public Builder booleanSource(Predicate<Item> booleanSource) {
			requireNonNull(booleanSource);
			checkState("boolean source already set", this.booleanSource==null);
			this.booleanSource = booleanSource;
			return this;
		}

		@Override
		protected AnnotationInfo create() {
			return new AnnotationInfo(this);
		}
	}
}