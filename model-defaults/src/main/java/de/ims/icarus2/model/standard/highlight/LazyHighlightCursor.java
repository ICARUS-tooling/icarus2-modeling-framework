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
 *
 */
package de.ims.icarus2.model.standard.highlight;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.function.IntFunction;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.highlight.Highlight;
import de.ims.icarus2.model.api.layer.HighlightLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public class LazyHighlightCursor extends AbstractHighlightCursor {

	private final int concurrentHighlightCount;
	private final IntFunction<DataSequence<Highlight>> highlightGenerator;

	/**
	 * Models either a singleton or an array of highlight sequences
	 */
	private volatile Object highlightCache;

	public LazyHighlightCursor(HighlightLayer layer, Container target,
			int concurrentHighlightCount, IntFunction<DataSequence<Highlight>> highlightGenerator) {
		super(layer, target);

		checkArgument("Highlight count must be greater than 0", concurrentHighlightCount>0);
		checkNotNull(highlightGenerator);

		this.concurrentHighlightCount = concurrentHighlightCount;
		this.highlightGenerator = highlightGenerator;
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.HighlightCursor#getConcurrentHighlightCount()
	 */
	@Override
	public int getConcurrentHighlightCount() {
		return concurrentHighlightCount;
	}

	private DataSequence<Highlight> loadSequenceForIndex(int index) {
		DataSequence<Highlight> sequence = highlightGenerator.apply(index);

		if(sequence==null)
			throw new ModelException(GlobalErrorCode.DELEGATION_FAILED,
					String.format("Failed to generate highlight sequence for index %d on container %s in layer %s",
							Integer.valueOf(index), getName(getTarget()), getName(getHighlightLayer())));

		return sequence;
	}

	@SuppressWarnings("unchecked")
	private DataSequence<Highlight> getSingletonSequence() {
		if(highlightCache==null) {
			synchronized (highlightGenerator) {
				if(highlightCache==null) {
					highlightCache = loadSequenceForIndex(0);
				}
			}
		}

		return (DataSequence<Highlight>) highlightCache;
	}

	@SuppressWarnings("unchecked")
	private DataSequence<Highlight> getSequenceAt(int index) {
		Object[] cache = (Object[]) highlightCache;

		if(cache==null || cache[index]==null) {
			synchronized (highlightGenerator) {
				if(highlightCache==null) {
					highlightCache = new Object[concurrentHighlightCount];
					cache = (Object[]) highlightCache;
				}

				if(cache[index]==null) { // Takes care of the out of bounds check
					cache[index] = loadSequenceForIndex(index);
				}
			}
		}

		return (DataSequence<Highlight>) cache[index];
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.HighlightCursor#getHighlights(int)
	 */
	@Override
	public DataSequence<Highlight> getHighlights(int index) {
		if(concurrentHighlightCount==1) {
			if(index!=0)
				throw new IndexOutOfBoundsException("Invalid index (only 0 allowed for singleton cache): "+index);

			return getSingletonSequence();
		} else {
			return getSequenceAt(index);
		}
	}

}
