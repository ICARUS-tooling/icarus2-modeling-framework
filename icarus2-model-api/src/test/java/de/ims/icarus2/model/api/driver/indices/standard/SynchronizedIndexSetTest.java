/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.test.DelegateTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.factories.DelegateTestFactory;

/**
 * @author Markus Gärtner
 *
 */
class SynchronizedIndexSetTest implements DelegateTest<SynchronizedIndexSet, IndexSet> {

	@Test
	void testConstructorNull() {
		assertNPE(() -> new SynchronizedIndexSet(null));
	}

	@Override
	public Class<?> getTestTargetClass() {
		return SynchronizedIndexSet.class;
	}

	@Override
	public SynchronizedIndexSet createTestInstance(TestSettings settings) {
		return settings.process(new SynchronizedIndexSet(mock(IndexSet.class, CALLS_REAL_METHODS)));
	}

	@Override
	public void configure(DelegateTestFactory<SynchronizedIndexSet, IndexSet> factory) {
		factory.sourceClass(IndexSet.class);
		factory.delegateGenerator(SynchronizedIndexSet::new);
		factory.methodFilter(m -> Modifier.isSynchronized(m.getModifiers())
				&& !"getFeatures".equals(m.getName()));
		factory.sourceProcessor(set -> doReturn(IndexSet.DEFAULT_FEATURES).when(set).getFeatures());
	}
}
