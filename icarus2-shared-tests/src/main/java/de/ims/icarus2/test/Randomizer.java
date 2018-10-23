/**
 *
 */
package de.ims.icarus2.test;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * @author Markus Gärtner
 *
 */
public class Randomizer<T extends Object> {

	private final List<T> buffer = new ArrayList<>();
	private final Random rng = new Random(System.currentTimeMillis());

	public Randomizer(Collection<? extends T> items) {
		requireNonNull(items);
		if(items.size()<2)
			throw new IllegalArgumentException("No point in ranodmizing collection with less than 2 elements...");
		buffer.addAll(items);
	}

	public T randomize() {
		int index = rng.nextInt(buffer.size());
		return buffer.get(index);
	}
}
