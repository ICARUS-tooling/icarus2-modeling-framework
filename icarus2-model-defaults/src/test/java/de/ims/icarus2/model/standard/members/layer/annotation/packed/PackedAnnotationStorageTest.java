/**
 *
 */
package de.ims.icarus2.model.standard.members.layer.annotation.packed;

import java.util.Set;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest;
import de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.PackedAnnotationStorage;
import de.ims.icarus2.test.annotations.PostponedTest;

/**
 * @author Markus Gärtner
 *
 */
@PostponedTest("need to test packedDataManager first!!!")
class PackedAnnotationStorageTest implements AnnotationStorageTest<PackedAnnotationStorage>,
		ManagedAnnotationStorageTest<PackedAnnotationStorage> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends PackedAnnotationStorage> getTestTargetClass() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#typesForSetters(java.lang.String)
	 */
	@Override
	public Set<ValueType> typesForSetters(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#typesForGetters(java.lang.String)
	 */
	@Override
	public Set<ValueType> typesForGetters(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#testValue(java.lang.String)
	 */
	@Override
	public Object testValue(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#valueType(java.lang.String)
	 */
	@Override
	public ValueType valueType(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#createForLayer(de.ims.icarus2.model.api.layer.AnnotationLayer)
	 */
	@Override
	public PackedAnnotationStorage createForLayer(AnnotationLayer layer) {
		// TODO Auto-generated method stub
		return null;
	}

}
