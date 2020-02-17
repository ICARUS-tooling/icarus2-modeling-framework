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
package de.ims.icarus2.query.api.eval;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.QuerySwitch;
import de.ims.icarus2.query.api.eval.Environment.NsEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public class EvaluationContext {

	/** Maps the usable raw names or aliases to corpus entries. */
	private final Map<String, NsEntry> corpora = new Object2ObjectOpenHashMap<>();

	/** Maps the usable raw names or aliases to layer entries. */
	private final Map<String, NsEntry> layers = new Object2ObjectOpenHashMap<>();

	/** Flags that have been switched on for the query. */
	private final Set<String> switches = new ObjectOpenHashSet<>();

	/** Additional properties that have been set for this query. */
	private final Map<String, String> properties = new Object2ObjectOpenHashMap<>();

	//TODO add mechanisms to obtain root namespace and to navigate namespace hierarchies

	//TODO add mechanism to register callbacks for stages of matching process?

	public boolean isSwitchSet(String name) {
		return switches.contains(checkNotEmpty(name));
	}

	public boolean isSwitchSet(QuerySwitch qs) {
		return switches.contains(qs.getKey());
	}

	public Environment getRootEnvironment() {

	}

	public Set<Environment> getActiveEnvironments() {

	}

	public Environment enter(Class<?> context) {

	}

	public void exit(Class<?> context) {

	}

	/**
	 * Tries to resolve the given {@code name} to a field or no-args method
	 * equivalent. Using the {@code resultFilter} argument, returned expressions
	 * can be restricted to be return type compatible to a desired target type.
	 */
	public Expression<?> resolve(String name, @Nullable TypeFilter filter) {

	}

	/**
	 * Tries to resolve the given {@code name} to a method that takes the
	 * specified {@code arguments} as input.
	 * If the {@code resultFilter} argument is provided, it will be used to
	 * restrict the pool of methods to be considered to those that return
	 * a compatible value.
	 */
	public Expression<?> resolve(String name, @Nullable TypeFilter resultFilter,
			Expression<?>[] arguments) {

	}

	/**
	 * Returns a lookup function for the specified annotation key. Note that the
	 * {@code key} has to be unique
	 * @param key
	 * @return
	 */
	public <T> Function<Item, T> annotationSource(String key) {

	}
}
