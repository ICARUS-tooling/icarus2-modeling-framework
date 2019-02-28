/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.structure;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockStructure;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertSetter;
import static de.ims.icarus2.test.TestUtils.randomLongPair;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvFileSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.EdgeTest;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.test.util.convert.ExtendedStringToPrimitiveConverter;

/**
 * @author Markus Gärtner
 *
 */
class DefaultEdgeTest implements EdgeTest<Edge> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends Edge> getTestTargetClass() {
		return DefaultEdge.class;
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#DefaultEdge()}.
		 */
		@Test
		void testDefaultEdge() {
			new DefaultEdge();
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#DefaultEdge(de.ims.icarus2.model.api.members.structure.Structure)}.
		 */
		@Test
		void testDefaultEdgeStructure() {
			Structure structure = mockStructure();
			DefaultEdge edge = new DefaultEdge(structure);
			assertSame(structure, edge.getStructure());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#DefaultEdge(de.ims.icarus2.model.api.members.structure.Structure, de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testDefaultEdgeStructureItemItem() {
			Structure structure = mockStructure();
			Item source = mockItem();
			Item target = mockItem();
			DefaultEdge edge = new DefaultEdge(structure, source, target);
			assertSame(structure, edge.getStructure());
			assertSame(source, edge.getSource());
			assertSame(target, edge.getTarget());
		}

	}

	@Nested
	class WithBareInstance {
		private DefaultEdge instance;


		@BeforeEach
		void setUp() {
			instance = new DefaultEdge();
		}


