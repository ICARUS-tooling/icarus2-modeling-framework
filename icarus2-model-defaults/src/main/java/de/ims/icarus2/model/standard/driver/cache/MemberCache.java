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
package de.ims.icarus2.model.standard.driver.cache;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Primitive;
import de.ims.icarus2.util.mem.Reference;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
@Assessable
@AccessControl(AccessPolicy.DENY)
public class MemberCache<M extends Item> {

	@Reference
	private Long2ObjectMap<M> cache;

	@Primitive
	private final int limit;

	@Primitive
	private final float loadFactor;

	public MemberCache() {
		this(1000, -1, 0.85f);
	}

	/**
	 * Constructs a new, empty hash-table with the specified initial capacity and
	 * default load factor, which is <code>0.5</code>.
	 *
	 * @param initialCapacity the initial capacity of the hash-table.
	 * @throws IllegalArgumentException if the initial capacity is less than zero.
	 */
	public MemberCache(int initialCapacity) {
		this(initialCapacity, -1, 0.5F);
	}

	public MemberCache(int initialCapacity, int limit, float loadFactor) {
		if (initialCapacity <= 0)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Illegal capacity (zero or less): " + initialCapacity);
		if (loadFactor <= 0f)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Illegal load-factor (zero or less): " + loadFactor);

		this.loadFactor = loadFactor;
		this.limit = limit;

		cache = new Long2ObjectOpenHashMap<>(initialCapacity, loadFactor);
	}

	/**
	 * Returns the currently used up size of this cache.
	 */
	@AccessRestriction(AccessMode.ALL)
	public int size() {
		return cache.size();
	}

	/**
	 * Returns {@code true} iff there are no items cached.
	 */
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
		requireNonNull(member);

		return cache.containsKey(member.getIndex());
	}

	public boolean register(M member) {
		return register(member.getIndex(), member);
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
		requireNonNull(member);

		if(limit>=0 && cache.size()>=limit) {
			return false;
		}

		if(cache.putIfAbsent(key, member) != cache.defaultReturnValue())
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
		requireNonNull(member);

		cache.remove(member.getIndex());
	}

	@AccessRestriction(AccessMode.MANAGE)
	public void clear() {
		cache.clear();
	}

	/**
	 * {@link MemberPool#recycle(Item) recycles} all the elements currently
	 * cached and then {@link #clear() clears} this cache.
	 */
	@AccessRestriction(AccessMode.MANAGE)
	public void recycleTo(MemberPool<? super M> pool) {
		if(isEmpty()) {
			return;
		}

		cache.values().forEach(pool::recycle);
		cache.clear();
	}
}
