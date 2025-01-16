/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.model.manifest;

import de.ims.icarus2.model.manifest.api.Lockable;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestFragment;
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

	/**
	 * Hint that the created test instance should be valid fully for live use,
	 * i.e. with a valid {@link ManifestFragment#getId() id} and other implementation
	 * specific features.
	 */
	LIVE,

	/**
	 * Directive to force batch tests to be run with a new instance of the class
	 * under test for every value in the batch.
	 */
	FORK,

	;
}