		@AfterEach
		void tearDown() {
			instance = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#setContainer(de.ims.icarus2.model.api.members.container.Container)}.
		 */
		@Test
		void testSetContainer() {
			assertSetter(instance,
					DefaultEdge::setContainer,
					mockStructure(), NPE_CHECK, NO_CHECK);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#setStructure(de.ims.icarus2.model.api.members.structure.Structure)}.
		 */
		@Test
		void testSetStructure() {
			assertSetter(instance,
					DefaultEdge::setStructure,
					mockStructure(), NPE_CHECK, NO_CHECK);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#getStructure()}.
		 */
		@Test
		void testGetStructure() {
			assertGetter(instance,
					mockStructure(), mockStructure(),
					NO_DEFAULT(),
					DefaultEdge::getStructure,
					DefaultEdge::setStructure);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#getContainer()}.
		 */
		@Test
		void testGetContainer() {
			assertGetter(instance,
					mockStructure(), mockStructure(),
					NO_DEFAULT(),
					DefaultEdge::getContainer,
					DefaultEdge::setContainer);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#getMemberType()}.
		 */
		@Test
		void testGetMemberType() {
			assertEquals(MemberType.EDGE, instance.getMemberType());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#getIndex()}.
		 */
		@Test
		void testGetIndex() {
			assertEquals(UNSET_LONG, instance.getIndex());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#toString()}.
		 */
		@Test
		void testToString() {
			assertNotNull(instance.toString());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#setId(long)}.
		 */
		@Test
		void testSetId() {
			assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					() -> instance.setId(1L));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#isLoop()}.
		 */
		@Test
		void testIsLoop() {
			// both null
			assertFalse(instance.isLoop());

			// only source non-null
			instance.setSource(mockItem());
			assertFalse(instance.isLoop());

			// both non-null, but no loop
			instance.setTarget(mockItem());
			assertFalse(instance.isLoop());

			// proper loop
			instance.setTarget(instance.getSource());
			assertTrue(instance.isLoop());

			// only target non-null
			instance.setSource(null);
			assertFalse(instance.isLoop());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#setLocked(boolean)}.
		 */
		@Test
		void testSetLocked() {
			assertFalse(instance.isLocked());

			instance.setLocked(true);
			assertFalse(instance.isLocked());

			instance.setLocked(false);
			assertFalse(instance.isLocked());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#isLocked()}.
		 */
		@Test
		void testIsLocked() {
			assertFalse(instance.isLocked());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#getSource()}.
		 */
		@Test
		void testGetSource() {
			assertGetter(instance,
					mockItem(), mockItem(),
					NO_DEFAULT(),
					DefaultEdge::getSource,
					DefaultEdge::setSource);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#getTarget()}.
		 */
		@Test
		void testGetTarget() {
			assertGetter(instance,
					mockItem(), mockItem(),
					NO_DEFAULT(),
					DefaultEdge::getTarget,
					DefaultEdge::setTarget);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#setSource(de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testSetSource() {
			assertSetter(instance,
					DefaultEdge::setSource,
					mockItem(),
					NO_NPE_CHECK, NO_CHECK);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#setTarget(de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testSetTarget() {
			assertSetter(instance,
					DefaultEdge::setTarget,
					mockItem(),
					NO_NPE_CHECK, NO_CHECK);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#getTerminal(boolean)}.
		 */
		@Test
		void testGetTerminal() {
			assertNull(instance.getTerminal(true));
			assertNull(instance.getTerminal(false));

			Item source = mockItem();
			Item target = mockItem();

			instance.setSource(source);
			assertSame(source, instance.getTerminal(true));
			assertNull(instance.getTerminal(false));

			instance.setTarget(target);
			assertSame(target, instance.getTerminal(false));

			// consistency check
			assertSame(instance.getSource(), instance.getTerminal(true));
			assertSame(instance.getTarget(), instance.getTerminal(false));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#setTerminal(de.ims.icarus2.model.api.members.item.Item, boolean)}.
		 */
		@Test
		void testSetTerminal() {
			Item source = mockItem();
			Item target = mockItem();

			instance.setTerminal(source, true);
			instance.setTerminal(target, false);
			assertSame(source, instance.getSource());
			assertSame(target, instance.getTarget());

			instance.setTerminal(null, true);
			assertNull(instance.getSource());

			instance.setTerminal(null, false);
			assertNull(instance.getTarget());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#isDirty()}.
		 */
		@Test
		void testIsDirty() {
			assertTrue(instance.isDirty());

			instance.setSource(mockItem());
			instance.setTarget(mockItem());
			assertFalse(instance.isDirty());

			instance.setDirty(true);
			assertTrue(instance.isDirty());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#isAlive()}.
		 */
		@Test
		void testIsAlive() {
			assertFalse(instance.isAlive());

			instance.setSource(mockItem());
			assertFalse(instance.isAlive());

			instance.setTarget(mockItem());
			assertFalse(instance.isAlive());

			instance.setAlive(true);
			assertTrue(instance.isAlive());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#recycle()}.
		 */
		@Test
		void testRecycle() {
			instance.setStructure(mockStructure());
			instance.setSource(mockItem());
			instance.setTarget(mockItem());

			instance.recycle();

			assertNull(instance.getStructure());
			assertNull(instance.getSource());
			assertNull(instance.getTarget());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#revive()}.
		 */
		@Test
		void testRevive() {
			assertFalse(instance.revive());

			instance.setStructure(mockStructure());
			assertFalse(instance.revive());

			instance.setSource(mockItem());
			assertFalse(instance.revive());

			instance.setTarget(mockItem());
			assertTrue(instance.revive());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#getBeginOffset()}.
		 */
		@Test
		void testGetBeginOffset() {
			assertEquals(UNSET_LONG, instance.getBeginOffset());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#getEndOffset()}.
		 */
		@Test
		void testGetEndOffset() {
			assertEquals(UNSET_LONG, instance.getEndOffset());
		}
	}

	/**
	 * Needs underlying items with stubbed offsets.
	 * @author Markus Gärtner
	 *
	 */
	@Nested
	class WithComplexEnvironment {

		private Item source, target;

		private DefaultEdge instance;

		@BeforeEach
		void setUp() {

			source = mockItem();
			target = mockItem();

			instance = new DefaultEdge();
		}

		@AfterEach
		void tearDown() {
			source = null;
			target = null;
			instance = null;
		}

		@SuppressWarnings("boxing")
		private void stubBegin(long sourceBegin, long targetBegin) {
			when(source.getBeginOffset()).thenReturn(sourceBegin);
			when(target.getBeginOffset()).thenReturn(targetBegin);
		}

		@SuppressWarnings("boxing")
		private void stubEnd(long sourceEnd, long targetEnd) {
			when(source.getEndOffset()).thenReturn(sourceEnd);
			when(target.getEndOffset()).thenReturn(targetEnd);
		}

		Stream<Pair<Long, Long>> randomPairs() {
			return TestUtils.randomPairs(UNSET_LONG, Long.MAX_VALUE);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#getBeginOffset()}.
		 */
		@ParameterizedTest
		@CsvFileSource(resources= {"fixedBeginOffsets.csv"})
		void testGetBeginOffsetFixedValues(
				@ConvertWith(ExtendedStringToPrimitiveConverter.class) long sourceBegin,
				@ConvertWith(ExtendedStringToPrimitiveConverter.class) long targetBegin,
				@ConvertWith(ExtendedStringToPrimitiveConverter.class) long expectedBegin) {
			assertEquals(UNSET_LONG, instance.getBeginOffset());

			instance.setSource(source);
			instance.setTarget(target);

			stubBegin(sourceBegin, targetBegin);

			assertEquals(expectedBegin, instance.getBeginOffset());

			verify(source).getBeginOffset();
			verify(target).getBeginOffset();
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#getEndOffset()}.
		 */
		@ParameterizedTest
		@CsvFileSource(resources= {"fixedEndOffsets.csv"})
		void testGetEndOffsetFixedValues(
				@ConvertWith(ExtendedStringToPrimitiveConverter.class) long sourceEnd,
				@ConvertWith(ExtendedStringToPrimitiveConverter.class) long targetEnd,
				@ConvertWith(ExtendedStringToPrimitiveConverter.class) long expectedEnd) {
			assertEquals(UNSET_LONG, instance.getEndOffset());

			instance.setSource(source);
			instance.setTarget(target);

			stubEnd(sourceEnd, targetEnd);

			assertEquals(expectedEnd, instance.getEndOffset());

			verify(source).getEndOffset();
			verify(target).getEndOffset();
		}

		@Nested
		class WithPresetTerminals {

			@BeforeEach
			void setUp() {
				instance.setSource(source);
				instance.setTarget(target);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#getBeginOffset()}.
			 */
			@SuppressWarnings("boxing")
			@TestFactory
			Stream<DynamicTest> testGetBeginOffsetRandomValues() {
				return Stream.generate(() -> randomLongPair(UNSET_LONG, Long.MAX_VALUE))
						.limit(RUNS)
						.map(p -> dynamicTest(p.toString(), () -> {
							stubBegin(p.first, p.second);
							assertEquals(Math.min(p.first, p.second), instance.getBeginOffset());
						}));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.structure.DefaultEdge#getEndOffset()}.
			 */
			@SuppressWarnings("boxing")
			@TestFactory
			Stream<DynamicTest> testGetEndOffsetRandomValues() {
				return Stream.generate(() -> randomLongPair(UNSET_LONG, Long.MAX_VALUE))
						.limit(RUNS)
						.map(p -> dynamicTest(p.toString(), () -> {
							stubEnd(p.first, p.second);
							assertEquals(Math.max(p.first, p.second), instance.getEndOffset());
						}));
			}
		}
	}

}
