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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/cache/MemberCache.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.driver.cache;

import static de.ims.icarus2.util.Conditions.checkNotNull;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.standard.driver.cache.CachedMember.CachedItem;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.mem.HeapMember;
import de.ims.icarus2.util.mem.Primitive;
import de.ims.icarus2.util.mem.Reference;

/**
 * @author Markus Gärtner
 * @version $Id: MemberCache.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
@HeapMember
@AccessControl(AccessPolicy.DENY)
public class MemberCache<M extends CachedItem> {

	@Reference
	private TLongObjectMap<M> cache;

	@Primitive
	private final int limit;

	@Primitive
	private final float loadFactor;

	public MemberCache() {
		this(1000, -1, 0.85f);
	}

	/**
	 * Constructs a new, empty hash-table with the specified initial capacity and
	 * default load factor, which is <code>0.75</code>.
	 *
	 * @param initialCapacity the initial capacity of the hash-table.
	 * @throws IllegalArgumentException if the initial capacity is less than zero.
	 */
	public MemberCache(int initialCapacity) {
		this(initialCapacity, -1, 0.85f);
	}

	public MemberCache(int initialCapacity, int limit, float loadFactor) {
		if (initialCapacity <= 0)
			throw new IllegalArgumentException("Illegal capacity (zero or less): " //$NON-NLS-1$
					+ initialCapacity);
		if (loadFactor <= 0)
			throw new IllegalArgumentException("Illegal load-factor (zero or less): " + loadFactor); //$NON-NLS-1$

		this.loadFactor = loadFactor;
		this.limit = limit;

		resizeCache(initialCapacity);
	}

	public void resizeCache(int cacheSize) {
		if(cache!=null && cacheSize<=cache.size()) {
			return;
		}

		TLongObjectMap<M> oldCache = cache;

		cache = new TLongObjectHashMap<>(cacheSize, loadFactor);

		if(oldCache!=null && !oldCache.isEmpty()) {
			cache.putAll(oldCache);
		}
	}

	@AccessRestriction(AccessMode.ALL)
	public int size() {
		return cache.size();
	}

	@AccessRestriction(AccessMode.ALL)
	public boolean isEmpty() {
		return cache.isEmpty();
	}

	/**
	 * Checks whether or not an entry for a given key is already present
	 * @param key
	 * @return
	 */
	@AccessRestriction(AccessMode.ALL)
	public boolean isCached(long key) {
		return cache.containsKey(key);
	}

	/**
	 * Checks whether or not an entry for a given member is already present
	 * @param key
	 * @return
	 */
	@AccessRestriction(AccessMode.ALL)
	public boolean isCached(M member) {
		checkNotNull(member);

		return cache.containsKey(member.getIndex());
	}

	/**
	 * Creates a new entry to map the given key to the given member.
	 *
	 * @param key
	 * @param member
	 * @throws ModelException if there already is an entry for the given key
	 */
	@AccessRestriction(AccessMode.MANAGE)
	public boolean register(long key, M member) {
		checkNotNull(member);

		if(limit>=0 && cache.size()>=limit) {
			return false;
		}

		if(cache.putIfAbsent(key, member)!=null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Key already used for mapping: "+key);

		return true;
	}

	/**
	 * Looks up the member registered for the given key and returns it if present.
	 * @param key
	 * @return
	 */
	@AccessRestriction(AccessMode.ALL)
	public M lookup(long key) {
		return cache.get(key);
	}

	@AccessRestriction(AccessMode.MANAGE)
	public M remove(long key) {
		return cache.remove(key);
	}

	@AccessRestriction(AccessMode.MANAGE)
	public void remove(M member) {
		checkNotNull(member);

		cache.remove(member.getIndex());
	}

	@AccessRestriction(AccessMode.MANAGE)
	public void clear() {
		cache.clear();
	}

	@AccessRestriction(AccessMode.MANAGE)
	public void recycleTo(MemberPool<? super M> pool) {
		if(isEmpty()) {
			return;
		}

		//FIXME does not remove recycled members from cache!
		cache.forEachValue(new TObjectProcedure<M>() {

			@Override
			public boolean execute(M object) {
				return pool.recycle(object);
			}
		});
	}
}
