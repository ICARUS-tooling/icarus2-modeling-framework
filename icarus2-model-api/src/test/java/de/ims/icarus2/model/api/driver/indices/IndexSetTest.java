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
package de.ims.icarus2.model.api.driver.indices;

import static de.ims.icarus2.model.api.ModelAssertions.assertThat;
import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.driver.indices.IndexSet.Feature;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.collections.ArrayUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface IndexSetTest<S extends IndexSet> extends ApiGuardedTest<S> {

	Stream<Config> configurations();

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#size()}.
	 */
	@TestFactory
	@RandomizedTest
	default Stream<DynamicTest> testSize() {
		return configurations()
				.map(Config::validate)
				.flatMap(Config::withSubSets)
				.map(config -> dynamicTest(config.label, () -> {
					if(config.features.contains(Feature.INDETERMINATE_SIZE)) {
						assertThat(config.set).hasUndefinedSize();
					} else {
						assertThat(config.set).hasSize(config.indices.length);
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#isEmpty()}.
	 */
	@TestFactory
	@RandomizedTest
	default Stream<DynamicTest> testIsEmpty() {
		return configurations()
				.map(Config::validate)
				.flatMap(Config::withSubSets)
				.map(config -> dynamicTest(config.label, () -> {
					if(config.indices.length==0) {
						assertThat(config.set).isEmpty();
					} else if(!config.features.contains(Feature.INDETERMINATE_SIZE)) {
						assertThat(config.set).isNotEmpty();
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#getIndexValueType()}.
	 */
	@TestFactory
	@RandomizedTest
	default Stream<DynamicTest> testGetIndexValueType() {
		return configurations()
				.map(Config::validate)
				.flatMap(Config::withSubSets)
				.map(config -> dynamicTest(config.label, () -> {
					assertThat(config.set).hasValueType(config.set.getIndexValueType());
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#isSorted()}.
	 */
	@TestFactory
	@RandomizedTest
	default Stream<DynamicTest> testIsSorted() {
		return configurations()
				.map(Config::validate)
				.flatMap(Config::withSubSets)
				.map(config -> dynamicTest(config.label, () -> {
					if(config.sorted) {
						assertThat(config.set).isSorted();
					} else {
						assertThat(config.set).isNotSorted();
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#sort()}.
	 */
	@TestFactory
	@RandomizedTest
	default Stream<DynamicTest> testSort() {
		return configurations()
				.map(Config::validate)
				.flatMap(Config::withSubSets)
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
	@RandomizedTest
	default Stream<DynamicTest> testExternalize() {
		return configurations()
				.map(Config::validate)
				.flatMap(Config::withSubSets)
				.map(config -> dynamicTest(config.label, () -> {
					if(config.features.contains(Feature.INDETERMINATE_SIZE)) {
						assertModelException(GlobalErrorCode.NOT_IMPLEMENTED,
								() -> config.set.externalize());
					} else if(config.indices.length==0) {
						assertTrue(config.set.externalize().isEmpty());
					} else {
						IndexSet set = config.set.externalize();
						assertThat(set)
							.hasNotFeature(Feature.INDETERMINATE_SIZE)
							.hasSameIndicesAs(config.set);
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#getFeatures()}.
	 */
	@TestFactory
	@RandomizedTest
	default Stream<DynamicTest> testGetFeatures() {
		return configurations()
				.map(Config::validate)
				.flatMap(Config::withSubSets)
				.map(config -> dynamicTest(config.label, () -> {
					assertThat(config.set).hasExacltyFeatures(config.features);
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#hasFeatures(de.ims.icarus2.model.api.driver.indices.IndexSet.Feature[])}.
	 */
	@TestFactory
	@RandomizedTest
	default Stream<DynamicTest> testHasFeatures() {
		return configurations()
				.map(Config::validate)
				.flatMap(Config::withSubSets)
				.map(config -> dynamicTest(config.label, () -> {
					assertThat(config.set).hasAllFeatures(config.features);

					Set<Feature> negative = EnumSet.allOf(Feature.class);
					negative.removeAll(config.features);

					assertThat(config.set).hasNoFeatures(negative);
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#hasFeature(de.ims.icarus2.model.api.driver.indices.IndexSet.Feature)}.
	 */
	@TestFactory
	@RandomizedTest
	default Stream<DynamicTest> testHasFeature() {
		return configurations()
				.map(Config::validate)
				.flatMap(Config::withSubSets)
				.map(config -> dynamicTest(config.label, () -> {
					for(Feature feature : Feature.values()) {
						if(config.features.contains(feature)) {
							assertThat(config.set).hasFeature(feature);
						} else {
							assertThat(config.set).hasNotFeature(feature);
						}
					}
				}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.indices.IndexSet#checkHasFeatures(de.ims.icarus2.model.api.driver.indices.IndexSet.Feature[])}.
	 */
	@TestFactory
	@RandomizedTest
	default Stream<DynamicTest> testCheckHasFeatures() {
		return configurations()
				.map(Config::validate)
				.flatMap(Config::withSubSets)
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

	public static long[] randomIndices(RandomGenerator rand, IndexValueType valueType, int size) {
		assertTrue(size<=valueType.maxValue());
		IndexValueType smaller = valueType.smaller();
		long min = smaller==null ? 0L : smaller.maxValue()+1;
		return rand.longs(min, valueType.maxValue())
				.distinct()
				.limit(size)
				.toArray();
	}

	public static long[] sortedIndices(IndexValueType valueType, int size, int offset) {
		assertTrue(size<=valueType.maxValue());
		IndexValueType smaller = valueType.smaller();
		long min = smaller==null ? 0L : smaller.maxValue()+1;
		assertTrue(min+offset+size<=valueType.maxValue(), "Value spac einsufficient: "+valueType.maxValue());
		long[] indices = new long[size];
		ArrayUtils.fillAscending(indices, min+offset);
		return indices;
	}

	static final class Config implements Cloneable {
		String label;
		IndexSet set;
		long[] indices;
		Set<Feature> features = EnumSet.noneOf(Feature.class);
		boolean sorted;
		IndexValueType valueType;
		RandomGenerator rand;
		boolean autoDetectSorted;

		public RandomGenerator rand() {
			assertNotNull(rand, "No random generator defined");
			return rand;
		}

		public Config rand(RandomGenerator rand) {
			this.rand = requireNonNull(rand);
			return this;
		}

		private Function<Config, IndexSet> constructor;

		public Config label(String label) { this.label = label; return this; }
		public Config set(IndexSet set) { this.set = set; return this; }
		public Config set(Function<Config, IndexSet> constructor) {
			this.constructor = requireNonNull(constructor);
			return this;
		}
		/** Produce random indices outside the next smaller type's value space */
		public Config randomIndices(int size) {
			assertNotNull(valueType, "Value type missing");
			indices = IndexSetTest.randomIndices(rand(), valueType, size);
			indices[rand().random(0, indices.length)] = valueType.maxValue();
			return this;
		}
		/** Create sorted indices outside the next smaller type's value space */
		public Config sortedIndices(int size) {
			return sortedIndices(size, 0);
		}
		/** Create sorted indices outside the next smaller type's value space + offset*/
		public Config sortedIndices(int size, int offset) {
			assertNotNull(valueType, "Value type missing");
			indices = IndexSetTest.sortedIndices(valueType, size, offset);
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
		public Config defaultFeatures() { this.features.addAll(IndexSet.DEFAULT_FEATURES); return this; }
		public Config features(Set<Feature> features) { this.features.addAll(features); return this; }
		public Config features(Feature...features) { Collections.addAll(this.features, features); return this; }
		public Config sorted(boolean sorted) { this.sorted = sorted; return this; }
		public Config autoDetectSorted(boolean autoDetectSorted) { this.autoDetectSorted = autoDetectSorted; return this; }
		public Config determineSorted() {
			if(autoDetectSorted) {
				this.sorted = ArrayUtils.isSorted(indices, 0, indices.length);
			}
			return this;
		}
		public Config valueType(IndexValueType valueType) { this.valueType = valueType; return this; }

		public Config limit(IndexValueType type) {
			if(indices!=null) {
				indices = LongStream.of(indices)
						.filter(v -> v<=type.maxValue())
						.toArray();
			}
			return this;
		}

		@Override
		public Config clone() {
			Config clone;
			try {
				clone = (Config) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new InternalError(e);
			}

			clone.features = EnumSet.copyOf(features);
			clone.indices = indices==null ? null : indices.clone();
			clone.rand = rand==null ? null : rand.clone();

			return clone;
		}

		public Config validate() {
			assertNotNull(indices, "Indices missing");
			assertNotNull(features, "Features missing");
			assertNotNull(valueType, "Value type missing");
			assertNotNull(label, "Label missing");

			if(set==null && constructor!=null) {
				set = constructor.apply(this);
			}

			assertNotNull(set, "IndexSet missing");

//			assertTrue(indices.length>0, "Indices must not be empty");
//			assertFalse(set.isEmpty(), "Set under test must not be empty");
			assertFalse(features.isEmpty(), "Features must not be empty");

			return this;
		}

		public Stream<Config> withSubSets() {
			if(getIndices().length>0 && features.contains(Feature.EXPORTABLE)) {
				long[] indices = getIndices();

				int slot = rand.random(0, indices.length);
				long[] single = new long[] {indices[slot]};
				IndexSet singleSub = getSet().subSet(slot, slot);

				int from = rand.random(0, indices.length);
				int to = rand.random(from, indices.length);
				long[] subs = Arrays.copyOfRange(indices, from, to+1);
				IndexSet subSet = getSet().subSet(from, to);

				return Stream.of(
						// Raw full set
						this,
						// Random sub with size==1
						clone()
						.label(label+" sub ["+slot+"]")
						.indices(single)
						.set(singleSub)
						.sorted(true),
						// Random sub with size>1
						clone()
						.label(label+" sub ["+from+"-"+to+"]")
						.indices(subs)
						.set(subSet)
						.determineSorted());
			}

			return Stream.of(this);
		}

		public String getLabel() { return label; }
		public boolean isSorted() { return sorted; }
		public IndexValueType getValueType() { return valueType; }
		public long[] getIndices() { return indices; }
		public IndexSet getSet() {
			if(set==null && constructor!=null) {
				set = constructor.apply(this);
			}
			return set;
		}
	}
}
