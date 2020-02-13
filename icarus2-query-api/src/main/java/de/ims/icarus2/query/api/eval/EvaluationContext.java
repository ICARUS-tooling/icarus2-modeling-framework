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

	public Environment getActiveEnvironment()
}
