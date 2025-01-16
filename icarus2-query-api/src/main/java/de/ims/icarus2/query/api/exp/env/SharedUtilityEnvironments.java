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
package de.ims.icarus2.query.api.exp.env;

import static de.ims.icarus2.query.api.exp.EvaluationUtils.argAt;
import static de.ims.icarus2.query.api.exp.Expressions.wrapBool;

import de.ims.icarus2.query.api.exp.CodePointUtils;
import de.ims.icarus2.query.api.exp.Environment;
import de.ims.icarus2.query.api.exp.Expression;
import de.ims.icarus2.query.api.exp.Expressions;
import de.ims.icarus2.query.api.exp.TypeInfo;

/**
 * @author Markus Gärtner
 *
 */
public class SharedUtilityEnvironments {

	public static Environment[] all() {
		return new Environment[] {
				forObject(),
				forComparable(),
				forText(),
				//TODO
		};
	}

	public static Environment forObject() {
		return ObjectEnvironment.getInstance();
	}

	private static final class ObjectEnvironment extends AbstractEnvironment {

		private volatile static SharedUtilityEnvironments.ObjectEnvironment instance;

		public static SharedUtilityEnvironments.ObjectEnvironment getInstance() {
			SharedUtilityEnvironments.ObjectEnvironment result = instance;

			if (result == null) {
				synchronized (SharedUtilityEnvironments.ObjectEnvironment.class) {
					result = instance;

					if (result == null) {
						instance = new SharedUtilityEnvironments.ObjectEnvironment();
						result = instance;
					}
				}
			}

			return result;
		}

		private ObjectEnvironment() {
			super(null, Object.class);
		}

		@Override
		protected void createEntries() {
			EntryBuilder builder = entryBuilder();

			builder.method("toString", TypeInfo.TEXT)
				.aliases("string") //TODO rethink this, as we might run into collisions
				.instantiator((e, ctx, t, args) -> Expressions.<Object,String>wrapObj(e, Object::toString, t, args))
				.commitAndReset();

			builder.method("equals", TypeInfo.BOOLEAN, TypeInfo.GENERIC)
				.instantiator((e, ctx, t, args) -> {
					Expression<?> other = argAt(args, 0);
					return wrapBool(e, obj -> obj.equals(other.compute()), t, args);
				})
				.commitAndReset();
		}
	}

	public static Environment forComparable() {
		return ComparableEnvironment.getInstance();
	}

	private static final class ComparableEnvironment extends AbstractEnvironment {

		private volatile static SharedUtilityEnvironments.ComparableEnvironment instance;

		public static SharedUtilityEnvironments.ComparableEnvironment getInstance() {
			SharedUtilityEnvironments.ComparableEnvironment result = instance;

			if (result == null) {
				synchronized (SharedUtilityEnvironments.ComparableEnvironment.class) {
					result = instance;

					if (result == null) {
						instance = new SharedUtilityEnvironments.ComparableEnvironment();
						result = instance;
					}
				}
			}

			return result;
		}

		private ComparableEnvironment() {
			super(forObject(), Comparable.class);
		}

		private static final TypeInfo type = TypeInfo.of(Comparable.class);

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected void createEntries() {
			entryBuilder().method("compareTo", TypeInfo.INTEGER, type)
				.aliases("compare")
				.instantiator((e, ctx, t, args) -> {
					Expression<?> other = argAt(args, 0);
					return Expressions.<Comparable>wrapInt(e, comp -> comp.compareTo(other.compute()), t, args);
				})
				.commitAndReset();
		}
	}

	public static Environment forText() {
		return TextEnvironment.getInstance();
	}

	private static final class TextEnvironment extends AbstractEnvironment {

		private volatile static SharedUtilityEnvironments.TextEnvironment instance;

		public static SharedUtilityEnvironments.TextEnvironment getInstance() {
			SharedUtilityEnvironments.TextEnvironment result = instance;

			if (result == null) {
				synchronized (SharedUtilityEnvironments.TextEnvironment.class) {
					result = instance;

					if (result == null) {
						instance = new SharedUtilityEnvironments.TextEnvironment();
						result = instance;
					}
				}
			}

			return result;
		}

		private TextEnvironment() {
			super(forObject(), CharSequence.class);
		}

		@Override
		protected void createEntries() {
			EntryBuilder builder = entryBuilder();

			builder.method("length", TypeInfo.INTEGER)
				.aliases("len", "symbols")
				.instantiator((e, ctx, t, args) -> Expressions.<CharSequence>wrapInt(e, CodePointUtils::codePointCount, t, args))
				.commitAndReset();

			builder.method("charAt", TypeInfo.INTEGER, TypeInfo.INTEGER)
				.aliases("char", "symbol")
				.instantiator((e, ctx, t, args) -> {
					Expression<?> index = argAt(args, 0);
					return Expressions.<CharSequence>wrapInt(e, seq -> CodePointUtils.codePointAt(seq, index.computeAsInt()), t, args);
				})
				.commitAndReset();
		}
	}
}
