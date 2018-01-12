/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus
 *
 */
public interface Categorizable {

	boolean addCategory(Category category);

	boolean removeCategory(Category category);

	void forEachCategory(Consumer<? super Category> action);

	default Set<Category> getCategories() {
		LazyCollection<Category> result = LazyCollection.lazySet();
		forEachCategory(result);
		return result.getAsSet();
	}

	default boolean hasCategory(Category category) {
		MutableBoolean result = new MutableBoolean(false);
		forEachCategory(c -> {
			if(c.equals(category))
				result.setBoolean(true);
		});
		return result.booleanValue();
	}
}
