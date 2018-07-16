/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 */
package de.ims.icarus2.model.api.driver.indices;

import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;


/**
 * @author Markus Gärtner
 *
 */
public interface IndexCollector extends LongConsumer, IntConsumer, Consumer<IndexSet> {

	void add(long index);

	default void add(long fromIndex, long toIndex) {
		for(long index = fromIndex; index <= toIndex; index++) {
			add(index);
		}
	}

	default void add(IndexSet indices) {
		indices.forEachIndex((LongConsumer)this);
	}

	default void add(IndexSet[] indices) {
		for(IndexSet set : indices) {
			add(set);
		}
	}

	@Override
	default void accept(long value) {
		add(value);
	}

	@Override
	default void accept(int value) {
		add(value);
	}

	@Override
	default void accept(IndexSet indices) {
		add(indices);
	}

	default void add(OfLong source) {
		source.forEachRemaining(this);
	}

	default void add(OfInt source) {
		source.forEachRemaining(this);
	}
}
