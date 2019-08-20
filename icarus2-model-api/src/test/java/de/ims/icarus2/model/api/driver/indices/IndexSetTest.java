/**
 *
 */
package de.ims.icarus2.model.api.driver.indices;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.assertCollectionEquals;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomLongs;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.driver.indices.IndexSet.Feature;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.guard.ApiGuard;
import de.ims.icarus2.util.collections.ArrayUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface IndexSetTest<S extends IndexSet> extends ApiGuardedTest<S> {

	/**
	 * @see de.ims.icarus2.test.ApiGuardedTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<S> apiGuard) {
		ApiGuardedTest.super.configureApiGuard(apiGuard);
		apiGuard.nullGuard(true);
	}

	Stream<Config> configurations();

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#size()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testSize() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					if(config.features.contains(Feature.INDETERMINATE_SIZE)) {
						assertEquals(UNSET_LONG, config.set.size());
					} else {
						assertEquals(config.indices.length, config.set.size());
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#isEmpty()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsEmpty() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					if(config.indices.length==0) {
						assertTrue(config.set.isEmpty(), "Expecting to be empty");
					} else if(!config.features.contains(Feature.INDETERMINATE_SIZE)) {
						assertFalse(config.set.isEmpty(), "Expected to not be empty");
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#getIndexValueType()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetIndexValueType() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					assertEquals(config.valueType, config.set.getIndexValueType());
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#isSorted()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsSorted() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					if(config.sorted) {
						assertTrue(config.set.isSorted(), "Expected to be sorted");
					} else {
						assertFalse(config.set.isSorted(), "Expected not to be sorted");
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#sort()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testSort() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					if(config.features.contains(Feature.SORTABLE)
							&& !config.sorted) {
						assertTrue(config.set.sort());
					}
				}));
	}

//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(byte[], int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testExportByteArrayInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int, int, byte[], int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testExportIntIntByteArrayInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(short[], int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testExportShortArrayInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int, int, short[], int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testExportIntIntShortArrayInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int[], int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testExportIntArrayInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int, int, int[], int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testExportIntIntIntArrayInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(long[], int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testExportLongArrayInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#export(int, int, long[], int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testExportIntIntLongArrayInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}

//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachIndex(java.util.function.LongConsumer)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testForEachIndexLongConsumer() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachIndex(java.util.function.LongConsumer, int, int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testForEachIndexLongConsumerIntInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachIndex(java.util.function.IntConsumer)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testForEachIndexIntConsumer() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachIndex(java.util.function.IntConsumer, int, int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testForEachIndexIntConsumerIntInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachEntry(java.util.function.LongBinaryOperator)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testForEachEntryLongBinaryOperator() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachEntry(java.util.function.LongBinaryOperator, int, int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testForEachEntryLongBinaryOperatorIntInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachEntry(java.util.function.IntBinaryOperator)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testForEachEntryIntBinaryOperator() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#forEachEntry(java.util.function.IntBinaryOperator, int, int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testForEachEntryIntBinaryOperatorIntInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}

//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkIndices(java.util.function.LongPredicate)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testCheckIndicesLongPredicate() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkIndices(java.util.function.LongPredicate, int, int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testCheckIndicesLongPredicateIntInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkIndices(java.util.function.IntPredicate)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testCheckIndicesIntPredicate() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkIndices(java.util.function.IntPredicate, int, int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testCheckIndicesIntPredicateIntInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkConsecutiveIndices(de.ims.icarus2.util.function.LongBiPredicate)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testCheckConsecutiveIndicesLongBiPredicate() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkConsecutiveIndices(de.ims.icarus2.util.function.LongBiPredicate, int, int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testCheckConsecutiveIndicesLongBiPredicateIntInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#split(int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testSplit() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#subSet(int, int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testSubSet() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#externalize()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testExternalize() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					IndexSet set = config.set.externalize();
					assertNotNull(set);
					assertFalse(set.hasFeature(Feature.INDETERMINATE_SIZE));
					assertEquals(config.indices.length, set.size());
					for (int i = 0; i < config.indices.length; i++) {
						assertEquals(config.indices[i], set.indexAt(i));
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#getFeatures()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetFeatures() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					assertCollectionEquals(config.features, config.set.getFeatures());
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#hasFeatures(de.ims.icarus2.model.api.driver.indices.IndexSet.Feature[])}.
	 */
	@TestFactory
	default Stream<DynamicTest> testHasFeatures() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					assertTrue(config.set.hasFeatures(config.features.toArray(new Feature[0])));

					Set<Feature> negative = EnumSet.allOf(Feature.class);
					negative.removeAll(config.features);

					assertFalse(config.set.hasFeatures(negative.toArray(new Feature[0])));
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#hasFeature(de.ims.icarus2.model.api.driver.indices.IndexSet.Feature)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testHasFeature() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					for(Feature feature : Feature.values()) {
						if(config.features.contains(feature)) {
							assertTrue(config.set.hasFeature(feature), "Expected to have feature: "+feature);
						} else {
							assertFalse(config.set.hasFeature(feature), "Not expected to have feature: "+feature);
						}
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkHasFeatures(de.ims.icarus2.model.api.driver.indices.IndexSet.Feature[])}.
	 */
	@TestFactory
	default Stream<DynamicTest> testCheckHasFeatures() {
		return configurations()
				.map(Config::validate)
				.map(config -> dynamicTest(config.label, () -> {
					config.set.checkHasFeatures(config.features.toArray(new Feature[0]));

					Set<Feature> negative = EnumSet.allOf(Feature.class);
					negative.removeAll(config.features);

					assertModelException(GlobalErrorCode.NOT_IMPLEMENTED,
							() -> config.set.checkHasFeatures(negative.toArray(new Feature[0])));
				}));
	}

