/**
 *
 */
package de.ims.icarus2.util.io.resource;

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

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

	public ReadOnlyStringResource create(String content, Charset encoding) {
		return new ReadOnlyStringResource(content, encoding);
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ReadOnlyStringResource#getSource()}.
	 */
	@Test
	void testGetSource() {
		ReadOnlyStringResource resource = create(DEFAULT_CONTENT);
		assertEquals(DEFAULT_CONTENT, resource.getSource());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.io.resource.ReadOnlyStringResource#getSource()}.
	 */
	@TestFactory
	Stream<DynamicTest> testGetEncoding() {
		return Stream.of(StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1, StandardCharsets.US_ASCII)
			.map(encoding -> dynamicTest(encoding.name(), () -> {
				assertSame(encoding, create(DEFAULT_CONTENT, encoding).getEncoding());
			}));
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
