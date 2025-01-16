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
package de.ims.icarus2.query.api.engine.result;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.List;

import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
public class MappingContext {

	public static Builder builder() {
		return new Builder();
	}

	private final IqlNode[][] nodes;
	private final IqlLane[] lanes;

	private MappingContext(Builder builder) {
		nodes = CollectionUtils.toArray(builder.nodes, IqlNode[][]::new);
		lanes = CollectionUtils.toArray(builder.lanes, IqlLane[]::new);
	}

	private void checkSingleLane() {
		if(lanes.length>0)
			throw new UnsupportedOperationException("Must specify a lange index");
	}

	public IqlNode getNode(int index) {
		checkSingleLane();
		return getNode(0, index);
	}

	public IqlNode getNode(int laneIndex, int nodeIndex) {
		return nodes[laneIndex][nodeIndex];
	}

	public IqlNode[] getNodes() {
		checkSingleLane();
		return getNodes(0);
	}

	public IqlNode[] getNodes(int laneIndex) {
		return nodes[laneIndex].clone();
	}

	public int getNodeCount() {
		checkSingleLane();
		return getNodeCount(0);
	}

	public int getNodeCount(int laneIndex) {
		return nodes[laneIndex].length;
	}

	public IqlLane getLane() {
		checkSingleLane();
		return getLane(0);
	}

	public IqlLane getLane(int laneIndex) {
		return lanes[laneIndex];
	}

	public static class Builder extends AbstractBuilder<Builder, MappingContext> {

		private final List<IqlNode[]> nodes = new ObjectArrayList<>();
		private final List<IqlLane> lanes = new ObjectArrayList<>();

		private Builder() {
			// no-op
		}

		public Builder map(IqlLane lane, IqlNode[] nodes) {
			requireNonNull(lane);
			requireNonNull(nodes);
			checkArgument("mapped nodes cannot be empty", nodes.length>0);
			this.lanes.add(lane);
			this.nodes.add(nodes.clone());
			return this;
		}

		@Override
		protected void validate() {
			checkState("no nodes defined", !nodes.isEmpty());
			checkState("no lanes defined", !lanes.isEmpty());
		}

		@Override
		protected MappingContext create() { return new MappingContext(this); }
	}
}
