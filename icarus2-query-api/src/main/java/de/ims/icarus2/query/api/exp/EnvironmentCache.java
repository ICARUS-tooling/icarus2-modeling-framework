/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import de.ims.icarus2.query.api.exp.Environment.EntryType;
import de.ims.icarus2.query.api.exp.Environment.NsEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public class EnvironmentCache {

	/**
	 * If given list of environments is {@code null} or empty, returns {@code null},
	 * otherwise returns  new instance of {@link EnvironmentCache} with {@code environments}
	 * as argument.
	 *
	 * @param environments
	 * @return
	 */
	public static @Nullable EnvironmentCache of(@Nullable Collection<Environment> environments) {
		if(environments==null || environments.isEmpty()) {
			return null;
		}
		return new EnvironmentCache(environments);
	}

	private final List<Environment> environments;

	private final Map<String, Set<NsEntry>> entriesByName = new Object2ObjectOpenHashMap<>();
	private AtomicBoolean populated = new AtomicBoolean(false);

	public EnvironmentCache(Collection<Environment> environments) {
		requireNonNull(environments);
		checkArgument("Environments list must not be empty", !environments.isEmpty());
		this.environments = new ArrayList<>(environments);
	}

	public void dispose() {
		populated.set(true); // this will prevent future (re)loading
		environments.clear();
		entriesByName.clear();
	}

	private static TypeFilter filterOrAll(TypeFilter filter) {
		return filter==null ? TypeFilter.ALL : filter;
	}

	private void ensureLookup() {
		if(populated.compareAndSet(false, true)) {
			for(Environment environment : environments) {
				// Local entries first, then continue with parent(s)
				while(environment!=null) {
					for(NsEntry entry : environment.getEntries()) {
						mapEntry(entry.getName(), entry);
						for(String alias : entry.getAliases()) {
							mapEntry(alias, entry);
						}
					}
					environment = environment.getParent().orElse(null);
				}
			}
		}
	}

	private void mapEntry(String name, NsEntry entry) {
		entriesByName.computeIfAbsent(name, k -> new ReferenceLinkedOpenHashSet<>()).add(entry);
	}

	private static Predicate<NsEntry> filterFieldCandidates() {
		return e -> e.getEntryType()==EntryType.FIELD || e.argumentCount()==0;
	}

	private static final Predicate<NsEntry> ALL = e -> true;
	private static final Predicate<NsEntry> GLOBAL_ONLY = e -> !e.getSource().getContext().isPresent();

	private static Predicate<NsEntry> filterByScope(TypeInfo scope) {
		if(scope==null) {
			return GLOBAL_ONLY;
		}

		Class<?> clazz = scope.getType();

		return e ->  {
			Class<?> context = e.getSource().getContext().orElse(null);
			return context!=null && context.isAssignableFrom(clazz);
		};
	}

	private static Predicate<NsEntry> filterByType(TypeFilter filter) {
		if(filter==TypeFilter.ALL) {
			return ALL;
		}

		return e -> filter.test(e.getValueType());
	}

	private static final Comparator<NsEntry> PRIORITY_ORDER = (e1, e2) ->
		e1.getSource().getPriority().compareTo(e2.getSource().getPriority());

	private List<NsEntry> fieldCandidates(TypeInfo scope, String name, TypeFilter filter) {
		return entriesByName.getOrDefault(name, Collections.emptySet()).stream()
				.filter(filterFieldCandidates())
				.filter(filterByScope(scope))
				.filter(filterByType(filter))
				.sorted(PRIORITY_ORDER)
				.collect(Collectors.toList());
	}

	/**
	 *
	 */
	private static Predicate<NsEntry> filterByArguments(TypeInfo[] argumentTypes) {
		return e -> {
			// Shortcut for zero-argument methods that 'collide' with fields
			if(argumentTypes.length==0 && (e.argumentCount()==0 || e.getEntryType()==EntryType.FIELD)) {
				return true;
			}

			// Discard non-methods and argument mismatches
			if(e.getEntryType()!=EntryType.METHOD || e.argumentCount()!=argumentTypes.length) {
				return false;
			}

			for (int i = 0; i < argumentTypes.length; i++) {
				if(!e.argumenTypeAt(i).isCompatible(argumentTypes[i])) {
					return false;
				}
			}

			return true;
		};
	}

	private List<NsEntry> methodCandidates(TypeInfo scope, String name,
			TypeFilter resultFilter, TypeInfo[] arguments) {
		return entriesByName.getOrDefault(name, Collections.emptySet()).stream()
				.filter(filterByScope(scope))
				.filter(filterByType(resultFilter))
				.filter(filterByArguments(arguments))
				.sorted(PRIORITY_ORDER)
				.collect(Collectors.toList());
	}

	public List<NsEntry> resolve(@Nullable TypeInfo scope, String name,
			@Nullable TypeFilter resultFilter, TypeInfo...argumentTypes) {
		requireNonNull(name);
		requireNonNull(argumentTypes);
		resultFilter = filterOrAll(resultFilter);

		ensureLookup();
		List<NsEntry> candidates = null;

		if(argumentTypes.length==0) {
			candidates = fieldCandidates(scope, name, resultFilter);
		}

		if(candidates==null || candidates.isEmpty()) {
			candidates = methodCandidates(scope, name, resultFilter, argumentTypes);
		}

		return candidates;
	}

	//TODO add methods for fuzzy candidate search. will be needed for auto-complete/recommenders.
}
