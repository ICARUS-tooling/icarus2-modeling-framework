/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.raster;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.util.id.IdentityTest;

/**
 * @author Markus Gärtner
 *
 */
public interface RasterAxisTest<R extends RasterAxis> extends IdentityTest<R> {

	/**
	 * Test method for {@link de.ims.icarus2.model.api.raster.RasterAxis#getMinValue()}.
	 */
	@Test
	default void testGetMinValue() {
		assertTrue(create().getMinValue()>=0);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.raster.RasterAxis#getMaxValue()}.
	 */
	@Test
	default void testGetMaxValue() {
		assertTrue(create().getMaxValue()>=0);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.raster.RasterAxis#getGranularity()}.
	 */
	@Test
	default void testGetGranularity() {
		assertTrue(create().getGranularity()>0);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.raster.RasterAxis#getRasterSize(de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.layer.FragmentLayer, java.lang.Object)}.
	 */
	@Test
	default void testGetRasterSize_NullArguments() {
		R instance = create();
		assertNPE(() -> instance.getRasterSize(null, mock(FragmentLayer.class), new Object()));
		assertNPE(() -> instance.getRasterSize(mockItem(), null, new Object()));
	}

}
