/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestTest <M extends Manifest> extends ManifestFragmentTest {

	/**
	 * Create an empty and unlocked manifest for testing.
	 * The used {@link ManifestLocation} will report to not host templates.
	 *
	 * @see de.ims.icarus2.model.manifest.api.ManifestFragmentTest#createUnlocked()
	 */
	@Override
	default M createUnlocked() {
		return createUnlocked(mock(ManifestRegistry.class), mockManifestLocation(false));
	}

	@SuppressWarnings("boxing")
	public static ManifestLocation mockManifestLocation(boolean template) {
		ManifestLocation location = mock(ManifestLocation.class);
		when(location.isTemplate()).thenReturn(template);
		return location;
	}

	M createUnlocked(ManifestRegistry registry, ManifestLocation location);

	/**
	 * Creates a manifest instance for testing that is registered as a template
	 * with {@link Manifest#getId() id} {@code templateId} with the underlying
	 * registry.
	 *
	 * @return
	 */
	@SuppressWarnings("boxing")
	default M createTemplate() {
		ManifestRegistry registry = mock(ManifestRegistry.class);

		String id = "templateId";
		M template = createUnlocked(registry, mockManifestLocation(true));
		template.setIsTemplate(true);
		template.setId(id);

		when(registry.getTemplate(id)).thenReturn(template);
		when(registry.hasTemplate(id)).thenReturn(true);

		return template;
	}

	/**
	 * Create a manifest that is derived from the given template.
	 *
	 * @return
	 */
	default M createDerived(M template) {
		// Sanity check for 'template' actually being a template manifest
		assertNotNull(template.getId());
		assertTrue(template.isTemplate());

		// Sanity checks for proper template resolution
		assertTrue(template.getRegistry().hasTemplate(template.getId()));
		assertSame(template, template.getRegistry().getTemplate(template.getId()));

		M derived = createUnlocked(template.getRegistry(), mockManifestLocation(false));
		derived.setTemplateId(template.getId());

		assertSame(template, derived.getTemplate());

		return derived;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#getUID()}.
	 */
	@Test
	default void testGetUID() {
		assertTrue(createUnlocked().getUID()>0);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#isTemplate()}.
	 */
	@Test
	default void testIsTemplate() {
		assertFalse(createUnlocked().isTemplate());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#isValidTemplate()}.
	 */
	@Test
	default void testIsValidTemplate() {
		assertFalse(createUnlocked().isValidTemplate());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#hasTemplate()}.
	 */
	@Test
	default void testHasTemplate() {
		assertFalse(createUnlocked().hasTemplate());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#getTemplate()}.
	 */
	@Test
	default void testGetTemplate() {
		assertNull(createUnlocked().getTemplate());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#isEmpty()}.
	 */
	@Test
	default void testIsEmpty() {
		assertTrue(createUnlocked().isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#setId(java.lang.String)}.
	 *
	 * @see ManifestUtils#isValidId(String)
	 */
	@Test
	default void testSetId() {
		Manifest manifest = createUnlocked();

		ManifestTestUtils.assertMalformedId(manifest, "");
		ManifestTestUtils.assertMalformedId(manifest, "a");
		ManifestTestUtils.assertMalformedId(manifest, "aa");
		ManifestTestUtils.assertMalformedId(manifest, "123");
		ManifestTestUtils.assertMalformedId(manifest, "123abc");
		ManifestTestUtils.assertMalformedId(manifest, "%$§!()");
		ManifestTestUtils.assertMalformedId(manifest, "abc:def");
		ManifestTestUtils.assertMalformedId(manifest, "abc/def");
		ManifestTestUtils.assertMalformedId(manifest, "abc@def");
		ManifestTestUtils.assertMalformedId(manifest, "abc+def");
		ManifestTestUtils.assertMalformedId(manifest, "abc~def");
		ManifestTestUtils.assertMalformedId(manifest, "abc#def");
		ManifestTestUtils.assertMalformedId(manifest, "abc'def");
		ManifestTestUtils.assertMalformedId(manifest, "abc*def");
		ManifestTestUtils.assertMalformedId(manifest, "abc=def");
		ManifestTestUtils.assertMalformedId(manifest, "abc&def");
		ManifestTestUtils.assertMalformedId(manifest, "abc%def");
		ManifestTestUtils.assertMalformedId(manifest, "abc$def");
		ManifestTestUtils.assertMalformedId(manifest, "abc§def");
		ManifestTestUtils.assertMalformedId(manifest, "abc\"def");
		ManifestTestUtils.assertMalformedId(manifest, "abc!def");
		ManifestTestUtils.assertMalformedId(manifest, "abc{def");
		ManifestTestUtils.assertMalformedId(manifest, "abc}def");
		ManifestTestUtils.assertMalformedId(manifest, "abc[def");
		ManifestTestUtils.assertMalformedId(manifest, "abc]def");
		ManifestTestUtils.assertMalformedId(manifest, "abc(def");
		ManifestTestUtils.assertMalformedId(manifest, "abc)def");
		ManifestTestUtils.assertMalformedId(manifest, "abc\\def");
		ManifestTestUtils.assertMalformedId(manifest, "abc`def");
		ManifestTestUtils.assertMalformedId(manifest, "abc´def");
		ManifestTestUtils.assertMalformedId(manifest, "abc°def");
		ManifestTestUtils.assertMalformedId(manifest, "abc^def");
		ManifestTestUtils.assertMalformedId(manifest, "abc<def");
		ManifestTestUtils.assertMalformedId(manifest, "abc>def");
		ManifestTestUtils.assertMalformedId(manifest, "abc|def");
		ManifestTestUtils.assertMalformedId(manifest, "abc;def");
		ManifestTestUtils.assertMalformedId(manifest, "abc"+TestUtils.EMOJI+"def");

		ManifestTestUtils.assertValidId(manifest, "abc");
		ManifestTestUtils.assertValidId(manifest, "abc123");
		ManifestTestUtils.assertValidId(manifest, "abcdef");
		ManifestTestUtils.assertValidId(manifest, "abc-def");
		ManifestTestUtils.assertValidId(manifest, "abc_def");
		ManifestTestUtils.assertValidId(manifest, "abc.def");
		ManifestTestUtils.assertValidId(manifest, "abc-def123");
		ManifestTestUtils.assertValidId(manifest, "abc_def123");
		ManifestTestUtils.assertValidId(manifest, "abc.def123");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#setIsTemplate(boolean)}.
	 */
	@Test
	default void testSetIsTemplate() {
		Manifest manifest = createUnlocked(mock(ManifestRegistry.class), mockManifestLocation(true));

		manifest.setIsTemplate(true);
		assertTrue(manifest.isTemplate());

		manifest.lock();
		LockableTest.assertLocked(() -> manifest.setIsTemplate(true));

		Manifest liveContextManifest = createUnlocked();

		ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_ERROR,
				() -> liveContextManifest.setIsTemplate(true));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#setTemplateId(java.lang.String)}.
	 */
	@Test
	default void testSetTemplateId() {
		Manifest manifest = createUnlocked();

		String id = "test123";

		Manifest template = mock(Manifest.class);

		ManifestRegistry registry = manifest.getRegistry();
		when(registry.getTemplate(id)).thenReturn(template);

		manifest.setTemplateId(id);
		assertSame(template, manifest.getTemplate());

		manifest.setTemplateId(null);
		assertNull(manifest.getTemplate());

		manifest.lock();
		LockableTest.assertLocked(() -> manifest.setTemplateId(id));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#getRegistry()}.
	 */
	@Test
	default void testGetRegistry() {
		assertNotNull(createUnlocked().getRegistry());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#getManifestLocation()}.
	 */
	@Test
	default void testGetManifestLocation() {
		Manifest unlocked = createUnlocked();

		assertNotNull(unlocked.getManifestLocation());

		assertFalse(unlocked.getManifestLocation().isTemplate());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#getVersionManifest()}.
	 */
	@Test
	default void testGetVersionManifest() {
		assertNull(createUnlocked().getVersionManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Manifest#setVersionManifest(de.ims.icarus2.model.manifest.api.VersionManifest)}.
	 */
	@Test
	default void testSetVersionManifest() {
		Manifest manifest = createUnlocked();

		VersionManifest versionManifest = mock(VersionManifest.class);
		manifest.setVersionManifest(versionManifest);
		assertSame(versionManifest, manifest.getVersionManifest());

		TestUtils.assertNPE(() -> manifest.setVersionManifest(null));

		ManifestTestUtils.assertManifestException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE,
				() -> manifest.setVersionManifest(versionManifest));

		Manifest lockedManifest = createUnlocked();
		lockedManifest.lock();
		LockableTest.assertLocked(() -> lockedManifest.setVersionManifest(versionManifest));
	}

}
