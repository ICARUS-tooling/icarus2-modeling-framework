/**
 *
 */
package de.ims.icarus2.model.manifest;

import de.ims.icarus2.model.manifest.api.Lockable;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.test.TestFeature;

/**
 * @author Markus Gärtner
 *
 */
public enum ManifestTestFeature implements TestFeature {
	/**
	 * Hint that a {@link Lockable#isLocked() unlocked} test instance is needed.
	 */
	UNLOCKED,

	/**
	 * Hint that a {@link Lockable#isLocked() locked} test instance is needed.
	 */
	LOCKED,

	/**
	 * Hint that a {@link Manifest#isTemplate() template} instance is needed.
	 */
	TEMPLATE,

	/**
	 * Hint that the created test instance should be properly embedded in a
	 * mocked environment according to its own requirements.
	 */
	EMBEDDED,

	/**
	 * Hint that the created test instance is expected to be derived from
	 * another template manifest.
	 */
	DERIVED,

	/**
	 * Forces templates to also feature a host environment.
	 */
	EMBED_TEMPLATE,
	;
}
