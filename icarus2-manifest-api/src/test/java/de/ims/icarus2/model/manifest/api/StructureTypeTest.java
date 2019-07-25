/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.test.TestUtils.assertCollectionEquals;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import it.unimi.dsi.fastutil.bytes.ByteArraySet;
import it.unimi.dsi.fastutil.bytes.ByteSet;

/**
 * @author Markus Gärtner
 *
 */
class StructureTypeTest {

	class Operations {

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#getOperations()}.
		 */
		@Test
		void testGetOperations() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#supportsOperation(de.ims.icarus2.util.EditOperation)}.
		 */
		@Test
		void testSupportsOperation() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#getOutgoingEdgeLimit()}.
		 */
		@Test
		void testGetOutgoingEdgeLimit() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#isLegalOutgoingEdgeCount(int)}.
		 */
		@Test
		void testIsLegalOutgoingEdgeCount() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#getIncomingEdgeLimit()}.
		 */
		@Test
		void testGetIncomingEdgeLimit() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#isLegalIncomingEdgeCount(int)}.
		 */
		@Test
		void testIsLegalIncomingEdgeCount() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#getMinEdgeCount()}.
		 */
		@Test
		void testGetMinEdgeCount() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#isLegalEdgeCount(int)}.
		 */
		@Test
		void testIsLegalEdgeCount() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#isDirected()}.
		 */
		@Test
		void testIsDirected() {
			fail("Not yet implemented"); // TODO
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#getStringValue()}.
	 */
	@Test
	void testGetStringValue() {
		Set<String> usedValues = new HashSet<>();

		for(StructureType type : StructureType.values()) {
			String value = type.getStringValue();
			assertFalse(usedValues.contains(value), "duplicate string form: "+value);
			usedValues.add(value);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#isCompatibleWith(de.ims.icarus2.model.manifest.api.StructureType)}.
	 */
	@Test
	void testIsCompatibleWith() {
		assertTrue(StructureType.GRAPH.isCompatibleWith(StructureType.TREE));
		assertTrue(StructureType.GRAPH.isCompatibleWith(StructureType.CHAIN));
		assertTrue(StructureType.GRAPH.isCompatibleWith(StructureType.SET));
		assertTrue(StructureType.TREE.isCompatibleWith(StructureType.CHAIN));
		assertTrue(StructureType.TREE.isCompatibleWith(StructureType.SET));
		assertTrue(StructureType.CHAIN.isCompatibleWith(StructureType.SET));

		assertFalse(StructureType.SET.isCompatibleWith(StructureType.CHAIN));
		assertFalse(StructureType.SET.isCompatibleWith(StructureType.TREE));
		assertFalse(StructureType.SET.isCompatibleWith(StructureType.GRAPH));
		assertFalse(StructureType.CHAIN.isCompatibleWith(StructureType.TREE));
		assertFalse(StructureType.CHAIN.isCompatibleWith(StructureType.GRAPH));
		assertFalse(StructureType.TREE.isCompatibleWith(StructureType.GRAPH));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#getCompatibleTypes()}.
	 */
	@Test
	void testGetCompatibleTypes() {
		assertCollectionEquals(set(StructureType.GRAPH.getCompatibleTypes()),
				StructureType.TREE, StructureType.CHAIN, StructureType.SET);
		assertCollectionEquals(set(StructureType.TREE.getCompatibleTypes()),
				StructureType.CHAIN, StructureType.SET);
		assertCollectionEquals(set(StructureType.CHAIN.getCompatibleTypes()),
				StructureType.SET);
		assertArrayEquals(new Object[0], StructureType.SET.getCompatibleTypes());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#getIncompatibleTypes()}.
	 */
	@Test
	void testGetIncompatibleTypes() {
		assertCollectionEquals(set(StructureType.SET.getIncompatibleTypes()),
				StructureType.CHAIN, StructureType.TREE, StructureType.GRAPH);
		assertCollectionEquals(set(StructureType.CHAIN.getIncompatibleTypes()),
				StructureType.TREE, StructureType.GRAPH);
		assertCollectionEquals(set(StructureType.TREE.getIncompatibleTypes()),
				StructureType.GRAPH);
		assertArrayEquals(new Object[0], StructureType.GRAPH.getIncompatibleTypes());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#id()}.
	 */
	@Test
	void testId() {
		ByteSet usedIds = new ByteArraySet();
		for(StructureType type : StructureType.values()) {
			byte id = type.id();
			assertFalse(usedIds.contains(id), "duplicate id: "+id);
			usedIds.add(id);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#parseStructureType(java.lang.String)}.
	 */
	@Test
	void testParseStructureType() {
		for(StructureType type : StructureType.values()) {
			assertEquals(type, StructureType.parseStructureType(type.getStringValue()));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureType#forId(byte)}.
	 */
	@Test
	void testForId() {
		for(StructureType type : StructureType.values()) {
			assertEquals(type, StructureType.forId(type.id()));
		}
	}

}
