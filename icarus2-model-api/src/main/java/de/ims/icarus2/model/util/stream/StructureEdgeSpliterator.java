/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.util.stream;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Spliterator;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.stream.AbstractFencedSpliterator;

/**
 * @author Markus Gärtner
 *
 */
public class StructureEdgeSpliterator extends AbstractFencedSpliterator<Edge> {

	public static Spliterator<Edge> spliterator(Structure source) {
		requireNonNull(source);

		return new StructureEdgeSpliterator(source, 0, IcarusUtils.UNSET_LONG);
	}

	public static Spliterator<Edge> spliterator(Structure source, long pos, long fence) {
		requireNonNull(source);
		checkArgument(pos>=0L);
		checkArgument(fence>pos);
		checkArgument(fence<=source.getEdgeCount());

		return new StructureEdgeSpliterator(source, pos, fence);
	}

	public static Spliterator<Edge> spliterator(Structure source, Item node, boolean isSource) {
		requireNonNull(source);
		requireNonNull(node);

		return new ForItem(source, node, isSource, 0, IcarusUtils.UNSET_LONG);
	}

	public static Spliterator<Edge> spliterator(Structure source, Item node, boolean isSource,
			long pos, long fence) {
		requireNonNull(source);
		requireNonNull(node);
		checkArgument(fence>pos);
		checkArgument(fence<=source.getEdgeCount(node, isSource));

		return new ForItem(source, node, isSource, pos, fence);
	}

	final Structure source;

	protected StructureEdgeSpliterator(Structure source, long pos, long fence) {
		super(pos, fence);
		this.source = source;
	}

	@Override
	protected void updateFence() {
		if(fence==UNDEFINED_FENCE) {
			fence = source.getEdgeCount();
		}
	}

	@Override
	protected Edge current() {
		return source.getEdgeAt(pos);
	}

	@Override
	protected StructureEdgeSpliterator split(long pos, long fence) {
		return new StructureEdgeSpliterator(source, pos, fence);
	}

	static class ForItem extends StructureEdgeSpliterator {

		private final Item node;
		private final boolean isSource;

		/**
		 * @param source
		 * @param pos
		 * @param fence
		 */
		ForItem(Structure source, Item node, boolean isSource, long pos, long fence) {
			super(source, pos, fence);

			this.node = node;
			this.isSource = isSource;
		}

		/**
		 * @see de.ims.icarus2.model.util.stream.StructureEdgeSpliterator#updateFence()
		 */
		@Override
		protected void updateFence() {
			if(fence==UNDEFINED_FENCE) {
				fence = source.getEdgeCount(node, isSource);
			}
		}

		/**
		 * @see de.ims.icarus2.model.util.stream.StructureEdgeSpliterator#split(long, long)
		 */
		@Override
		protected StructureEdgeSpliterator split(long pos, long fence) {
			return new ForItem(source, node, isSource, pos, fence);
		}

		/**
		 * @see de.ims.icarus2.model.util.stream.StructureEdgeSpliterator#edge()
		 */
		@Override
		protected Edge current() {
			return source.getEdgeAt(node, pos, isSource);
		}
	}
}
