/**
 *
 */
package de.ims.icarus2.model.standard.members.layers.annotation.unbound;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ManagedAnnotationStorageTest;
import de.ims.icarus2.model.api.layer.MultiKeyAnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
class ComplexAnnotationStorageTest implements ManagedAnnotationStorageTest<ComplexAnnotationStorage>,
		MultiKeyAnnotationStorageTest<ComplexAnnotationStorage> {

	@Override
	public Object testValue(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> keys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends ComplexAnnotationStorage> getTestTargetClass() {
		return ComplexAnnotationStorage.class;
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
		return new ComplexAnnotationStorage();
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
