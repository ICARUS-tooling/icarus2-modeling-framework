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
 *
 * $Revision: 380 $
 * $Date: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/collections/BiDiMap.java $
 *
 * $LastChangedDate: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $ 
 * $LastChangedRevision: 380 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.collections;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Markus Gärtner
 * @version $Id: BiDiMap.java 380 2015-04-02 01:28:48Z mcgaerty $
 *
 */
public class BiDiMap<K extends Object, V extends Object> implements Map<K, V> {
	
	private Map<K, V> map;
	private Map<V, K> reverseMap;

	public BiDiMap() {
		map = new IdentityHashMap<>();
		reverseMap = new IdentityHashMap<>();
	}

	/**
	 * @see java.util.Map#size()
	 */
	@Override
	public synchronized int size() {
		return map.size();
	}

	/**
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public synchronized boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@Override
	public synchronized boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	/**
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@Override
	public synchronized boolean containsValue(Object value) {
		return reverseMap.containsKey(value);
	}

	/**
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override
	public synchronized V get(Object key) {
		return map.get(key);
	}
	
	public synchronized K getKey(Object value) {
		return reverseMap.get(value);
	}

	/**
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public synchronized V put(K key, V value) {
		V oldValue = map.put(key, value);
		reverseMap.put(value, key);
		
		return oldValue;
	}

	/**
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override
	public synchronized V remove(Object key) {
		V value = map.remove(key);
		if(value!=null) {
			reverseMap.remove(value);
		}
		return value;
	}
	
	public synchronized K removeValue(Object value) {
		K key = reverseMap.get(value);
		if(key!=null) {
			map.remove(key);
		}
		return key;
	}

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	@Override
	public synchronized void putAll(Map<? extends K, ? extends V> m) {
		for(Entry<? extends K, ? extends V> entry : m.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
			reverseMap.put(entry.getValue(), entry.getKey());
		}
	}

	/**
	 * @see java.util.Map#clear()
	 */
	@Override
	public synchronized void clear() {
		map.clear();
		reverseMap.clear();
	}

	/**
	 * @see java.util.Map#keySet()
	 */
	@Override
	public synchronized Set<K> keySet() {
		return map.keySet();
	}
	
	public synchronized Set<V> valueSet() {
		return reverseMap.keySet();
	}

	/**
	 * @see java.util.Map#values()
	 */
	@Override
	public synchronized Collection<V> values() {
		return reverseMap.keySet();
	}

	/**
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public synchronized Set<Entry<K, V>> entrySet() {
		return map.entrySet();
	}
	
	public synchronized Set<Entry<V, K>> reverseEntrySet() {
		return reverseMap.entrySet();
	}
}
