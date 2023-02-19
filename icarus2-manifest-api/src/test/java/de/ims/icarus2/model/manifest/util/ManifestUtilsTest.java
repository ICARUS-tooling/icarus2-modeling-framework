/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.util;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.getIllegalIdValues;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getLegalIdValues;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubIsTemplate;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.api.Embedded;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.standard.AbstractMemberManifest;

/**
 * @author Markus Gärtner
 *
 */
class ManifestUtilsTest {

	class Checks {

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#checkId(java.lang.String)}.
		 */
		@Test
		void testCheckId() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#checkIdNotNull(java.lang.String)}.
		 */
		@Test
		void testCheckIdNotNull() {
			fail("Not yet implemented"); // TODO
		}
	}

	class Extraction {

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#getUniqueId(de.ims.icarus2.model.manifest.api.ManifestFragment)}.
		 */
		@Test
		void testGetUniqueId() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#extractHostId(java.lang.String)}.
		 */
		@Test
		void testExtractHostId() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#extractElementId(java.lang.String)}.
		 */
		@Test
		void testExtractElementId() {
			fail("Not yet implemented"); // TODO
		}

	}

	class Requirements {

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#require(java.util.Optional, de.ims.icarus2.model.manifest.api.ManifestFragment, java.lang.String)}.
		 */
		@Test
		void testRequireOptionalOfTMString() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#require(de.ims.icarus2.model.manifest.api.ManifestFragment, java.util.function.Function, java.lang.String)}.
		 */
		@Test
		void testRequireMFunctionOfMOptionalOfTString() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#require(java.util.Optional, java.util.function.Function, de.ims.icarus2.model.manifest.api.ManifestFragment, java.lang.String, java.lang.String)}.
		 */
		@Test
		void testRequireOptionalOfTFunctionOfTOptionalOfUMStringString() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#requireHost(de.ims.icarus2.model.manifest.api.Embedded)}.
		 */
		@Test
		void testRequireHost() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#requireGrandHost(de.ims.icarus2.model.manifest.api.Embedded)}.
		 */
		@Test
		void testRequireGrandHost() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#requireId(de.ims.icarus2.model.manifest.api.ManifestFragment)}.
		 */
		@Test
		void testRequireId() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#requireName(de.ims.icarus2.model.manifest.api.MemberManifest)}.
		 */
		@Test
		void testRequireName() {
			fail("Not yet implemented"); // TODO
		}

	}

	@Nested
	class Misc {

		private String limit(String s) {
			if(s.length()>10) {
				s = s.substring(0, 10);
			}
			return s;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#isValidId(java.lang.String)}.
		 */
		@TestFactory
		Stream<DynamicTest> testIsValidId() {
			return Stream.concat(
					Stream.of(getLegalIdValues())
						.map(id -> dynamicTest("valid: "+limit(id),
							() -> assertTrue(ManifestUtils.isValidId(id)))),
					Stream.of(getIllegalIdValues())
						.map(id -> dynamicTest("invalid: "+limit(id),
							() -> assertFalse(ManifestUtils.isValidId(id)))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#getName(java.lang.Object)}.
		 */
		@Test
		void testGetName() {
			assertNotNull(mock(Object.class));
			//TODO more cases
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#isItemLayerManifest(de.ims.icarus2.model.manifest.api.Manifest)}.
		 */
		@SuppressWarnings("boxing")
		@Test
		void testIsItemLayerManifest() {
			for(ManifestType type : ManifestType.values()) {
				Manifest manifest = mock(Manifest.class);
				when(manifest.getManifestType()).thenReturn(type);
				assertEquals(type==ManifestType.ITEM_LAYER_MANIFEST,
						ManifestUtils.isItemLayerManifest(manifest));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#isAnyItemLayerManifest(de.ims.icarus2.model.manifest.api.Manifest)}.
		 */
		@SuppressWarnings("boxing")
		@Test
		void testIsAnyItemLayerManifest() {
			for(ManifestType type : ManifestType.values()) {
				Manifest manifest = mock(Manifest.class);
				when(manifest.getManifestType()).thenReturn(type);
				assertEquals(type==ManifestType.ITEM_LAYER_MANIFEST
						|| type==ManifestType.STRUCTURE_LAYER_MANIFEST
						|| type==ManifestType.FRAGMENT_LAYER_MANIFEST,
						ManifestUtils.isAnyItemLayerManifest(manifest));
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#isAnnotationLayerManifest(de.ims.icarus2.model.manifest.api.Manifest)}.
		 */
		@SuppressWarnings("boxing")
		@Test
		void testIsAnnotationLayerManifest() {
			for(ManifestType type : ManifestType.values()) {
				Manifest manifest = mock(Manifest.class);
				when(manifest.getManifestType()).thenReturn(type);
				assertEquals(type==ManifestType.ANNOTATION_LAYER_MANIFEST,
						ManifestUtils.isAnnotationLayerManifest(manifest));
			}
		}

		/**
		 * Test method for {@link ManifestUtils#getHostOfType(de.ims.icarus2.model.manifest.api.TypedManifest, ManifestType)).
		 */
		@Test
		void testGetHostOfType() {
			ManifestType[] types = ManifestType.values();
			Manifest[] manifests = new Manifest[types.length];

			/*
			 * Test setup:
			 * We create a chain of embedded manifests and then look
			 * them up via types.
			 */

			for (int i = 0; i < types.length; i++) {
				manifests[i] = mock(AbstractMemberManifest.class, CALLS_REAL_METHODS);
				when(manifests[i].getManifestType()).thenReturn(types[i]);
				if(i>0) {
					Embedded embedded = (Embedded)manifests[i-1];
					when(embedded.getHost()).thenReturn(Optional.of(manifests[i]));
				}
			}

			AbstractMemberManifest<?, ?> start = mock(AbstractMemberManifest.class, CALLS_REAL_METHODS);
			when(start.getHost()).thenReturn(Optional.of(manifests[0]));

			for (int i = 0; i < types.length; i++) {
				assertSame(manifests[i], ManifestUtils.getHostOfType(start, types[i]));
			}
		}

		/**
		 * Test method for {@link ManifestUtils#getHostOfType(de.ims.icarus2.model.manifest.api.TypedManifest, ManifestType)).
		 */
		@Test
		void testGetHostOfTypeNullArguments() {
			assertNPE(() -> ManifestUtils.getHostOfType(null, ManifestType.ANNOTATION_LAYER_MANIFEST));
			assertNPE(() -> ManifestUtils.getHostOfType(mock(Manifest.class), null));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.util.ManifestUtils#hasTemplateContext(de.ims.icarus2.model.manifest.api.TypedManifest)}.
		 */
		@Test
		void testHasTemplateContext() {
			Manifest m = mock(Manifest.class);
			assertFalse(ManifestUtils.hasTemplateContext(m));

			stubIsTemplate(m);
			assertTrue(ManifestUtils.hasTemplateContext(m));

			ManifestType[] types = {
				ManifestType.ITEM_LAYER_MANIFEST,
				ManifestType.CONTEXT_MANIFEST,
			};

			for (ManifestType type : types) {
				Manifest cm = mockTypedManifest(ManifestType.CONTAINER_MANIFEST, true);
				assertFalse(ManifestUtils.hasTemplateContext(cm));
				stubIsTemplate(ManifestUtils.getHostOfType(cm, type));
				assertTrue(ManifestUtils.hasTemplateContext(cm));
			}
		}
	}

}
