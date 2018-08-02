/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.Documentation.Resource;
import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface DocumentationTest extends LockableTest, ModifiableIdentityTest {

	@Override
	Documentation createUnlocked();

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableIdentityTest#createEmpty()
	 */
	@Override
	default ModifiableIdentity createEmpty() {
		return createUnlocked();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#getManifestType()}.
	 */
	@Test
	default void testGetManifestType() {
		assertEquals(ManifestType.DOCUMENTATION, createUnlocked().getManifestType());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#getContent()}.
	 */
	@Test
	default void testGetContent() {
		assertNull(createUnlocked().getContent());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#getResources()}.
	 */
	@Test
	default void testGetResources() {
		assertTrue(createUnlocked().getResources().isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#forEachResource(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachResource() {
		Resource[] resources = {
			mock(Resource.class),
			mock(Resource.class),
			mock(Resource.class),
			mock(Resource.class),
		};

		Documentation documentation = createUnlocked();
		for(Resource resource : resources) {
			documentation.addResource(resource);
		}

		TestUtils.assertForEachSorted(documentation::forEachResource, resources);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#setContent(java.lang.String)}.
	 */
	@Test
	default void testSetContent() {
		Documentation documentation = createUnlocked();

		documentation.setContent("content");
		assertEquals("content", documentation.getContent());

		documentation.setContent(null);
		assertNull(documentation.getContent());

		documentation.lock();
		LockableTest.assertLocked(() -> documentation.setContent("content"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#addResource(de.ims.icarus2.model.manifest.api.Documentation.Resource)}.
	 */
	@Test
	default void testAddResource() {
		Documentation documentation = createUnlocked();

		Resource resource = mock(Resource.class);
		documentation.addResource(resource);

		List<Resource> resources = documentation.getResources();

		assertEquals(1, resources.size());
		assertTrue(resources.contains(resource));

		assertThrows(NullPointerException.class, () -> documentation.addResource(null));

		documentation.lock();
		LockableTest.assertLocked(() -> documentation.addResource(resource));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#removeResource(de.ims.icarus2.model.manifest.api.Documentation.Resource)}.
	 */
	@Test
	default void testRemoveResource() {
		Documentation documentation = createUnlocked();

		Resource resource1 = mock(Resource.class);
		Resource resource2 = mock(Resource.class);

		documentation.addResource(resource1);
		documentation.addResource(resource2);

		documentation.removeResource(resource1);

		List<Resource> resources = documentation.getResources();

		assertEquals(1, resources.size());
		assertTrue(resources.contains(resource2));

		documentation.lock();
		LockableTest.assertLocked(() -> documentation.removeResource(resource2));
	}

}
