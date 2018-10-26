/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
public interface Hierarchy<E extends Object> extends Lockable, Iterable<E> {

	public static final int ROOT = 0;

	E getRoot();

	int getDepth();

	E atLevel(int level);

	default Optional<E> tryLevel(int level) {
		return Optional.ofNullable(level<getDepth() ? atLevel(level) : null);
	}

	default Optional<E> adjacent(E item, Direction direction) {
		int level = levelOf(item);
		if(level != -1) {
			level += direction==Direction.BELOW ? 1 : -1;
			if(level>=0 && level <getDepth()) {
				return Optional.of(atLevel(level));
			}
		}

		return Optional.empty();
	}

	void add(E item);

	void remove(E item);

	void insert(E item, int index);

	/**
	 * Returns the level of specified {@code item} or {@code -1}
	 * if the item is not contained in this heirarchy.
	 *
	 * @param item
	 * @return
	 */
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

	public enum Direction {
		ABOVE,
		BELOW,
		;
	}

	@SuppressWarnings("unchecked")
	public static <E extends Object> Hierarchy<E> empty() {
		return (Hierarchy<E>) EMPTY;
	}

	public static final Hierarchy<Object> EMPTY = new Hierarchy<Object>() {

		private final Collection<Object> buffer = Collections.emptyList();

		@Override
		public void lock() {
			// no-op
		}

		@Override
		public boolean isLocked() {
			return true;
		}

		@Override
		public Iterator<Object> iterator() {
			return buffer.iterator();
		}

		@Override
		public Object getRoot() {
			return null;
		}

		@Override
		public int getDepth() {
			return 0;
		}

		@Override
		public Object atLevel(int level) {
			throw new IndexOutOfBoundsException();
		}

		@Override
		public void add(Object item) {
			// no-op
		}

		@Override
		public void remove(Object item) {
			// no-op
		}

		@Override
		public void insert(Object item, int index) {
			// no-op
		}

		@Override
		public int levelOf(Object item) {
			return -1;
		}

	};
}
