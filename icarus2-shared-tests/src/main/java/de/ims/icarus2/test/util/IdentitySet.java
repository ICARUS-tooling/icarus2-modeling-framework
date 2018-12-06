/**
 *
 */
package de.ims.icarus2.test.util;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Markus Gärtner
 *
 */
public class IdentitySet<E extends Object> extends AbstractCollection<E> {

    private final LinkedList<E> list = new LinkedList<>();

    @Override
	public boolean contains(Object o) {
        for(E existing:list) {
            if (existing == o) {
                return true;
            }
        }
        return false;
    }

    @Override
	public boolean add(E o) {
        list.add(o);
        return true;
    }

	/**
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return list.iterator();
	}

	/**
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return list.size();
	}
}
