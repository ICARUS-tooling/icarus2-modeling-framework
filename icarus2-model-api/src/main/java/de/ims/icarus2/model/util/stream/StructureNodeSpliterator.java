/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.stream.AbstractFencedSpliterator;

/**
 * TODO mention that when starting at -1 for initial pos, the virtual root node will be returned first!
 *
 * @author Markus Gärtner
 *
 */
public class StructureNodeSpliterator extends AbstractFencedSpliterator<Item> {

	private static final long ROOT_POS = -1L;

	public static StructureNodeSpliterator spliterator(Structure source, long pos, long fence) {
		requireNonNull(source);
		checkArgument(pos>=-1L);
		checkArgument(fence>pos && fence<=source.getItemCount());

		return new StructureNodeSpliterator(source, pos, fence);
	}

	public static StructureNodeSpliterator spliterator(Structure source) {
		requireNonNull(source);

		return new StructureNodeSpliterator(source, ROOT_POS, UNDEFINED_FENCE);
	}

	private final Structure source;

	public StructureNodeSpliterator(Structure source, long pos, long fence) {
		super(pos, fence);

		this.source = source;
	}

	@Override
	protected Item current() {
		return pos==-1L ? source.getVirtualRoot() : source.getItemAt(pos);
	}

	/**
	 * @see de.ims.icarus2.util.stream.AbstractFencedSpliterator#split(long, long)
	 */
	@Override
	protected Spliterator<Item> split(long pos, long fence) {
		return new StructureNodeSpliterator(source, pos, fence);
	}

	/**
	 * @see de.ims.icarus2.util.stream.AbstractFencedSpliterator#updateFence()
	 */
	@Override
	protected void updateFence() {
		if(fence==IcarusUtils.UNSET_LONG) {
			fence = source.getItemCount();
		}
	}
}
