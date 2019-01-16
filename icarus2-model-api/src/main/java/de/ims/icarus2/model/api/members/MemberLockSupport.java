/**
 *
 */
package de.ims.icarus2.model.api.members;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.Mutable.MutableObject;

/**
 * Provides a facility that stores and manages locks for arbitrary members
 * of the modeling framework.
 *
 * @author Markus Gärtner
 *
 */
public class MemberLockSupport {

	private Consumer<Runnable> getProtector(Item item) {
		//TODO implement
		throw new UnsupportedOperationException("not implemented");
	}

	public void doProtected(Item item, Runnable job) {
		getProtector(item).accept(job);
	}

	public <T extends Object> T doProtected(Item item, Supplier<T> job) {
		MutableObject<T> buffer = new MutableObject<>();
		getProtector(item).accept(() -> buffer.set(job.get()));
		return buffer.get();
	}

	public <I extends Item, T extends Object> T doProtected(I item, Function<I, T> job) {
		MutableObject<T> buffer = new MutableObject<>();
		getProtector(item).accept(() -> buffer.set(job.apply(item)));
		return buffer.get();
	}

	//TODO add a mechanism to handle lambdas that throw exceptions
}
