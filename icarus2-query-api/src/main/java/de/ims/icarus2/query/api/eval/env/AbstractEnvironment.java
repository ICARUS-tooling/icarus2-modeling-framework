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
package de.ims.icarus2.query.api.eval.env;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.query.api.eval.Environment;
import de.ims.icarus2.query.api.eval.Expression;
import de.ims.icarus2.query.api.eval.TypeInfo;
import de.ims.icarus2.util.AbstractBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * Provides a base implementation of {@link Environment} for convenience.
 * Subclasses only need to override the {@link #createEntries()} method and
 * use the {@link EntryBuilder} obtainable via {@link #entryBuilder()} to
 * populate the internal entry storage. Note that this implementation will
 * <b>not</b> validate the overall collection of entries
 *
 * @author Markus Gärtner
 *
 */
public abstract class AbstractEnvironment implements Environment {

	//TODO actually make subclasses that model our default environments!

	private final Set<Entry> entries = new ObjectOpenHashSet<>();
	private final Set<NsEntry> entryView = Collections.unmodifiableSet(entries);
	private final Environment parent;
	private final Class<?> context;

	protected AbstractEnvironment(Environment parent, Class<?> context) {
		// No null checks here, subclasses must know their stuff
		this.parent = parent;
		this.context = context;
	}

	@Override
	public Optional<Environment> getParent() { return Optional.ofNullable(parent); }

	@Override
	public Optional<Class<?>> getContext() { return Optional.ofNullable(context); }

	@Override
	public Set<NsEntry> getEntries() {
		if(entries.isEmpty()) {
			synchronized (this) {
				if(entries.isEmpty()) {
					createEntries();
					if(entries.isEmpty())
						throw new IcarusRuntimeException(GlobalErrorCode.DELEGATION_FAILED,
								"Subclass failed to register any entries: "+getClass());
				}
			}
		}
		return entryView;
	}

	protected abstract void createEntries();

	protected EntryBuilder entryBuilder() {
		return new EntryBuilder(this);
	}

	private void add(Entry entry) {
		entries.add(entry);
	}

	/**
	 * Allows delegation of the actual expression construction to a lambda expression.
	 * @author Markus Gärtner
	 *
	 */
	@FunctionalInterface
	protected interface Instantiator {
		Expression<?> instantiate(NsEntry entry, @Nullable Expression<?> target, Expression<?>...arguments);
	}

	public static class Entry implements NsEntry {

		private final Environment source;

		private final EntryType entryType;
		private final TypeInfo valueType;
		private final String name;

		private final Set<String> aliases;
		private final TypeInfo[] argumentTypes;

		private final Instantiator instantiator;

		private Entry(EntryBuilder builder) {
			source = builder.getSource();
			entryType = builder.getEntryType();
			name = builder.getName();
			Set<String> al = builder.getAliases();
			aliases = al==null ? Collections.emptySet() : Collections.unmodifiableSet(al);
			valueType = builder.getValueType();
			argumentTypes = builder.getArgumentTypes();
			instantiator = builder.getInstantiator();
		}

		@Override
		public EntryType getEntryType() { return entryType; }

		@Override
		public TypeInfo getValueType() { return valueType; }

		@Override
		public String getName() { return name; }

		@Override
		public Set<String> getAliases() { return aliases; }

		@Override
		public int argumentCount() { return argumentTypes.length; }

		@Override
		public TypeInfo argumenTypeAt(int index) { return argumentTypes[index]; }

		@Override
		public Environment getSource() { return source; }

		@Override
		public synchronized Expression<?> instantiate(Expression<?> target, Expression<?>... arguments) {
			return instantiator.instantiate(this, target, arguments);
		}

	}

	public static class EntryBuilder extends AbstractBuilder<EntryBuilder, Entry> {

		private static final TypeInfo[] NO_ARGS = {};

		private final AbstractEnvironment source;

		private EntryType entryType;
		private TypeInfo valueType;
		private String name;

		private Set<String> aliases;
		private TypeInfo[] argumentTypes;

		private Instantiator instantiator;

		private EntryBuilder(AbstractEnvironment source) { this.source = requireNonNull(source); }

		public EntryBuilder entryType(EntryType entryType) {
			requireNonNull(entryType);
			checkState("Entry type already set", this.entryType==null);
			this.entryType = entryType;
			return this;
		}
		public EntryType getEntryType() { return entryType; }

		public EntryBuilder name(String name) {
			requireNonNull(name);
			checkState("Name already set", this.name==null);
			this.name = name;
			return this;
		}
		public String getName() { return name; }

		public EntryBuilder valueType(TypeInfo valueType) {
			requireNonNull(valueType);
			checkState("Value type already set", this.valueType==null);
			this.valueType = valueType;
			return this;
		}
		public TypeInfo getValueType() { return valueType; }

		public EntryBuilder argumentTypes(TypeInfo...argumentTypes) {
			requireNonNull(argumentTypes);
			checkArgument("Argument types must not be empty", argumentTypes.length>0);
			checkState("Argument types already set", this.argumentTypes==null);
			this.argumentTypes = argumentTypes;
			return this;
		}
		public EntryBuilder noArgs() {
			checkState("Argument types already set", argumentTypes==null);
			argumentTypes = NO_ARGS;
			return this;
		}
		public TypeInfo[] getArgumentTypes() {
			return argumentTypes==null ? NO_ARGS : argumentTypes;
		}

		public EntryBuilder aliases(String...aliases) {
			requireNonNull(aliases);
			checkArgument("Aliases must be empty", aliases.length>0);
			checkState("Aliases already set", this.aliases==null);
			this.aliases = set(aliases);
			return this;
		}
		public Set<String> getAliases() { return aliases; }

		public EntryBuilder instantiator(Instantiator instantiator) {
			requireNonNull(instantiator);
			checkState("Instantiator already set", this.instantiator==null);
			this.instantiator = instantiator;
			return this;
		}
		public Instantiator getInstantiator() { return instantiator; }

		public Environment getSource() { return source; }

		// Shorthand methods

		public EntryBuilder method(String name, TypeInfo resultType, TypeInfo...argumentTypes) {
			name(name).entryType(EntryType.METHOD).valueType(resultType);
			if(argumentTypes.length>0) {
				argumentTypes(argumentTypes);
			}
			return this;
		}

		public EntryBuilder field(String name, TypeInfo type) {
			return name(name).entryType(EntryType.FIELD).valueType(type);
		}

		@Override
		protected void validate() {
			checkState("Entry type not set", entryType!=null);
			checkState("Value type not set", valueType!=null);
			checkState("Name not set", name!=null);
			checkState("Instantiator not set", instantiator!=null);

			if(entryType==EntryType.METHOD) {
				checkState("Argument types not set", argumentTypes!=null);
			}
		}

		@Override
		protected Entry create() { return new Entry(this); }

		private void reset() {
			entryType = null;
			valueType = null;
			name = null;
			aliases = null;
			argumentTypes = null;
			instantiator = null;
		}

		public Entry commitAndReset() {
			Entry entry = build();
			source.add(entry);
			reset();
			return entry;
		}
	}
}
