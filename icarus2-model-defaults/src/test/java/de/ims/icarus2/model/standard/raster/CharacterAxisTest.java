/**
 *
 */
package de.ims.icarus2.model.standard.raster;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.test.TestUtils.MAX_INTEGER_INDEX;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.raster.RasterAxisTest;
import de.ims.icarus2.test.Dummy;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class CharacterAxisTest implements RasterAxisTest<CharacterAxis> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends CharacterAxis> getTestTargetClass() {
		return CharacterAxis.class;
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public CharacterAxis createTestInstance(TestSettings settings) {
		return new CharacterAxis();
	}

	@Nested
	class WithEnvironment {
		CharacterAxis instance;
		Item item;
		FragmentLayer layer;

		@BeforeEach
		void setUp() {
			instance = create();
			item = mockItem();
			layer = mock(FragmentLayer.class);
		}

		@AfterEach
		void tearDown() {
			instance = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.raster.CharacterAxis#getRasterSize(de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.layer.FragmentLayer, java.lang.Object)}.
		 */
		@Test
		void testGetRasterSizeNull() {
			assertEquals(0, instance.getRasterSize(item, layer, null));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.raster.CharacterAxis#getRasterSize(de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.layer.FragmentLayer, java.lang.Object)}.
		 */
		@SuppressWarnings("boxing")
		@TestFactory
		Stream<DynamicTest> testGetRasterSize_ClassCast() {
			return Stream.of(new Object(), new Dummy(),
					GlobalErrorCode.ILLEGAL_STATE, 1, Boolean.TRUE)
				.map(value -> dynamicTest(value.getClass().getName(),
						() -> assertThrows(ClassCastException.class,
								() -> instance.getRasterSize(item, layer, value))));
		}

		@SuppressWarnings("boxing")
		private CharSequence mockString(int length) {
			CharSequence sequence = mock(CharSequence.class);
			when(sequence.length()).thenReturn(length);
			return sequence;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.raster.CharacterAxis#getRasterSize(de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.layer.FragmentLayer, java.lang.Object)}.
		 */
		@TestFactory
		Stream<DynamicTest> testGetRasterSize() {
			return Stream.concat(Stream.of("", "1"),
					random().ints(RUNS, 2, MAX_INTEGER_INDEX)
						.mapToObj(length -> mockString(length)))
					.map(value -> dynamicTest(String.valueOf(value.length()),
							() -> assertEquals(value.length(),
									instance.getRasterSize(item, layer, value))));
		}
	}
}
