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
package de.ims.icarus2.query.api.exp.env;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.query.api.exp.Environment;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * Provides a base implementation of {@link Environment} for convenience.
 * Subclasses only need to override the {@link #createEntries()} method and
 * use the {@link EntryBuilder} obtainable via {@link #entryBuilder()} to
 * populate the internal entry storage. Note that this implementation will
 * <b>not</b> validate the overall collection of entries.
 *
 * @author Markus Gärtner
 *
 */
public abstract class AbstractEnvironment implements Environment {

	//TODO actually make subclasses that model our default environments!

	private final Set<NsEntry> entries = new ObjectOpenHashSet<>();
	private final Set<NsEntry> entryView = Collections.unmodifiableSet(entries);
	private final Environment parent;
	private final Class<?> context;

	protected AbstractEnvironment(@Nullable Environment parent, @Nullable Class<?> context) {
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

	/**
	 * Provides an {@link EntryBuilder} that is linked to this environment and
	 * commits new entries into our internal storage.
	 */
	protected EntryBuilder entryBuilder() {
		return new EntryBuilder(this, entries::add);
	}
}
