/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine.matcher;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.query.api.exp.Assignable;
import de.ims.icarus2.query.api.exp.Expression;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * Matches a single node in the structural constraints of a query
 * against potential target items. This implementation has an
 * integrated cache function to employ memoization in order to
 * prevent redundant calls to the actual constraint expression.
 *
 * @author Markus Gärtner
 *
 */
public class CachedNodeMatcher extends AbstractMatcher<Item> {

	private static final int DEFAULT_SIZE = 1<<6;

	private static final int BLOCK_MASK = (1<<5)-1;
	private static final int BLOCK_POWER = 5;

	private static final long ENTRY_MASK = (1<<2)-1;

	private static final long EVAL_FLAG = 2;
	private static final long RESULT_FLAG = 1;

	private static final long RESULT_YES = 1;
	private static final long RESULT_NO = 0;

	/** Storage to put new items for evaluation */
	private final Assignable<? extends Item> element;
	/** Constraint to be evaluated */
	private final Expression<?> constraint;

	/**
	 * Memoization cache.
	 *
	 * Each entry is stored as 2 bits with the higher bit signaling if the entry has
	 * already been computed and the lower bit holding the actual result.
	 *
	 * Each "block" in the cache holds 32 entries and with the initial default
	 * size of 64 this leaves room for 2048 entries before the cache needs to be resized.
	 */
	private long[] cache;

	public CachedNodeMatcher(int id, Assignable<? extends Item> element, Expression<?> constraint) {
		super(id);

		requireNonNull(element);
		requireNonNull(constraint);

		checkArgument("Constraint must be of type BOOLEAN", constraint.isBoolean());

		this.constraint = constraint;
		this.element = element;

		cache = new long[DEFAULT_SIZE];
	}

	private long getBlock(int index) {
		int block = index >> BLOCK_POWER;
		if(block >= cache.length) {
			int newSize = CollectionUtils.growSize(cache.length, index);
			cache = Arrays.copyOf(cache, newSize);
		}
		return cache[block];
	}

	private static long getEntry(long block, int index) {
		int slot = (index & BLOCK_MASK) << 1;
		return (block >> slot) & ENTRY_MASK;
	}

	private static boolean isValid(long entry) {
		return (entry & EVAL_FLAG) == EVAL_FLAG;
	}

	private static boolean getResult(long entry) {
		return (entry & RESULT_FLAG) == RESULT_YES;
	}

	private void updateBlock(long block, int index, boolean value) {
		long entry = EVAL_FLAG | (value ? RESULT_YES : RESULT_NO);
		int slot = (index & BLOCK_MASK) << 1;
		block |= (entry<<slot);
		cache[index >> BLOCK_POWER] = block;
	}

	@Override
	public boolean matches(long index, Item target) {
		requireNonNull(target);
		int idx = strictToInt(index);

		long block = getBlock(idx);
		long entry = getEntry(block, idx);

		// If possible use cached result
		if(isValid(entry)) {
			return getResult(entry);
		}

		// Prepare context for current target item (cheap op)
		element.assign(target);

		// Now evaluate constraint (expensive op)
		boolean value = constraint.computeAsBoolean();

		// Remember result
		updateBlock(block, idx, value);

		return value;
	}
	public void resetCache() {
		//TODO maybe we should remember the maximum index we stored so that we don't need to reset all
		Arrays.fill(cache, 0L);
	}

	@VisibleForTesting
	boolean hasEntry(int index) {
		int block = index >> BLOCK_POWER;
		if(block >= cache.length) {
			return false;
		}

		long entry = getEntry(cache[block], index);
		return isValid(entry);
	}

	@VisibleForTesting
	boolean getCachedValue(int index) {
		int block = index >> BLOCK_POWER;
		if(block >= cache.length) {
			return false;
		}

		long entry = getEntry(cache[block], index);
		return isValid(entry) && getResult(entry);
	}

	/** Total number of available cache entries */
	@VisibleForTesting
	int cacheSize() {
		return cache.length * 32;
	}

}
