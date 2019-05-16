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
/**
 *
 */
package de.ims.icarus2.model.standard.members.container;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class ListItemStorageIntTest implements ItemStorageTest<ListItemStorageInt> {

	@Nested
	class Constructors {

		@Test
		void noArgs() {
			new ListItemStorageInt();
		}

		@Test
		void withCapacity() {
			new ListItemStorageInt(100);
		}

		@Test
		void withDefaultCapacity() {
			new ListItemStorageInt(UNSET_INT);
		}

		@Test
		void invalidCapacity() {
			IcarusRuntimeException exception = assertThrows(IcarusRuntimeException.class,
					() -> new ListItemStorageInt(-10));
			assertEquals(GlobalErrorCode.INVALID_INPUT, exception.getErrorCode());
		}
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends ListItemStorageInt> getTestTargetClass() {
		return ListItemStorageInt.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public ListItemStorageInt createTestInstance(TestSettings settings) {
		return settings.process(new ListItemStorageInt());
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.ItemStorageTest#getExpectedContainerType()
	 */
	@Override
	public ContainerType getExpectedContainerType() {
		return ContainerType.LIST;
	}

	//TODO add tests for actual storage logic
}
