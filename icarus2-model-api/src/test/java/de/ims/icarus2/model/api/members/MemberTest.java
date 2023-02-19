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
package de.ims.icarus2.model.api.members;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.contracts.EqualsContract;
import de.ims.icarus2.test.contracts.HashContract;

/**
 * @author Markus Gärtner
 *
 */
public interface MemberTest<I extends Item> extends GenericTest<I>, ApiGuardedTest<I>,
		EqualsContract<I>, HashContract<I> {

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default I createTestInstance(TestSettings settings) {
		return settings.process(createNoArgs());
	}

	/**
	 * This implementation return the {@code source} argument.
	 *
	 * {@inheritDoc}
	 *
	 * @see de.ims.icarus2.test.contracts.EqualsContract#createEqual(java.lang.Object)
	 */
	@Override
	default I createEqual(I source) {
		return source;
	}
}
