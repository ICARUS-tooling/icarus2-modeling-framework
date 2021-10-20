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
package de.ims.icarus2.query.api.engine;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.query.api.engine.matcher.StructurePattern;
import de.ims.icarus2.query.api.exp.EvaluationContext.LaneContext;
import de.ims.icarus2.query.api.iql.IqlLane;

/**
 * Encapsulates all the contextual information for matching in a single
 * lane.
 *
 * @author Markus Gärtner
 *
 */
public final class LaneSetup {
	private final int index;
	private final IqlLane lane;
	private final LaneContext laneContext;
	private final StructurePattern pattern;
	private final boolean isFirst, isLast;

	LaneSetup(int index, IqlLane lane, LaneContext laneContext, StructurePattern pattern,
			boolean isFirst, boolean isLast) {
		this.index = index;
		this.lane = requireNonNull(lane);
		this.laneContext = requireNonNull(laneContext);
		this.pattern = requireNonNull(pattern);
		this.isFirst = isFirst;
		this.isLast = isLast;
	}

	public int getIndex() {
		return index;
	}

	public IqlLane getLane() {
		return lane;
	}

	public LaneContext getLaneContext() {
		return laneContext;
	}

	public StructurePattern getPattern() {
		return pattern;
	}

	public boolean isFirst() {
		return isFirst;
	}

	public boolean isLast() {
		return isLast;
	}
}