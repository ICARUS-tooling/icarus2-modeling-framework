/**
 *
 */
package de.ims.icarus2.model.api;

import org.assertj.core.api.AbstractObjectArrayAssert;

import de.ims.icarus2.model.api.driver.indices.IndexSet;

/**
 * @author Markus Gärtner
 *
 */
public class IndexSetArrayAssert extends AbstractObjectArrayAssert<IndexSetArrayAssert, IndexSet> {

	public IndexSetArrayAssert(IndexSet...actual) {
		super(actual, IndexSetArrayAssert.class);
	}

	@Override
	protected IndexSetArrayAssert newObjectArrayAssert(IndexSet[] array) {
		return new IndexSetArrayAssert(array);
	}

	public IndexSetAssert element(int index) {
		hasSizeGreaterThan(index);
		return new IndexSetAssert(actual[index]);
	}

	public IndexSetAssert onlyElement() {
		hasSize(1);
		return new IndexSetAssert(actual[0]);
	}

	//TODO
}
