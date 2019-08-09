/**
 *
 */
package de.ims.icarus2.model.standard.members.layers.annotation.unbound;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.AnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
class ComplexAnnotationStorageTest implements AnnotationStorageTest<ComplexAnnotationStorage> {

	@Override
	public Class<? extends ComplexAnnotationStorage> getTestTargetClass() {
		return ComplexAnnotationStorage.class;
	}

	@Override
	public ValueType getValueType() {
		return ValueType.UNKNOWN;
	}

	@Override
	public Set<ValueType> typesForSetters() {
		return ALL_TYPES;
	}

	@Override
	public Set<ValueType> typesForGetters() {
		return ALL_TYPES;
	}

	@Override
	public ComplexAnnotationStorage createForLayer(AnnotationLayer layer) {
		return new ComplexAnnotationStorage(ComplexAnnotationStorage.LARGE_BUNDLE_FACTORY);
	}

	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationStorage#ComplexAnnotationStorage(java.util.function.Supplier)}.
		 */
		@Test
		void testComplexAnnotationStorageSupplierOfAnnotationBundle() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationStorage#ComplexAnnotationStorage(int, java.util.function.Supplier)}.
		 */
		@Test
		void testComplexAnnotationStorageIntSupplierOfAnnotationBundle() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.unbound.ComplexAnnotationStorage#ComplexAnnotationStorage(boolean, int, java.util.function.Supplier)}.
		 */
		@Test
		void testComplexAnnotationStorageBooleanIntSupplierOfAnnotationBundle() {
			fail("Not yet implemented"); // TODO
		}

	}

}
