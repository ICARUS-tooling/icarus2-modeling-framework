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
package de.ims.icarus2.query.api.exp;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.strings.NamedObject;

/**
 * @author Markus Gärtner
 *
 */
public final class References {

	private References() { /* no-op */ }

	public static Assignable<?> variable(String name) {
		//TODO implement
		//TODO make sure any implementation uses context.getVariable(name) if called to duplicate!
		throw new UnsupportedOperationException();
	}

	public static Expression<?> payload(String name, byte[] data) {
		return new Payload(name, data);
	}

	public static Assignable<? extends Item> member(String name, TypeInfo type) {
		return new Member(name, type);
	}

	static final class Member implements Assignable<Item> {
		private Item item;
		private final TypeInfo type;
		private final String name;

		private Member(String name, TypeInfo type) {
			this.name = checkNotEmpty(name);
			this.type = requireNonNull(type);
			// Sanity check against (accidentally) setting incompatible type
			checkArgument("Not an Item type: "+type, Item.class.isAssignableFrom(type.getType()));
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public Item compute() { return item; }

		@Override
		public void assign(Object value) { item = (Item) value; }

		@Override
		public void clear() { item = null; }

		@Override
		public Assignable<Item> duplicate(EvaluationContext context) {
			return context.getMember(name)
					.map(EvaluationUtils::castMember)
					.orElseThrow(() -> EvaluationUtils.forInternalError(
							"Context is missing member store for name: %s", name));
		}
	}

	static final class Payload implements Expression<byte[]>, NamedObject {

		private final String name;
		private final byte[] data;

		private Payload(String name, byte[] data) {
			this.name = checkNotEmpty(name);
			this.data = requireNonNull(data);
		}

		@Override
		public TypeInfo getResultType() { return TypeInfo.BINARY; }

		@Override
		public byte[] compute() { return data; }

		@Override
		public Expression<byte[]> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return this;
		}

		@Override
		public String getName() { return name; }
	}

	//TODO
}
