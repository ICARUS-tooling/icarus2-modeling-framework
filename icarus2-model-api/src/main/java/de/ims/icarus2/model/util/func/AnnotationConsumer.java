/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.util.func;

import static de.ims.icarus2.util.lang.Primitives.cast;

import java.util.function.Consumer;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;

/**
 * A specialized non-primitive version of the {@link Consumer} interface,
 * meant to consume entire triples of annotations, consisting of the
 * annotated {@link Item}, the {@link AnnotationManifest#getKey() annotation key}
 * and the {@code AnnotationLayer#getValue(Item, String) annotation value} itself.
 *
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface AnnotationConsumer<E extends Object> {

	void apply(Item item, String key, E value);

	@FunctionalInterface
	public interface IntAnnotationConsumer extends AnnotationConsumer<Integer> {

		@Override
		default void apply(Item item, String key, Integer value) {
			applyInt(item, key, cast(value));
		}

		void applyInt(Item item, String key, int value);
	}

	@FunctionalInterface
	public interface LongAnnotationConsumer extends AnnotationConsumer<Long> {

		@Override
		default void apply(Item item, String key, Long value) {
			applyLong(item, key, cast(value));
		}

		void applyLong(Item item, String key, long value);
	}

	@FunctionalInterface
	public interface FloatAnnotationConsumer extends AnnotationConsumer<Float> {

		@Override
		default void apply(Item item, String key, Float value) {
			applyFloat(item, key, cast(value));
		}

		void applyFloat(Item item, String key, float value);
	}

	@FunctionalInterface
	public interface DoubleAnnotationConsumer extends AnnotationConsumer<Double> {

		@Override
		default void apply(Item item, String key, Double value) {
			applyDouble(item, key, cast(value));
		}

		void applyDouble(Item item, String key, double value);
	}

	@FunctionalInterface
	public interface BooleanAnnotationConsumer extends AnnotationConsumer<Boolean> {

		@Override
		default void apply(Item item, String key, Boolean value) {
			applyBoolean(item, key, cast(value));
		}

		void applyBoolean(Item item, String key, boolean value);
	}
}
