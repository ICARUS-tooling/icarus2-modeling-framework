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
package de.ims.icarus2.query.api.engine;

import static java.util.Objects.requireNonNull;

import java.util.List;

import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.query.api.engine.result.MatchImpl;
import de.ims.icarus2.util.collections.ArrayUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
public class MatchMaker {

	private final long index;
	private final int lane;
	private final List<Match> matches = new ObjectArrayList<>();

	public MatchMaker(int lane, long index) {
		this.lane = lane;
		this.index = index;
	}

	public MatchMaker match(Match match) {
		matches.add(requireNonNull(match));
		return this;
	}

	/** Create and add Match with mapping for implicit mappingIds to given indices */
	public MatchMaker match(int...indices) {
		int[] mappingIds = new int[indices.length];
		ArrayUtils.fillAscending(mappingIds);
		return match(MatchImpl.of(lane, index, mappingIds, indices));
	}

	public Match[] make() {
		return matches.toArray(new Match[matches.size()]);
	}
}
