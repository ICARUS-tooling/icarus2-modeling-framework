/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 */
package de.ims.icarus2.model.util.stream;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.Spliterator;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.structure.Structure;

/**
 * @author Markus Gärtner
 *
 */
public class StructureEdgeSpliterator implements Spliterator<Edge> {

	private final Structure source;

	/**
	 * Maximum index (exclusive)
	 */
	private final long fence;

	/**
	 * Current position in the structure
	 */
	private long pos;

	public StructureEdgeSpliterator(Structure source, long pos, long fence) {
		checkNotNull(source);
		checkArgument(pos>=0L);
		checkArgument(fence>pos);
		checkArgument(fence<=source.getItemCount());

		this.source = source;
		this.pos = pos;
		this.fence = fence;
	}

	public StructureEdgeSpliterator(Structure source) {
		checkNotNull(source);

		this.source = source;
		this.pos = 0L;
		this.fence = source.getEdgeCount();
	}

	/**
	 * @see java.util.Spliterator#tryAdvance(java.util.function.Consumer)
	 */
	@Override
	public boolean tryAdvance(Consumer<? super Edge> action) {
		if(pos<fence) {
			action.accept(edge());
			pos++;
			return true;
		} else {
			return false;
		}
	}

	public Edge edge() {
		return source.getEdgeAt(pos);
	}

	/**
	 * @see java.util.Spliterator#trySplit()
	 */
	@Override
	public Spliterator<Edge> trySplit() {
		long lo = pos; // divide range in half
		long mid = ((lo + fence) >>> 1) & ~1; // force midpoint to be even
		if (lo < mid) { // split out left half
			pos = mid; // reset this Spliterator's origin
			return new StructureEdgeSpliterator(source, lo, mid);
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
	public void forEachRemaining(Consumer<? super Edge> action) {
		for(;pos<fence;pos++) {
			action.accept(edge());
		}
	}

}
