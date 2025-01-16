/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package de.ims.icarus2.util;

import static de.ims.icarus2.util.Conditions.checkArgument;

import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import de.ims.icarus2.util.strings.ToStringBuilder;

/**
 * @author Markus Gärtner
 *
 * @param <T> the enum used to identify fields
 * @param <K> type of values to be counted
 */
@ThreadSafe
public class CountingStats<T extends Enum<T>, K> implements Cloneable {

	private final Counter<K>[] counts;
	private final Class<T> type;

	@SuppressWarnings("unchecked")
	public CountingStats(Class<T> type) {
		checkArgument("Not an enum: "+type, type.isEnum());
		this.type = type;
		counts = new Counter[type.getEnumConstants().length];
		reset();
	}

	@SuppressWarnings("unchecked")
	private CountingStats(CountingStats<T,K> source) {
		counts = new Counter[source.counts.length];
		type = source.type;
		for (int i = 0; i < counts.length; i++) {
			counts[i] = source.counts[i].copy();
		}
	}

	public synchronized void reset() {
		for (int i = 0; i < counts.length; i++) {
			counts[i] = new Counter<>();
		}
	}

	@Override
	public synchronized CountingStats<T,K> clone() {
		return new CountingStats<>(this);
	}

	private Counter<K> forField(T key) {
		return counts[key.ordinal()];
	}

	public synchronized CountingStats<T,K> count(T field, K key) {
		forField(field).increment(key);
		return this;
	}

	public synchronized CountingStats<T,K> reset(T field) {
		forField(field).clear();
		return this;
	}

	public synchronized long getCount(T field, K key) {
		return forField(field).getCount(key);
	}

	public synchronized Set<K> getKeys(T field) {
		return forField(field).getItems();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		ToStringBuilder tsb = ToStringBuilder.create();
		for(T key : type.getEnumConstants()) {
			tsb.add(key.name(), forField(key).toString());
		}
		return tsb.build();
	}
}