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
