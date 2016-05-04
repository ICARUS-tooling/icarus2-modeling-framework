/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/cache/MemberPool.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.driver.cache;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.mem.HeapMember;
import de.ims.icarus2.util.mem.Primitive;
import de.ims.icarus2.util.mem.Reference;

/**
 * @author Markus Gärtner
 * @version $Id: MemberPool.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
@HeapMember
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


	/**
	 * Adds the given member to the internal object pool.
	 * Note that if the pool is full this method does nothing
	 * and simply discards the given {@code member}.
	 *
	 * @param member
	 * @return {@code true} iff there was still enough space available to pool the given {@code member}
	 */
	public boolean recycle(M member) {
		checkNotNull(member);

		if(pool==null) {
			synchronized (this) {
				if(pool==null) {
					pool = new ArrayList<>(poolSize);
				}
			}
		}

		if(pool.size()<poolSize) {
			pool.add(member);
			return true;
		}

		return false;
	}

	/**
	 * Returns the last pooled member or {@code null} if there currently is
	 * no object pooled.
	 * @return
	 */
	public M revive() {
		if(pool==null || pool.isEmpty()) {
			return null;
		}

		return pool.remove(pool.size()-1);
	}

	public void recycleAll(Collection<? extends M> members) {
		checkNotNull(members);

		if(pool==null) {
			pool = new ArrayList<>(poolSize);
		}

		for(M member : members) {
			if(pool.size()>=poolSize) {
				break;
			}

			pool.add(member);
		}
	}

	public void recycleAll(DataSequence<? extends M> members) {
		checkNotNull(members);

		if(pool==null) {
			pool = new ArrayList<>(poolSize);
		}

		for(int i = limitToIntegerValueRange(members.entryCount()); i>=0; i--) {
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
