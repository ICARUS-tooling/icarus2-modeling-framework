/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.LocationManifest.PathEntry;

/**
 * @author Markus Gärtner
 *
 */
public interface LocationManifestTest<M extends LocationManifest> {

	public static PathEntry mockEntry() {

	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest#isInline()}.
	 */
	@Test
	default void testIsInline() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest#getInlineData()}.
	 */
	@Test
	default void testGetInlineData() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest#getRootPath()}.
	 */
	@Test
	default void testGetRootPath() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest#getRootPathType()}.
	 */
	@Test
	default void testGetRootPathType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest#getPathResolverManifest()}.
	 */
	@Test
	default void testGetPathResolverManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest#forEachPathEntry(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachPathEntry() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest#getPathEntries()}.
	 */
	@Test
	default void testGetPathEntries() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest#setIsInline(boolean)}.
	 */
	@Test
	default void testSetIsInline() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest#setInlineData(java.lang.CharSequence)}.
	 */
	@Test
	default void testSetInlineData() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest#setRootPath(java.lang.String)}.
	 */
	@Test
	default void testSetRootPath() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest#setRootPathType(de.ims.icarus2.model.manifest.api.LocationManifest.PathType)}.
	 */
	@Test
	default void testSetRootPathType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest#setPathResolverManifest(de.ims.icarus2.model.manifest.api.PathResolverManifest)}.
	 */
	@Test
	default void testSetPathResolverManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest#addPathEntry(de.ims.icarus2.model.manifest.api.LocationManifest.PathEntry)}.
	 */
	@Test
	default void testAddPathEntry() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest#removePathEntry(de.ims.icarus2.model.manifest.api.LocationManifest.PathEntry)}.
	 */
	@Test
	default void testRemovePathEntry() {
		fail("Not yet implemented");
	}

}
