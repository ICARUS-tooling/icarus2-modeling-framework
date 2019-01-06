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
import java.util.function.Consumer;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;

/**
 * TODO mention that when starting at -1 for initial pos, the virtual root node will be returned first!
 *
 * @author Markus Gärtner
 *
 */
public class StructureNodeSpliterator implements Spliterator<Item> {

	private final Structure source;

	/**
	 * Maximum index (exclusive)
	 */
	private final long fence;

	/**
	 * Current position in the structure
	 */
	private long pos;

	public StructureNodeSpliterator(Structure source, long pos, long fence) {
		requireNonNull(source);
		checkArgument("pos must be positive or 0",pos>=-1L);
		checkArgument("fence must be positive",fence>0);
		checkArgument("fence must be greater than pos",fence>pos);
		checkArgument("fence cannot exceed structrue size",fence<=source.getItemCount());

		this.source = source;
		this.pos = pos;
		this.fence = fence;
	}

	public StructureNodeSpliterator(Structure source) {
		requireNonNull(source);

		this.source = source;
		this.pos = -1L;
		this.fence = source.getItemCount();
	}

	/**
	 * @see java.util.Spliterator#tryAdvance(java.util.function.Consumer)
	 */
	@Override
	public boolean tryAdvance(Consumer<? super Item> action) {
		if(pos<fence) {
			action.accept(item());
			pos++;
			return true;
		} else {
			return false;
		}
	}

	public Item item() {
		return pos==-1L ? source.getVirtualRoot() : source.getItemAt(pos);
	}

	/**
	 * @see java.util.Spliterator#trySplit()
	 */
	@Override
	public Spliterator<Item> trySplit() {
		long lo = pos; // divide range in half
		long mid = ((lo + fence) >>> 1) & ~1; // force midpoint to be even
		if (lo < mid) { // split out left half
			pos = mid; // reset this Spliterator's origin
			return new StructureNodeSpliterator(source, lo, mid);
		} else {
			// too small to split
			return null;
		}
	}

	/**
	 * @see java.util.Spliterator#estimateSize()
	 */
	@Override
	public long estimateSize() {
		return fence-pos;
	}

	/**
	 * @see java.util.Spliterator#characteristics()
	 */
	@Override
	public int characteristics() {
		return ORDERED | SIZED | IMMUTABLE | SUBSIZED;
	}

	@Override
	public void forEachRemaining(Consumer<? super Item> action) {
		for(;pos<fence;pos++) {
			action.accept(item());
		}
	}

}
