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
package de.ims.icarus2.model.api.driver.indices.func;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;

/**
 * Basic template for a utility class that processes multiple {@link IndexSet}
 * instances and produces a result {@link IndexSet}.
 * The basic protocol for this is that client code provides index sets via the
 * various {@link #add(Collection)} methods and then calls the implementation
 * specific processing method (e.g. {@link IterativeIntersection#intersectAll()}.
 *
 * @author Markus Gärtner
 *
 */
public abstract class AbstractIndexSetProcessor<P extends AbstractIndexSetProcessor<P>> {

	protected final List<IndexSet> buffer = new ArrayList<>();

	protected long estimatedResultSize;

	private final boolean requiresSortedInput;

	/**
	 * @param requiresSortedInput
	 */
	public AbstractIndexSetProcessor(boolean requiresSortedInput) {
		this.requiresSortedInput = requiresSortedInput;
	}

	@SuppressWarnings("unchecked")
	protected P self() {
		return (P) this;
	}

	/**
	 * Called whenever a new {@link IndexSet} is added to this processor so that
	 * subclasses can adjust the prediction about required buffer sizes for output
	 * data.
	 *
	 * @param indexSet
	 */
	protected abstract void refreshEstimatedResultSize(IndexSet indexSet);

	/**
	 * Signals whether or not the input for this processor has to be sorted.
	 * Used in all the {@code add} methods to determine if a check is
	 * required.
	 * <p>
	 * The default implementation returns {@code true}
	 *
	 * @return
	 */
	public final boolean isRequiresSortedInput() {
		return requiresSortedInput;
	}

	protected void checkNewIndexSet(IndexSet set) {
		if(set.size()==IndexSet.UNKNOWN_SIZE)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Unable to process index set of unknown size");

		if(isRequiresSortedInput()) {
			IndexUtils.checkSorted(set);
		}
	}

	public P add(IndexSet...indices) {
		for(IndexSet set : indices) {
			checkNewIndexSet(set);
			buffer.add(set);
			refreshEstimatedResultSize(set);
		}
		return self();
	}

	public P add(Collection<? extends IndexSet> indices) {
		for(IndexSet set : indices) {
			checkNewIndexSet(set);
			buffer.add(set);
			refreshEstimatedResultSize(set);
		}
		return self();
	}

	public long getEstimatedResultSize() {
		return estimatedResultSize;
	}

}
