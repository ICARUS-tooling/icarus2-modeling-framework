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
package de.ims.icarus2.model.standard.driver.cache;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Primitive;
import de.ims.icarus2.util.mem.Reference;

/**
 * @author Markus Gärtner
 *
 */
@Assessable
public class MemberPool<M extends Item> implements Consumer<M>, Supplier<M> {

	@Reference
	private volatile ArrayList<M> pool;

	@Primitive
	private final int poolSize;

	public MemberPool() {
		this(1000);
	}

	public MemberPool(int poolSize) {
		if (poolSize <= 0)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Illegal pool-size (negative or zero): " + poolSize);
		this.poolSize = poolSize;
	}

	private ArrayList<M> ensurePool() {
		if(pool==null) {
			synchronized (this) {
				if(pool==null) {
					pool = new ArrayList<>(poolSize);
				}
			}
		}

		return pool;
	}


	/**
	 * Adds the given member to the internal object pool.
	 * Note that if the pool is full this method does nothing
	 * and simply discards the given {@code member}.
	 *
	 * @param member
	 * @return {@code true} iff there was still enough space available to pool the given {@code member}
	 */
	public boolean recycle(M member) {
		requireNonNull(member);
		ArrayList<M> pool = ensurePool();

		boolean canAdd = pool.size()<poolSize;

		if(canAdd) {
			pool.add(member);
		}

		return canAdd;
	}

	public boolean isEmpty() {
		ArrayList<M> pool = this.pool;
		return pool==null || pool.isEmpty();
	}

	/**
	 * Returns the last pooled member or {@code null} if there currently is
	 * no object pooled.
	 * @return
	 */
	public M revive() {
		if(isEmpty()) {
			return null;
		}

		ArrayList<M> pool = ensurePool();

		return pool.remove(pool.size()-1);
	}

	public void recycleAll(Collection<? extends M> members) {
		requireNonNull(members);
		ArrayList<M> pool = ensurePool();

		for(M member : members) {
			if(pool.size()>=poolSize) {
				break;
			}

			pool.add(member);
		}
	}

	public void recycleAll(DataSequence<? extends M> members) {
		requireNonNull(members);
		ArrayList<M> pool = ensurePool();

		for(int i = IcarusUtils.limitToIntegerValueRange(members.entryCount()-1); i>=0; i--) {
			if(pool.size()>=poolSize) {
				break;
			}

			pool.add(members.elementAt(i));
		}
	}

	/**
	 * @see java.util.function.Supplier#get()
	 */
	@Override
	public M get() {
		return revive();
	}

	/**
	 * @see java.util.function.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(M t) {
		recycle(t);
	}
}
