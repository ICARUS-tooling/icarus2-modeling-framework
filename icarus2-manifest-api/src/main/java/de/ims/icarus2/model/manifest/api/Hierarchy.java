/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Consumer;

import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
public interface Hierarchy<E extends Object> extends Lockable, Iterable<E> {

	E getRoot();

	int getDepth();

	E atLevel(int level);

	void add(E item);

	void remove(E item);

	void insert(E item, int index);

	int levelOf(E item);

	default boolean isEmpty() {
		return getDepth()==0;
	}

	default void forEachItem(Consumer<? super E> action) {
		requireNonNull(action);
		for(int i=0; i<getDepth(); i++) {
			action.accept(atLevel(i));
		}
	}

	default List<E> getItems() {
		return LazyCollection.<E>lazyList()
				.addFromForEach(this::forEachItem)
				.getAsList();
	}
}
