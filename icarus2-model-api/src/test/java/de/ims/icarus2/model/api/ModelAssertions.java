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
/**
 *
 */
package de.ims.icarus2.model.api;

import org.assertj.core.api.InstanceOfAssertFactory;

import de.ims.icarus2.model.api.driver.indices.IndexSet;

/**
 * @author Markus Gärtner
 *
 */
public final class ModelAssertions {

	// Assertions

	public static IndexSetAssert assertThat(IndexSet actual) {
		return new IndexSetAssert(actual);
	}

	public static IndexSetArrayAssert assertThat(IndexSet[] actual) {
		return new IndexSetArrayAssert(actual);
	}

	// Assertion Factories

	public static final InstanceOfAssertFactory<IndexSet, IndexSetAssert> INDEX_SET =
			new InstanceOfAssertFactory<>(IndexSet.class, ModelAssertions::assertThat);

	public static final InstanceOfAssertFactory<IndexSet[], IndexSetArrayAssert> INDEX_SETS =
			new InstanceOfAssertFactory<>(IndexSet[].class, ModelAssertions::assertThat);
}
