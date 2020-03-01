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
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.query.api.eval.Environment.EntryType;
import de.ims.icarus2.query.api.eval.Environment.NsEntry;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public class EnvironmentCache {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentCache.class);

	public static @Nullable EnvironmentCache of(Collection<Environment> environments) {
		if(environments.isEmpty()) {
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
		populated.set(true);
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

	private static final Predicate<NsEntry> ALL = e -> true;
	private static final Predicate<NsEntry> GLOBAL_ONLY = e -> !e.getSource().getContext().isPresent();

	private static Predicate<NsEntry> filterByScope(Expression<?> scope) {
		if(scope==null) {
			return GLOBAL_ONLY;
		}

		TypeInfo type = scope.getResultType();
		Class<?> clazz = type.getType();

		return e ->  {
			Class<?> context = e.getSource().getContext().orElse(null);
			return context!=null && context.isAssignableFrom(clazz);
		};
	}

	private static Predicate<NsEntry> filterByType(TypeFilter filter) {
		if(filter==null || filter==TypeFilter.ALL) {
			return ALL;
		}

		return e -> filter.test(e.getValueType());
	}

	private static final Comparator<NsEntry> PRIORITY_ORDER = (e1, e2) ->
		e1.getSource().getPriority().compareTo(e2.getSource().getPriority());

	private List<NsEntry> fieldCandidates(Expression<?> scope, String name, TypeFilter filter) {
		return entriesByName.getOrDefault(name, Collections.emptySet()).stream()
				.filter(filterByScope(scope))
				.filter(filterByType(filter))
				.sorted(PRIORITY_ORDER)
				.collect(Collectors.toList());
	}

	public Expression<?> resolve(@Nullable Expression<?> scope, String name,
			@Nullable TypeFilter filter) {
		requireNonNull(name);
		filter = filterOrAll(filter);

		ensureLookup();
		List<NsEntry> candidates = fieldCandidates(scope, name, filter);

		if(candidates.isEmpty()) {
			return null;
		}

		if(candidates.size()>1) {
			log.debug("Ambiguous name '{}' leading to entries: {}", name,
					CollectionUtils.toString(candidates));
		}

		return candidates.get(0).instantiate(scope);
	}

	/**
	 *
	 */
	private static Predicate<NsEntry> filterByArguments(Expression<?>[] arguments) {

		final TypeInfo[] argumentTypes = Stream.of(arguments)
				.map(Expression::getResultType)
				.toArray(TypeInfo[]::new);

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

	private List<NsEntry> methodCandidates(Expression<?> scope, String name,
			TypeFilter resultFilter, Expression<?>[] arguments) {
		return entriesByName.getOrDefault(name, Collections.emptySet()).stream()
				.filter(filterByScope(scope))
				.filter(filterByType(resultFilter))
				.filter(filterByArguments(arguments))
				.sorted(PRIORITY_ORDER)
				.collect(Collectors.toList());
	}

	public Expression<?> resolve(@Nullable Expression<?> scope, String name,
			@Nullable TypeFilter resultFilter, Expression<?>[] arguments) {
		requireNonNull(name);
		requireNonNull(arguments);
		resultFilter = filterOrAll(resultFilter);

		ensureLookup();
		List<NsEntry> candidates = methodCandidates(scope, name, resultFilter, arguments);

		if(candidates.isEmpty()) {
			return null;
		}

		if(candidates.size()>1) {
			log.debug("Ambiguous method name '{}' leading to entries: {}", name,
					CollectionUtils.toString(candidates));
		}

		return candidates.get(0).instantiate(scope, arguments);
	}
}
