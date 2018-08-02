/**
 *
 */
package de.ims.icarus2.util;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Map;

import de.ims.icarus2.util.strings.StringResource;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * A simple utility class for enum implementations of the
 * {@link StringResource} interface.
 *
 * @author Markus Gärtner
 *
 */
public class LazyNameStore<F extends StringResource> {

	private Map<String, F> lookup;

	private final Class<F> clazz;

	/**
	 * @param clazz
	 */
	public LazyNameStore(Class<F> clazz) {
		this.clazz = requireNonNull(clazz);
		checkArgument(clazz.isEnum());
	}

	public synchronized F lookup(String name) {
		requireNonNull(name);

		if(lookup==null) {
			lookup = new Object2ObjectOpenHashMap<>();

			F[] flags = clazz.getEnumConstants();
			for(F flag : flags) {
				lookup.put(flag.getStringValue(), flag);
			}
		}

		F flag = lookup.get(name);
		if(flag==null)
			throw new IllegalArgumentException("Unknown flag name: "+name);
		return flag;
	}
}
