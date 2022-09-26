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
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;

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

	public static ItemAssert assertThat(Item actual) {
		return new ItemAssert(actual);
	}

	public static ContainerAssert assertThat(Container actual) {
		return new ContainerAssert(actual);
	}

	public static StructureAssert assertThat(Structure actual) {
		return new StructureAssert(actual);
	}

	public static EdgeAssert assertThat(Edge actual) {
		return new EdgeAssert(actual);
	}

	public static FragmentAssert assertThat(Fragment actual) {
		return new FragmentAssert(actual);
	}


	// Assertion Factories

	public static final InstanceOfAssertFactory<IndexSet, IndexSetAssert> INDEX_SET =
			new InstanceOfAssertFactory<>(IndexSet.class, ModelAssertions::assertThat);

	public static final InstanceOfAssertFactory<IndexSet[], IndexSetArrayAssert> INDEX_SETS =
			new InstanceOfAssertFactory<>(IndexSet[].class, ModelAssertions::assertThat);

	public static final InstanceOfAssertFactory<Item, ItemAssert> ITEM =
			new InstanceOfAssertFactory<>(Item.class, ModelAssertions::assertThat);

	public static final InstanceOfAssertFactory<Container, ContainerAssert> CONTAINER =
			new InstanceOfAssertFactory<>(Container.class, ModelAssertions::assertThat);

	public static final InstanceOfAssertFactory<Structure, StructureAssert> STRUCTURE =
			new InstanceOfAssertFactory<>(Structure.class, ModelAssertions::assertThat);

	public static final InstanceOfAssertFactory<Edge, EdgeAssert> EDGE =
			new InstanceOfAssertFactory<>(Edge.class, ModelAssertions::assertThat);

	public static final InstanceOfAssertFactory<Fragment, FragmentAssert> FRAGMENT =
			new InstanceOfAssertFactory<>(Fragment.class, ModelAssertions::assertThat);
}
