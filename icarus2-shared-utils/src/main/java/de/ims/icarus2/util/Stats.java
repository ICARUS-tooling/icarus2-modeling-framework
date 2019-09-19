/**
 *
 */
package de.ims.icarus2.util;

import static de.ims.icarus2.util.Conditions.checkArgument;

import java.util.concurrent.atomic.LongAdder;

import de.ims.icarus2.util.strings.ToStringBuilder;

/**
 * @author Markus Gärtner
 *
 */
public final class Stats<T extends Enum<T>> implements Cloneable {

	private final LongAdder[] counts;
	private final Class<T> type;

	public Stats(Class<T> type) {
		checkArgument("Not an enum: "+type, type.isEnum());
		this.type = type;
		counts = new LongAdder[type.getEnumConstants().length];
		reset();
	}

	private Stats(Stats<T> source) {
		counts = new LongAdder[source.counts.length];
		type = source.type;
		for (int i = 0; i < counts.length; i++) {
			counts[i] = new LongAdder();
			counts[i].add(source.counts[i].longValue());
		}
	}

	public synchronized void reset() {
		for (int i = 0; i < counts.length; i++) {
			counts[i] = new LongAdder();
		}
	}

	@Override
	public synchronized Stats<T> clone() {
		return new Stats<>(this);
	}

	private LongAdder forKey(T key) {
		return counts[key.ordinal()];
	}

	public synchronized Stats<T> count(T key) {
		forKey(key).increment();
		return this;
	}

	public synchronized Stats<T> reset(T key) {
		forKey(key).reset();
		return this;
	}

	public synchronized long getCount(T key) {
		return forKey(key).longValue();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		ToStringBuilder tsb = ToStringBuilder.create();
		for(T key : type.getEnumConstants()) {
			tsb.add(key.name(), forKey(key).longValue());
		}
		return tsb.build();
	}
}
