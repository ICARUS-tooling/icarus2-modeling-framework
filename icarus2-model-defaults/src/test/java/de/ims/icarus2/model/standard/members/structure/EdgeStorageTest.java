/**
 *
 */
package de.ims.icarus2.model.standard.members.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifier;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
public interface EdgeStorageTest<S extends EdgeStorage> extends ApiGuardedTest<S> {

	/**
	 * Return expected structure type or {@code null} if the implementation
	 * isn't bound to a single specific structure type.
	 */
	@Provider
	StructureType getExpectedStructureType();

	/**
	 * Mocks a {@link Container} usable for testing the current {@link EdgeStorage}.
	 * The default implementation creates a mock with access to the underlying
	 * {@link StructureManifest} and the associated {@link StructureLayerManifest}.
	 * The latter is configured to return {@link Optional#empty() empty} {@link Optional}
	 * objects for every method that uses {@link Optional} as a return type.
	 * <p>
	 * Subclasses should override this method to either adjust the returned structure
	 * or provide an alternative construction.
	 * @return
	 */
	default Structure createStructure() {
		StructureLayerManifest layerManifest = mock(StructureLayerManifest.class, inv -> {
			if(inv.getMethod().getReturnType()==Optional.class) {
				return Optional.empty();
			}
			return null;
		});
		StructureManifest manifest = mock(StructureManifest.class);
		when(manifest.getLayerManifest()).thenReturn(Optional.of(layerManifest));
		Structure structure = mock(Structure.class);
		when(structure.getManifest()).then(inv -> manifest);

		return structure;
	}

	/**
	 * @see de.ims.icarus2.test.ApiGuardedTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<S> apiGuard) {
		ApiGuardedTest.super.configureApiGuard(apiGuard);

		apiGuard.detectUnmarkedMethods(true)
			.nullGuard(true);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage##addNotify(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@Test
	default void testAddNotify() {
		S storage = create();
		Structure structure = createStructure();
		storage.addNotify(structure);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage##removeNotify(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@Test
	default void testRemoveNotify() {
		S storage = create();
		Structure structure = createStructure();
		storage.addNotify(structure);
		storage.removeNotify(structure);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage##getContainerType()}.
	 */
	@Test
	default void testGetStructureType() {
		StructureType expectedType = getExpectedStructureType();
		if(expectedType!=null) {
			assertEquals(expectedType, create().getStructureType());
		} else {
			assertNotNull(create().getStructureType());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage##createEditVerifier(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@Test
	default void testCreateEditVerifier() {
		S storage = create();
		Structure structure = createStructure();
		storage.addNotify(structure);

		try(StructureEditVerifier verifier = storage.createEditVerifier(structure,
				mock(ContainerEditVerifier.class))) {
			assertNotNull(verifier);
			assertSame(structure, verifier.getSource());
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage##recycle()}.
	 */
	@Test
	default void testRecycle() {
		create().recycle();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage##isDirty(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@Test
	default void testIsDirty() {
		S storage = create();
		Structure structure = createStructure();
		storage.addNotify(structure);

		assertFalse(storage.isDirty(structure));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage#getVirtualRoot(de.ims.icarus2.model.api.members.structure.Structure)}.
	 */
	@Test
	default void testGetVirtualRoot() {
		S storage = create();
		Structure structure = createStructure();
		storage.addNotify(structure);

		assertNotNull(storage.getVirtualRoot(structure));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.structure.EdgeStorage#isRoot(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testIsRoot() {
		S storage = create();
		Structure structure = createStructure();
		storage.addNotify(structure);

		assertFalse(storage.isRoot(structure, storage.getVirtualRoot(structure)));
	}

}
