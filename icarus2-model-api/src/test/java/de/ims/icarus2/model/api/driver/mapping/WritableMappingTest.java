/**
 *
 */
package de.ims.icarus2.model.api.driver.mapping;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.func.ThrowingBiConsumer;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
public interface WritableMappingTest<M extends WritableMapping> extends MappingTest<M> {

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.WritableMapping#newWriter()}.
	 */
	@Test
	default Stream<DynamicNode> testNewWriter() {
		return basicTests((config, instance) -> {
			MappingWriter writer = instance.newWriter();
			assertNotNull(writer);
			assertSame(instance, writer.getSource());
		});
	}

	default Stream<DynamicNode> writerTests(ThrowingBiConsumer<Config<M>, MappingWriter> task) {
		return configurations().map(config -> dynamicTest(config.label, () -> {
			try(M mapping = config.create();
					MappingWriter writer = mapping.newWriter()) {
				task.accept(config, writer);
			}
		}));
	}

	default Stream<Coverage> coverages() {
		return Stream.of(Coverage.values());
	}

	default Stream<Relation> relations() {
		return Stream.of(Relation.values());
	}


	// WRITER TESTS

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(long, long)}.
	 */
	@TestFactory
	@RandomizedTest
	default Stream<DynamicNode> testMapLongLong(RandomGenerator rng) {
		return configurations().map(config -> dynamicContainer(config.label,
				// grab a couple values for every test variable
				coverages().flatMap(coverage ->
				IntStream.of(1, 10, 100).boxed().flatMap(count ->
				IntStream.of(1, 2, 4).boxed().flatMap(multiplier ->
				// create randomized sequences of mappings
				MappingTestUtils.create1to1Mappings(rng, coverage, count.intValue(),
						MappingTestUtils.randRange(rng, count.intValue(), multiplier.intValue()),
						MappingTestUtils.randRange(rng, count.intValue(), multiplier.intValue())).map(data ->
						// test each sequence in isolation
						dynamicTest(String.format("cov=%s count=%d mult=%d mode=%s",
								coverage, count, multiplier, data.first), () -> {
									try(M mapping = config.create();
											MappingWriter writer = mapping.newWriter()) {
										// in this test we only write the date, no read verification!
										data.second.forEach(p -> writer.map(
												p.first.longValue(), p.second.longValue()));
									}
						})))))));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(long, long, long, long)}.
	 */
	@TestFactory
	@RandomizedTest
	default Stream<DynamicNode> testMapLongLongLongLong(RandomGenerator rng) {
		fail("Not yet implemented"); // TODO
		return null;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(de.ims.icarus2.model.api.driver.indices.IndexSet, de.ims.icarus2.model.api.driver.indices.IndexSet)}.
	 */
	@TestFactory
	@RandomizedTest
	default Stream<DynamicNode> testMapIndexSetIndexSet(RandomGenerator rng) {
		fail("Not yet implemented"); // TODO
		return null;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.MappingWriter#map(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
	 */
	@TestFactory
	@RandomizedTest
	default Stream<DynamicNode> testMapIndexSetArrayIndexSetArray(RandomGenerator rng) {
		fail("Not yet implemented"); // TODO
		return null;
	}

	//READER TESTS
}