//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#iterator()}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testIterator() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#iterator(int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testIteratorInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#iterator(int, int)}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testIteratorIntInt() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#intStream()}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testIntStream() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}
//
//	/**
//	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#longStream()}.
//	 */
//	@TestFactory
//	default Stream<DynamicTest> testLongStream() {
//		fail("Not yet implemented"); // TODO
//		return configurations()
//				.map(Config::validate)
//				.map(config -> dynamicTest(config.label, () -> {
//
//				}));
//	}

	static final class Config implements Cloneable {
		String label;
		IndexSet set;
		long[] indices;
		Set<Feature> features = EnumSet.noneOf(Feature.class);
		boolean sorted;
		IndexValueType valueType;

		public Config label(String label) { this.label = label; return this; }
		public Config set(IndexSet set) { this.set = set; return this; }
		public Config set(Function<Config, IndexSet> constructor) {
			assertNotNull(valueType, "Value type missing");
			assertNotNull(indices, "Indices missing");
			this.set = constructor.apply(this);
			return this;
		}
		public Config randomIndices(int size) {
			assertNotNull(valueType, "Value type missing");
			indices = randomLongs(size, 0, valueType.maxValue());
			indices[random(0, indices.length)] = valueType.maxValue();
			return this;
		}
		public Config sortedIndices(int size) {
			assertNotNull(valueType, "Value type missing");
			assertTrue(size<=valueType.maxValue());
			indices = new long[size];
			ArrayUtils.fillAscending(indices);
			indices[indices.length-1] = valueType.maxValue();
			return this;
		}
		public Config indices(long...indices) { this.indices = indices; return this; }
		public Config indices(int...indices) {
			this.indices = new long[indices.length];
			for (int i = 0; i < indices.length; i++) this.indices[i] = indices[i];
			return this; }
		public Config indices(short...indices) {
			this.indices = new long[indices.length];
			for (int i = 0; i < indices.length; i++) this.indices[i] = indices[i];
			return this; }
		public Config indices(byte...indices) {
			this.indices = new long[indices.length];
			for (int i = 0; i < indices.length; i++) this.indices[i] = indices[i];
			return this; }
		public Config features(Set<Feature> features) { this.features.addAll(features); return this; }
		public Config features(Feature...features) { Collections.addAll(this.features, features); return this; }
		public Config sorted(boolean sorted) { this.sorted = sorted; return this; }
		public Config valueType(IndexValueType valueType) { this.valueType = valueType; return this; }

		@Override
		public Config clone() {
			Config clone;
			try {
				clone = (Config) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new InternalError(e);
			}

			clone.features = EnumSet.copyOf(clone.features);
			clone.indices = indices==null ? null : indices.clone();

			return clone;
		}

		public Config validate() {
			//TODO check states
			assertNotNull(label, "Label missing");
			assertNotNull(set, "IndexSet missing");
			assertNotNull(indices, "Indices missing");
			assertNotNull(features, "Features missing");
			assertNotNull(valueType, "Value type missing");
			return this;
		}

		public String getLabel() { return label; }
		public boolean isSorted() { return sorted; }
		public IndexValueType getValueType() { return valueType; }
		public long[] getIndices() { return indices; }
	}
}
