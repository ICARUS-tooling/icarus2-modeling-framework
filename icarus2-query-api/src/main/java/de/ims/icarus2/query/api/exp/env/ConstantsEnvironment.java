/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import de.ims.icarus2.query.api.exp.Environment;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.exp.Expression;
import de.ims.icarus2.query.api.exp.TypeInfo;

/**
 * Implements an {@link Environment} that only holds constant values wrapped into
 * {@link Expression} instances.
 *
 * @author Markus Gärtner
 *
 */
public class ConstantsEnvironment implements Environment {

	public static ConstantsEnvironment forMapping(Map<String, Object> source) {
		return new ConstantsEnvironment(source);
	}

	private final class StaticEntry implements NsEntry, Expression<Object> {
		private final String name;
		private final Object value;
		private final TypeInfo type;

		StaticEntry(String name, Object value) {
			this.name = requireNonNull(name);
			this.value = requireNonNull(value);
			this.type = TypeInfo.of(value.getClass());
		}

		@Override
		public TypeInfo getValueType() { return type; }

		@Override
		public String getName() { return name; }

		@Override
		public Set<String> getAliases() { return Collections.emptySet(); }

		@Override
		public EntryType getEntryType() { return EntryType.FIELD; }

		@Override
		public int argumentCount() { return 0; }

		@Override
		public TypeInfo argumenTypeAt(int index) { throw new ArrayIndexOutOfBoundsException(); }

		@Override
		public Environment getSource() { return ConstantsEnvironment.this; }
		@Override
		public Expression<?> instantiate(EvaluationContext context,
				@Nullable Expression<?> target,
				@Nullable Expression<?>... arguments) {
			requireNonNull(context);
			checkArgument("Target must be null", target==null);
			checkArgument("No argument supported for field access", arguments==null || arguments.length==0);
			return this;
		}

		@Override
		public TypeInfo getResultType() { return type; }

		@Override
		public Object compute() { return value; }

		@Override
		public Expression<Object> duplicate(EvaluationContext context) {
			requireNonNull(context);
			return this;
		}
	}

	private final Set<NsEntry> entries;

	private ConstantsEnvironment(Map<String, Object> source) {
		requireNonNull(source);
		checkArgument("Source must not be empty", !source.isEmpty());

		Set<NsEntry> entries = source.entrySet().stream()
			.map(entry -> this.new StaticEntry(entry.getKey(), entry.getValue()))
			.collect(Collectors.toSet());

		this.entries = Collections.unmodifiableSet(entries);
	}

	@Override
	public Set<NsEntry> getEntries() { return entries; }

	@Override
	public Optional<Class<?>> getContext() { return null; }
}
