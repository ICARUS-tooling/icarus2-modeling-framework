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
package de.ims.icarus2.model.standard.highlight;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.function.IntFunction;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.highlight.Highlight;
import de.ims.icarus2.model.api.highlight.HighlightCursor;
import de.ims.icarus2.model.api.layer.HighlightLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(HighlightCursor.class)
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
		requireNonNull(highlightGenerator);

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
				}
				cache = (Object[]) highlightCache;

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
		}

		return getSequenceAt(index);
	}

}
