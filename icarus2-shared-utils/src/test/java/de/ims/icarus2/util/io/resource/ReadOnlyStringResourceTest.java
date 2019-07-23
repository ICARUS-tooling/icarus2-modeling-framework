/**
 *
 */
package de.ims.icarus2.util.io.resource;

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.util.AccessMode;

/**
 * @author Markus Gärtner
 *
 */
class ReadOnlyStringResourceTest implements IOResourceTest<ReadOnlyStringResource> {

	private static final String DEFAULT_CONTENT = TestUtils.LOREM_IPSUM_ASCII;

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResourceTest#getSupportedAccessModes()
	 */
	@Override
	public Set<AccessMode> getSupportedAccessModes() {
		return singleton(AccessMode.READ);
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends ReadOnlyStringResource> getTestTargetClass() {
		return ReadOnlyStringResource.class;
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResourceTest#create(de.ims.icarus2.util.AccessMode)
	 */
	@Override
	public ReadOnlyStringResource create(AccessMode accessMode) throws IOException {
		assertEquals(AccessMode.READ, accessMode);
		return create(DEFAULT_CONTENT);
	}

	public ReadOnlyStringResource create(String content) {
		return new ReadOnlyStringResource(content);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ReadOnlyStringResource#delete()}.
	 * @throws IOException
	 */
	@Test
	void testDelete() throws IOException {
		ReadOnlyStringResource resource = create(DEFAULT_CONTENT);
		assertEquals(DEFAULT_CONTENT, resource.getSource());
		resource.delete();
		assertNull(resource.getSource());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ReadOnlyStringResource#delete()}.
	 * @throws IOException
	 */
	@Test
	void testDeletedSource() throws IOException {
		ReadOnlyStringResource resource = create(DEFAULT_CONTENT);
		resource.delete();

		assertIcarusException(GlobalErrorCode.ILLEGAL_STATE,
				() -> resource.prepare());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ReadOnlyStringResource#size()}.
	 * @throws IOException
	 */
	@Test
	void testSizeEmpty() throws IOException {
		ReadOnlyStringResource resource = create("");
		resource.prepare();
		assertEquals(0, resource.size());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ReadOnlyStringResource#size()}.
	 * @throws IOException
	 */
	@Test
	void testSizeFilled() throws IOException {
		ReadOnlyStringResource resource = create(DEFAULT_CONTENT);
		resource.prepare();
		assertEquals(DEFAULT_CONTENT.length(), resource.size());
	}

	/**
	 * Test method for reading.
	 * @throws IOException
	 */
	@Test
	void testRead() throws IOException {
		ReadOnlyStringResource resource = create(DEFAULT_CONTENT);
		resource.prepare();
		assertArrayEquals(DEFAULT_CONTENT.getBytes(resource.getEncoding()),
				read(resource));
	}
}
