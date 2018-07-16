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
 */
package de.ims.icarus2.model.util.func;

import static de.ims.icarus2.util.lang.Primitives.cast;

import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface AnnotationConsumer<E extends Object> {

	void apply(Item item, String key, E value);

	public interface IntAnnotationConsumer extends AnnotationConsumer<Integer> {

		@Override
		default void apply(Item item, String key, Integer value) {
			applyInt(item, key, cast(value));
		}

		void applyInt(Item item, String key, int value);
	}

	public interface LongAnnotationConsumer extends AnnotationConsumer<Long> {

		@Override
		default void apply(Item item, String key, Long value) {
			applyLong(item, key, cast(value));
		}

		void applyLong(Item item, String key, long value);
	}

	public interface FloatAnnotationConsumer extends AnnotationConsumer<Float> {

		@Override
		default void apply(Item item, String key, Float value) {
			applyFloat(item, key, cast(value));
		}

		void applyFloat(Item item, String key, float value);
	}

	public interface DoubleAnnotationConsumer extends AnnotationConsumer<Double> {

		@Override
		default void apply(Item item, String key, Double value) {
			applyDouble(item, key, cast(value));
		}

		void applyDouble(Item item, String key, double value);
	}

	public interface BooleanAnnotationConsumer extends AnnotationConsumer<Boolean> {

		@Override
		default void apply(Item item, String key, Boolean value) {
			applyBoolean(item, key, cast(value));
		}

		void applyBoolean(Item item, String key, boolean value);
	}
}
