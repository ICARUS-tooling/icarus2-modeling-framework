/**
 *
 */
package de.ims.icarus2.util;

import java.util.function.Predicate;

/**
 * @author Markus Gärtner
 *
 */
public interface Searchable<T extends Object> extends Traversable<T> {

	void forEachUntil(Predicate<? super T> check);

	default T find(Predicate<? super T> check) {
		Mutable<T> buffer = new Mutable.MutableObject<>();
		forEachUntil(check.and(item -> {
			buffer.set(item);
			return true;
		}));
		return buffer.get();
	}
}
