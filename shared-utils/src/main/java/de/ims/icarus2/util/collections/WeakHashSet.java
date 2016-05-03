/*
 * $Revision: 380 $
 * $Date: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/collections/WeakHashSet.java $
 *
 * $LastChangedDate: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $LastChangedRevision: 380 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.collections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * @author Markus Gärtner
 * @version $Id: WeakHashSet.java 380 2015-04-02 01:28:48Z mcgaerty $
 *
 */
public class WeakHashSet<E extends Object> extends AbstractSet<E> {

	private static final Object dummy = "DUMMY"; //$NON-NLS-1$

	private final WeakHashMap<E, Object> store;

	public WeakHashSet() {
		store = new WeakHashMap<>();
	}

	public WeakHashSet(int initialCapacity) {
		store = new WeakHashMap<>(initialCapacity);
	}

	public WeakHashSet(Collection<? extends E> c) {
		store = new WeakHashMap<>(c.size());
		addAll(c);
	}

	/**
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return store.keySet().iterator();
	}

	/**
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return store.size();
	}

	@Override
	public boolean contains(Object o) {
		return store.containsKey(o);
	}

	@Override
	public Object[] toArray() {
		return store.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return store.keySet().toArray(a);
	}

	@Override
	public boolean add(E e) {
		if(store.containsKey(e)) {
			return false;
		} else {
			store.put(e, dummy);
			return true;
		}
	}

	@Override
	public boolean remove(Object o) {
		return store.remove(o) != null;
	}

	@Override
	public void clear() {
		store.clear();
	}

}
