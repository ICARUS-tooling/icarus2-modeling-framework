/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.io.sets;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 * @author Markus Gärtner
 *
 */
public interface ResourceSetTest<S extends ResourceSet> extends ApiGuardedTest<S> {

	/** Create a resource set with at least 1 resourc ein it */
	@Provider
	S createFilled();

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.sets.ResourceSet#getResourceAt(int)}.
	 */
	@Test
	default void testGetResourceAt() {
		S set = createFilled();
		assertThat(set.getResourceCount()).isPositive();
		for (int i = 0; i < set.getResourceCount(); i++) {
			assertThat(set.getResourceAt(i)).isNotNull();
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.sets.ResourceSet#indexOfResource(de.ims.icarus2.util.io.resource.IOResource)}.
	 */
	@Test
	default void testIndexOfResource() {
		S set = createFilled();
		assertThat(set.getResourceCount()).isPositive();
		for (int i = 0; i < set.getResourceCount(); i++) {
			assertThat(set.indexOfResource(set.getResourceAt(i))).isEqualTo(i);
		}

		assertThat(set.indexOfResource(mock(IOResource.class))).isEqualTo(UNSET_INT);
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.sets.ResourceSet#forEachResource(java.util.function.IntConsumer)}.
	 */
	@Test
	default void testForEachResourceIntConsumer() {
		S set = createFilled();
		assertThat(set.getResourceCount()).isPositive();
		IntConsumer action = mock(IntConsumer.class);
		set.forEachResource(action);
		for (int i = 0; i < set.getResourceCount(); i++) {
			verify(action).accept(i);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.sets.ResourceSet#forEachResource(java.util.function.ObjIntConsumer)}.
	 */
	@Test
	default void testForEachResourceObjIntConsumerOfIOResource() {
		S set = createFilled();
		assertThat(set.getResourceCount()).isPositive();
		ObjIntConsumer<IOResource> action = mock(ObjIntConsumer.class);
		set.forEachResource(action);
		for (int i = 0; i < set.getResourceCount(); i++) {
			verify(action).accept(set.getResourceAt(i), i);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.io.sets.ResourceSet#forEachResource(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachResourceConsumerOfIOResource() {
		S set = createFilled();
		assertThat(set.getResourceCount()).isPositive();
		Consumer<IOResource> action = mock(Consumer.class);
		set.forEachResource(action);
		for (int i = 0; i < set.getResourceCount(); i++) {
			verify(action).accept(set.getResourceAt(i));
		}
	}

}
