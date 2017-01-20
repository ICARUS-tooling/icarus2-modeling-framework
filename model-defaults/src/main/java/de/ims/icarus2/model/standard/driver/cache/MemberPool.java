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
package de.ims.icarus2.model.standard.driver.cache;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
	private int poolSize;

	public MemberPool() {
		this(1000);
	}

	public MemberPool(int poolSize) {
		resizePool(poolSize);
	}

	public void resizePool(int poolSize) {
		if (poolSize <= 0)
			throw new IllegalArgumentException("Illegal pool-size (negative or zero): " //$NON-NLS-1$
					+ poolSize);
		this.poolSize = poolSize;
		pool = null;
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

		for(int i = IcarusUtils.limitToIntegerValueRange(members.entryCount()); i>=0; i--) {
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
